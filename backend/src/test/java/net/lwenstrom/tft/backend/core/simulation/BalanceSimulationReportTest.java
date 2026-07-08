package net.lwenstrom.tft.backend.core.simulation;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.engine.ShopOdds;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.test.SeededRandomProvider;
import org.junit.jupiter.api.Test;

class BalanceSimulationReportTest {
    private static final List<Integer> BOARD_SIZES = List.of(2, 3, 4, 5, 6, 7);
    private static final List<Integer> STAR_LEVELS = List.of(1, 2, 3);
    private static final Map<Integer, List<List<Integer>>> COST_PROFILES_BY_BOARD_SIZE =
            createCostProfilesByBoardSize();
    private static final int MIN_IMPACT_APPEARANCES = 20;

    @Test
    void writeBalanceSimulationReport() throws IOException {
        assumeTrue(Boolean.getBoolean("simulation.report"));

        var fixture = SimulationTestSupport.createFixture();
        var mode = GameMode.fromString(System.getProperty("simulation.mode", "pokemon"));
        var seed = Long.getLong("simulation.seed", 42L);
        var matchups = Integer.getInteger("simulation.matchups", Integer.getInteger("simulation.runs", 100_000));
        var threads = simulationThreads(matchups);
        var outputDir = Path.of("target", "simulation-reports");
        Files.createDirectories(outputDir);

        var scenarios = generateBoardMatchups(fixture, mode, seed, matchups, threads);
        var coverage = coverageStats(scenarios);
        var unitImpacts = unitImpactRows(scenarios, mode, fixture);
        var traitImpacts = traitImpactRows(scenarios, mode, fixture);
        var traitPairImpacts = traitPairImpactRows(scenarios, mode, fixture);

        Files.writeString(
                outputDir.resolve("balance-report.md"),
                toMarkdown(
                        mode,
                        seed,
                        matchups,
                        threads,
                        coverage,
                        scenarios,
                        unitImpacts,
                        traitImpacts,
                        traitPairImpacts));
        deleteCsvReports(outputDir);
    }

    private int simulationThreads(int matchups) {
        var configuredThreads = Integer.getInteger("simulation.threads", 1);
        if (configuredThreads < 1) {
            return 1;
        }
        return Math.min(configuredThreads, Math.max(1, matchups));
    }

    private List<ScenarioResult> generateBoardMatchups(
            SimulationTestSupport.Fixture fixture, GameMode mode, long seed, int matchups, int threads) {
        var workloads = generateScenarioWorkloads(fixture, mode, seed, matchups);
        var progress = new ProgressBar(
                "Simulating combats",
                workloads.size(),
                Boolean.parseBoolean(System.getProperty("simulation.progress", "true")));

        try {
            if (threads <= 1) {
                return simulateSequentially(fixture, workloads, progress);
            }
            return simulateInParallel(fixture, workloads, threads, progress);
        } finally {
            progress.finish(workloads.size());
        }
    }

    private List<ScenarioWorkload> generateScenarioWorkloads(
            SimulationTestSupport.Fixture fixture, GameMode mode, long seed, int matchups) {
        var randomProvider = new SeededRandomProvider(seed);
        var allUnits = fixture.dataLoader().getAllUnits(mode);
        var unitsByCost = allUnits.stream()
                .collect(Collectors.groupingBy(UnitDefinition::cost, LinkedHashMap::new, Collectors.toList()));
        var workloads = new ArrayList<ScenarioWorkload>();

        for (var index = 0; index < matchups; index++) {
            var boardSize = BOARD_SIZES.get(index % BOARD_SIZES.size());
            var starLevel = STAR_LEVELS.get((index / BOARD_SIZES.size()) % STAR_LEVELS.size());
            var boardOne =
                    generateBoard(unitsByCost, randomProvider, boardSize, starLevel, "Matchup " + (index + 1) + " A");
            var boardTwo =
                    generateBoard(unitsByCost, randomProvider, boardSize, starLevel, "Matchup " + (index + 1) + " B");
            var request = new CombatSimulationRequest(
                    mode,
                    boardOne.spec(),
                    boardTwo.spec(),
                    1,
                    randomProvider.getRandom().nextLong());
            workloads.add(new ScenarioWorkload(boardOne, boardTwo, request));
        }
        return workloads;
    }

