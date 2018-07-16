package de.az.demo.mn.opa;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.lines;

/**
 * Provides facts about Chuck Norris.
 */
@Singleton
public class ChuckNorrisFacts {

    private final List<String> norrisFacts = loadNorrisFacts("/norris-facts.txt");

    /**
     * Get a random Chuck Norris fact.
     *
     * @return random Chuck Norris fact.
     */
    public String getRandom() {
        return norrisFacts.get(ThreadLocalRandom.current().nextInt(norrisFacts.size()));
    }

    private List<String> loadNorrisFacts(String location) {
        try (Stream<String> lines = lines(Paths.get(getClass().getResource(location).toURI()))) {
            return lines.filter(l -> l.contains("Chuck Norris")).collect(Collectors.toList());
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
