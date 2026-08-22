package ch.iselaj.quarry.source.lichess;

import ch.iselaj.quarry.source.GameSource;
import ch.iselaj.quarry.source.RawGame;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Stream;

@Component
public class LichessGameSource implements GameSource {

    private static final Logger log = LoggerFactory.getLogger(LichessGameSource.class);
    private static final String BASE = "https://lichess.org/api/games/user";
    private static final Duration RATE_LIMIT_BACKOFF = Duration.ofSeconds(60);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String name() {
        return "lichess";
    }

    @Override
    public Stream<RawGame> fetchGames(String username, int max) {
        var uri = UriComponentsBuilder.fromUriString(BASE)
                .pathSegment(username)
                .queryParam("max", max)
                .queryParam("evals", true)
                .build()
                .encode()
                .toUri();

        var request = HttpRequest.newBuilder(uri)
                .header("Accept", "application/x-ndjson")
                .header("User-Agent", "quarry/0.1 (github.com/merc1x/quarry; romanovskijv508@gmail.com)")
                .timeout(Duration.ofMinutes(5))
                .GET()
                .build();

        var response = send(request);

        if (response.statusCode() == 429) {
            log.warn("Rate limited by Lichess, backing off for {}", RATE_LIMIT_BACKOFF);
            discard(response);
            sleep(RATE_LIMIT_BACKOFF);
            response = send(request);
        }

        if (response.statusCode() != 200) {
            discard(response);
            throw new IllegalStateException(
                    "Lichess returned " + response.statusCode() + " for user " + username);
        }

        return response.body()
                .filter(line -> !line.isBlank())
                .map(this::toRawGame);
    }

    private HttpResponse<Stream<String>> send(HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofLines());
        } catch (java.io.IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling Lichess", e);
        }
    }

    /**
     * Releases a response we are not going to read. {@code ofLines()} keeps the
     * connection open until its stream is consumed or closed.
     */
    private void discard(HttpResponse<Stream<String>> response) {
        response.body().close();
    }

    private RawGame toRawGame(String line) {
        try {
            var id = mapper.readTree(line).path("id").asText();
            return new RawGame(id, line);
        } catch (tools.jackson.core.JacksonException e) {
            throw new IllegalStateException("Malformed NDJSON line from Lichess", e);
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during rate limit backoff", e);
        }
    }
}