    private List<ScenarioResult> simulateSequentially(
            SimulationTestSupport.Fixture fixture, List<ScenarioWorkload> workloads, ProgressBar progress) {
        var scenarios = new ArrayList<ScenarioResult>();
        for (var index = 0; index < workloads.size(); index++) {
            scenarios.add(simulateWorkload(fixture, workloads.get(index)));
            progress.step(index + 1);
        }
        return scenarios;
    }

    private List<ScenarioResult> simulateInParallel(
            SimulationTestSupport.Fixture fixture,
            List<ScenarioWorkload> workloads,
            int threads,
            ProgressBar progress) {
        var executor = Executors.newFixedThreadPool(threads);
        try {
            CompletionService<ScenarioResult> completionService = new ExecutorCompletionService<>(executor);
            workloads.forEach(workload -> completionService.submit(() -> simulateWorkload(fixture, workload)));

            var scenarios = new ArrayList<ScenarioResult>();
            for (var completed = 0; completed < workloads.size(); completed++) {
                scenarios.add(completedResult(completionService));
                progress.step(completed + 1);
            }
            return scenarios;
        } finally {
            executor.shutdownNow();
        }
    }

    private ScenarioResult completedResult(CompletionService<ScenarioResult> completionService) {
        try {
            return completionService.take().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Simulation report interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Simulation report failed", e.getCause());
        }
    }

    private ScenarioResult simulateWorkload(SimulationTestSupport.Fixture fixture, ScenarioWorkload workload) {
        return new ScenarioResult(
                workload.boardOne(), workload.boardTwo(), fixture.simulator().simulate(workload.request()));
    }

    private GeneratedBoard generateBoard(
            Map<Integer, List<UnitDefinition>> unitsByCost,
            SeededRandomProvider randomProvider,
            int boardSize,
            int starLevel,
            String boardName) {
        var selected = new LinkedHashMap<String, UnitDefinition>();
        var costProfile = rollCostProfile(boardSize, randomProvider);

        for (var cost : costProfile) {
            addRandomUnitFromCost(selected, unitsByCost, cost, randomProvider);
        }

        if (selected.size() < boardSize) {
            allowedCosts(boardSize).forEach(cost -> unitsByCost.getOrDefault(cost, List.of()).stream()
                    .filter(unit -> !selected.containsKey(unit.lineId()))
                    .forEach(unit -> selected.putIfAbsent(unit.lineId(), unit)));
        }

        var specs = new ArrayList<UnitSpec>();
        var slot = 0;
        var positionedUnits =
                new ArrayList<>(selected.values().stream().limit(boardSize).toList());
        randomProvider.shuffle(positionedUnits);
        for (var unit : positionedUnits) {
            specs.add(new UnitSpec(unit.id(), starLevel, slot % 9, 2 - (slot / 3)));
            slot++;
        }

        return new GeneratedBoard(
                new BoardSpec(boardName + " L" + boardSize + " " + starLevel + " star", boardSize, specs), costProfile);
    }

    private List<Integer> rollCostProfile(int boardSize, SeededRandomProvider randomProvider) {
        var profiles = COST_PROFILES_BY_BOARD_SIZE.get(boardSize);
        return profiles.get(randomProvider.nextInt(profiles.size()));
    }

    private void addRandomUnitFromCost(
            Map<String, UnitDefinition> selected,
            Map<Integer, List<UnitDefinition>> unitsByCost,
            int cost,
            SeededRandomProvider randomProvider) {
        var candidates = unitsByCost.getOrDefault(cost, List.of());
        if (candidates.isEmpty()) {
            return;
        }

        for (var attempt = 0; attempt < candidates.size() * 2; attempt++) {
            var unit = candidates.get(randomProvider.nextInt(candidates.size()));
            if (!selected.containsKey(unit.lineId())) {
                selected.put(unit.lineId(), unit);
                return;
            }
        }

        candidates.stream()
                .filter(unit -> !selected.containsKey(unit.lineId()))
                .findFirst()
                .ifPresent(unit -> selected.put(unit.lineId(), unit));
    }

