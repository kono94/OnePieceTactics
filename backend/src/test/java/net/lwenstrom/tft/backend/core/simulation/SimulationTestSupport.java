package net.lwenstrom.tft.backend.core.simulation;

import java.util.List;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeProvider;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.game.onepiece.OnePieceGameModeProvider;
import net.lwenstrom.tft.backend.game.pokemon.PokemonGameModeProvider;
import tools.jackson.databind.json.JsonMapper;

final class SimulationTestSupport {

    private SimulationTestSupport() {}

    static Fixture createFixture() {
        var jsonMapper = JsonMapper.builder().build();
        List<GameModeProvider> providers =
                List.of(new OnePieceGameModeProvider(jsonMapper), new PokemonGameModeProvider(jsonMapper));
        var registry = new GameModeRegistry(providers, "pokemon");
        var dataLoader = new DataLoader(registry, jsonMapper);
        dataLoader.loadData();
        return new Fixture(registry, dataLoader, new CombatSimulator(dataLoader, registry));
    }

    record Fixture(GameModeRegistry registry, DataLoader dataLoader, CombatSimulator simulator) {}
}
