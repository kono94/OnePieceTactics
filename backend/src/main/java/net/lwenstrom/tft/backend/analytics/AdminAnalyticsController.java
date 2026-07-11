package net.lwenstrom.tft.backend.analytics;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import net.lwenstrom.tft.backend.core.model.GameMode;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {
    private final AdminAnalyticsRepository repository;

    @GetMapping("/summary")
    ResponseEntity<AdminAnalyticsRepository.Summary> summary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String mode) {
        var range = range(from, to);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(repository.summary(range.from(), range.to(), mode(mode)));
    }

    @GetMapping("/runs")
    ResponseEntity<AdminAnalyticsRepository.RunsPage> runs(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String analyticsClientId,
            @RequestParam(required = false) Boolean abandoned,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int size) {
        if (size < 1 || size > 100) {
            throw badRequest("size must be between 1 and 100");
        }
        var range = range(from, to);
        AdminAnalyticsRepository.Cursor decodedCursor = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                decodedCursor = repository.decodeCursor(cursor);
            } catch (IllegalArgumentException exception) {
                throw badRequest("cursor is invalid");
            }
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(repository.runs(
                        range.from(), range.to(), mode(mode), analyticsClientId, abandoned, decodedCursor, size));
    }

    @GetMapping("/runs/{runId}")
    ResponseEntity<AdminAnalyticsRepository.RunDetail> runDetail(@PathVariable String runId) {
        var detail = repository.runDetail(runId);
        if (detail == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Run not found");
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(detail);
    }

    private Range range(String from, String to) {
        try {
            var parsedFrom = from == null ? Instant.EPOCH : Instant.parse(from);
            var parsedTo = to == null ? Instant.now() : Instant.parse(to);
            if (!parsedFrom.isBefore(parsedTo)) {
                throw badRequest("from must be before to");
            }
            return new Range(parsedFrom.toEpochMilli(), parsedTo.toEpochMilli());
        } catch (DateTimeParseException exception) {
            throw badRequest("from and to must be ISO-8601 UTC timestamps");
        }
    }

    private String mode(String mode) {
        if (mode == null || mode.isBlank()) {
            return null;
        }
        try {
            return GameMode.valueOf(mode.toUpperCase()).name();
        } catch (IllegalArgumentException exception) {
            throw badRequest("mode is invalid");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, message);
    }

    private record Range(long from, long to) {}
}