    private List<ImpactRow> unitImpactRows(
            List<ScenarioResult> scenarios, GameMode mode, SimulationTestSupport.Fixture fixture) {
        var unitById = fixture.dataLoader().getAllUnits(mode).stream()
                .collect(Collectors.toMap(UnitDefinition::id, Function.identity()));
        var rows = new HashMap<String, ImpactAccumulator>();
        scenarios.forEach(scenario -> {
            addUnitImpact(rows, scenario.boardOne(), scenario.result().boardOneWinRate(), unitById);
            addUnitImpact(rows, scenario.boardTwo(), scenario.result().boardTwoWinRate(), unitById);
        });
        return sortedImpactRows(rows);
    }

    private void addUnitImpact(
            Map<String, ImpactAccumulator> rows,
            GeneratedBoard board,
            double winRate,
            Map<String, UnitDefinition> unitById) {
        board.spec().units().stream()
                .map(unit -> unitImpactKey(unit, unitById))
                .distinct()
                .forEach(
                        key -> rows.computeIfAbsent(key, ImpactAccumulator::new).add(winRate));
    }

    private String unitImpactKey(UnitSpec spec, Map<String, UnitDefinition> unitById) {
        var unit = unitById.get(spec.definitionId());
        var unitName = unit == null ? spec.definitionId() : unit.name();
        var cost = unit == null ? "?" : Integer.toString(unit.cost());
        return spec.starLevel() + "|" + unitName + "|" + cost;
    }

    private List<ImpactRow> traitImpactRows(
            List<ScenarioResult> scenarios, GameMode mode, SimulationTestSupport.Fixture fixture) {
        var unitById = fixture.dataLoader().getAllUnits(mode).stream()
                .collect(Collectors.toMap(UnitDefinition::id, Function.identity()));
        var rows = new HashMap<String, ImpactAccumulator>();
        scenarios.forEach(scenario -> {
            addTraitImpact(rows, scenario.boardOne(), scenario.result().boardOneWinRate(), unitById);
            addTraitImpact(rows, scenario.boardTwo(), scenario.result().boardTwoWinRate(), unitById);
        });
        return sortedImpactRows(rows);
    }

    private void addTraitImpact(
            Map<String, ImpactAccumulator> rows,
            GeneratedBoard board,
            double winRate,
            Map<String, UnitDefinition> unitById) {
        traitCounts(board, unitById).forEach((trait, count) -> {
            var key = trait + "=" + count;
            rows.computeIfAbsent(key, ImpactAccumulator::new).add(winRate);
        });
    }

    private List<ImpactRow> traitPairImpactRows(
            List<ScenarioResult> scenarios, GameMode mode, SimulationTestSupport.Fixture fixture) {
        var unitById = fixture.dataLoader().getAllUnits(mode).stream()
                .collect(Collectors.toMap(UnitDefinition::id, Function.identity()));
        var rows = new HashMap<String, ImpactAccumulator>();
        scenarios.forEach(scenario -> {
            addTraitPairImpact(rows, scenario.boardOne(), scenario.result().boardOneWinRate(), unitById);
            addTraitPairImpact(rows, scenario.boardTwo(), scenario.result().boardTwoWinRate(), unitById);
        });
        return sortedImpactRows(rows);
    }

    private void addTraitPairImpact(
            Map<String, ImpactAccumulator> rows,
            GeneratedBoard board,
            double winRate,
            Map<String, UnitDefinition> unitById) {
        var traits = traitCounts(board, unitById).keySet().stream().sorted().toList();
        for (var first = 0; first < traits.size(); first++) {
            for (var second = first + 1; second < traits.size(); second++) {
                var key = traits.get(first) + "+" + traits.get(second);
                rows.computeIfAbsent(key, ImpactAccumulator::new).add(winRate);
            }
        }
    }

    private Map<String, Long> traitCounts(GeneratedBoard board, Map<String, UnitDefinition> unitById) {
        return board.spec().units().stream()
                .map(unit -> {
                    var definition = unitById.get(unit.definitionId());
                    return definition == null ? List.<String>of() : definition.getTraits(unit.starLevel());
                })
                .flatMap(List::stream)
                .filter(unit -> unit != null)
                .map(TraitManager::normalizeTraitId)
                .sorted()
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
    }

    private List<ImpactRow> sortedImpactRows(Map<String, ImpactAccumulator> rows) {
        return rows.values().stream()
                .map(ImpactAccumulator::toRow)
                .sorted(Comparator.comparing(
                                BalanceSimulationReportTest::hasEnoughAppearances, Comparator.reverseOrder())
                        .thenComparing(
                                Comparator.comparingDouble(ImpactRow::winRate).reversed())
                        .thenComparing(
                                Comparator.comparingInt(ImpactRow::appearances).reversed())
                        .thenComparing(ImpactRow::key))
                .toList();
    }

    private static boolean hasEnoughAppearances(ImpactRow row) {
        return row.appearances() >= MIN_IMPACT_APPEARANCES;
    }

    private String toMarkdown(
            GameMode mode,
            long seed,
            int matchups,
            int threads,
            CoverageStats coverage,
            List<ScenarioResult> scenarios,
            List<ImpactRow> unitImpacts,
            List<ImpactRow> traitImpacts,
            List<ImpactRow> traitPairImpacts) {
        var builder = new StringBuilder();
        builder.append("# Balance Simulation Report\n\n");
        builder.append("- Mode: ").append(mode.getValue()).append('\n');
        builder.append("- Seed: ").append(seed).append('\n');
        builder.append("- Board matchups: ").append(matchups).append('\n');
        builder.append("- Combat samples per matchup: 1\n");
        builder.append("- Simulation threads: ").append(threads).append('\n');
        builder.append("- Board sizes: ").append(BOARD_SIZES).append('\n');
        builder.append("- Star levels: all-1-star, all-2-star, all-3-star boards\n");
        builder.append("- Unit costs: uniformly sampled from all cost profiles available at board size/player level\n");
        builder.append("- Draws: included as 0% win contribution for both boards\n");
        builder.append("- Completed combats: ").append(scenarios.size()).append("\n\n");
        builder.append("## Sample Coverage\n\n");
        builder.append("| Metric | Count |\n");
        builder.append("|---|---:|\n");
        builder.append("| Unique board signatures | ")
                .append(coverage.uniqueBoards())
                .append(" |\n");
        builder.append("| Unique matchup signatures | ")
                .append(coverage.uniqueMatchups())
                .append(" |\n");
        builder.append("| Unique cost profiles sampled | ")
                .append(coverage.uniqueCostProfiles())
                .append(" |\n");

        appendUnitSections(builder, unitImpacts);
        appendImpactSection(builder, "Trait Count Ranking", traitImpacts, 40);
        appendImpactSection(builder, "Trait Pair Ranking", traitPairImpacts, 40);
        appendCostProfileSection(builder);
        return builder.toString();
    }

    private void appendUnitSections(StringBuilder builder, List<ImpactRow> rows) {
        for (var starLevel : STAR_LEVELS) {
            builder.append("\n## Unit Ranking - ").append(starLevel).append(" Star\n\n");
            builder.append("| Rank | Unit | Cost | Appearances | Avg Win % |\n");
            builder.append("|---:|---|---:|---:|---:|\n");
            var rank = new int[] {1};
            rows.stream().filter(row -> row.starLevel() == starLevel).limit(60).forEach(row -> builder.append("| ")
                    .append(rank[0]++)
                    .append(" | ")
                    .append(row.unitName())
                    .append(" | ")
                    .append(row.cost())
                    .append(" | ")
                    .append(row.appearances())
                    .append(" | ")
                    .append(percent(row.winRate()))
                    .append(" |\n"));
        }
    }

    private void appendImpactSection(StringBuilder builder, String title, List<ImpactRow> rows, int limit) {
        builder.append("\n## ").append(title).append("\n\n");
        builder.append("| Rank | Key | Appearances | Avg Win % |\n");
        builder.append("|---:|---|---:|---:|\n");
        var rank = new int[] {1};
        rows.stream().limit(limit).forEach(row -> builder.append("| ")
                .append(rank[0]++)
                .append(" | ")
                .append(row.key())
                .append(" | ")
                .append(row.appearances())
                .append(" | ")
                .append(percent(row.winRate()))
                .append(" |\n"));
    }

    private void appendCostProfileSection(StringBuilder builder) {
        builder.append("\n## Cost Profiles Used\n\n");
        builder.append("| Board Size / Level | Available Costs | Possible Cost Profiles |\n");
        builder.append("|---:|---|---:|\n");
        BOARD_SIZES.forEach(level -> builder.append("| ")
                .append(level)
                .append(" | ")
                .append(allowedCosts(level))
                .append(" | ")
                .append(COST_PROFILES_BY_BOARD_SIZE.get(level).size())
                .append(" |\n"));
    }

    private void deleteCsvReports(Path outputDir) throws IOException {
        Files.deleteIfExists(outputDir.resolve("combat-results.csv"));
        Files.deleteIfExists(outputDir.resolve("unit-impact.csv"));
        Files.deleteIfExists(outputDir.resolve("trait-impact.csv"));
        Files.deleteIfExists(outputDir.resolve("trait-pair-impact.csv"));
        Files.deleteIfExists(outputDir.resolve("trait-combination-impact.csv"));
        Files.deleteIfExists(outputDir.resolve("bot-snapshots.csv"));
    }

    private String percent(double value) {
        return String.format(Locale.ROOT, "%.2f%%", value * 100.0);
    }

    private CoverageStats coverageStats(List<ScenarioResult> scenarios) {
        Set<String> boardSignatures = scenarios.stream()
                .flatMap(scenario -> List.of(scenario.boardOne(), scenario.boardTwo()).stream())
                .map(this::boardSignature)
                .collect(Collectors.toSet());
        Set<String> matchupSignatures =
                scenarios.stream().map(this::matchupSignature).collect(Collectors.toSet());
        Set<String> costProfileSignatures = scenarios.stream()
                .flatMap(scenario -> List.of(scenario.boardOne(), scenario.boardTwo()).stream())
                .map(this::costProfileSignature)
                .collect(Collectors.toSet());

        return new CoverageStats(boardSignatures.size(), matchupSignatures.size(), costProfileSignatures.size());
    }

    private String matchupSignature(ScenarioResult scenario) {
        var first = boardSignature(scenario.boardOne());
        var second = boardSignature(scenario.boardTwo());
        return first.compareTo(second) <= 0 ? first + " vs " + second : second + " vs " + first;
    }

    private String boardSignature(GeneratedBoard board) {
        var units = board.spec().units().stream()
                .map(unit -> unit.starLevel() + ":" + unit.definitionId())
                .sorted()
                .collect(Collectors.joining(","));
        return board.spec().level() + "|" + units;
    }

    private String costProfileSignature(GeneratedBoard board) {
        return board.spec().level() + "|"
                + board.costProfile().stream().map(String::valueOf).collect(Collectors.joining("-"));
    }

    private static Map<Integer, List<List<Integer>>> createCostProfilesByBoardSize() {
        var profilesByBoardSize = new HashMap<Integer, List<List<Integer>>>();
        for (var boardSize : BOARD_SIZES) {
            var profiles = new ArrayList<List<Integer>>();
            appendCostProfiles(allowedCosts(boardSize), boardSize, 0, new ArrayList<>(), profiles);
            profilesByBoardSize.put(boardSize, List.copyOf(profiles));
        }
        return Map.copyOf(profilesByBoardSize);
    }

    private static void appendCostProfiles(
            List<Integer> allowedCosts,
            int boardSize,
            int costIndex,
            List<Integer> current,
            List<List<Integer>> profiles) {
        if (current.size() == boardSize) {
            profiles.add(List.copyOf(current));
            return;
        }

        for (var index = costIndex; index < allowedCosts.size(); index++) {
            current.add(allowedCosts.get(index));
            appendCostProfiles(allowedCosts, boardSize, index, current, profiles);
            current.removeLast();
        }
    }

    private static List<Integer> allowedCosts(int playerLevel) {
        var odds = ShopOdds.getOddsForLevel(playerLevel);
        var costs = new ArrayList<Integer>();
        for (var index = 0; index < odds.length; index++) {
            if (odds[index] > 0) {
                costs.add(index + 1);
            }
        }
        return List.copyOf(costs);
    }

    private record GeneratedBoard(BoardSpec spec, List<Integer> costProfile) {}

    private record ScenarioWorkload(
            GeneratedBoard boardOne, GeneratedBoard boardTwo, CombatSimulationRequest request) {}

    private record ScenarioResult(GeneratedBoard boardOne, GeneratedBoard boardTwo, CombatSimulationResult result) {}

    private record CoverageStats(int uniqueBoards, int uniqueMatchups, int uniqueCostProfiles) {}

    private record ImpactRow(String key, int appearances, double winRate) {
        private int starLevel() {
            return Integer.parseInt(key.split("\\|", -1)[0]);
        }

        private String unitName() {
            return key.split("\\|", -1)[1];
        }

        private String cost() {
            return key.split("\\|", -1)[2];
        }
    }

    private static final class ImpactAccumulator {
        private final String key;
        private int appearances;
        private double totalWinRate;

        private ImpactAccumulator(String key) {
            this.key = key;
        }

        private void add(double winRate) {
            appearances++;
            totalWinRate += winRate;
        }

        private ImpactRow toRow() {
            return new ImpactRow(key, appearances, appearances == 0 ? 0 : totalWinRate / appearances);
        }
    }

    private static final class ProgressBar {
        private static final int WIDTH = 30;
        private static final long UPDATE_INTERVAL_NANOS = 200_000_000L;

        private final String label;
        private final int total;
        private final boolean enabled;
        private final long startNanos;
        private long lastUpdateNanos;
        private int lastLineLength;
        private int lastRenderedCurrent;

        private ProgressBar(String label, int total, boolean enabled) {
            this.label = label;
            this.total = Math.max(1, total);
            this.enabled = enabled;
            this.startNanos = System.nanoTime();
            this.lastUpdateNanos = 0L;
            this.lastRenderedCurrent = -1;
        }

        private void step(int current) {
            if (!enabled) {
                return;
            }

            var now = System.nanoTime();
            if (current < total && now - lastUpdateNanos < UPDATE_INTERVAL_NANOS) {
                return;
            }

            lastUpdateNanos = now;
            render(Math.min(current, total), false);
        }

        private void finish(int current) {
            if (!enabled) {
                return;
            }

            var cappedCurrent = Math.min(current, total);
            if (lastRenderedCurrent != cappedCurrent) {
                render(cappedCurrent, true);
            }
            System.out.println();
        }

        private void render(int current, boolean finalRender) {
            var ratio = (double) current / total;
            var filled = (int) Math.round(ratio * WIDTH);
            var bar = "#".repeat(filled) + "-".repeat(WIDTH - filled);
            var elapsedNanos = Math.max(1L, System.nanoTime() - startNanos);
            var elapsedSeconds = elapsedNanos / 1_000_000_000.0;
            var rate = current / elapsedSeconds;
            var line = String.format(
                    Locale.ROOT,
                    "%s: %3.0f%%|%s| %,d/%,d [elapsed %s, %.0f combat/s]",
                    label,
                    ratio * 100.0,
                    bar,
                    current,
                    total,
                    formatDuration(elapsedNanos),
                    rate);

            if (line.length() < lastLineLength) {
                line += " ".repeat(lastLineLength - line.length());
            }
            lastLineLength = line.length();
            lastRenderedCurrent = current;

            System.out.print('\r' + line);
            if (finalRender) {
                System.out.flush();
            }
        }

        private String formatDuration(long elapsedNanos) {
            var totalSeconds = elapsedNanos / 1_000_000_000L;
            var minutes = totalSeconds / 60;
            var seconds = totalSeconds % 60;
            return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
        }
    }
}
