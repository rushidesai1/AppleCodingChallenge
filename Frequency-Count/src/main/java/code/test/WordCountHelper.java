package code.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static code.test.FixedBatchSpliterator.withBatchSize;

public class WordCountHelper {


    static public Map<String, Long> countAndSortByFrequency(final String path, final int batchSize) throws IOException {
        final Stream<String> stringStream = fetchStream(path, batchSize);
        try (final Stream<String> lines = stringStream) {
            final Map<String, Long> wordFreqMap = countFrequency(lines);
            final Map<String, Long> finalMap = sortMapByValue(wordFreqMap);
            return finalMap;

        }
    }

    public static Stream<String> fetchStream(final String path, final int batchSize) throws IOException {
        return withBatchSize(Files.lines(Paths.get(path)), batchSize);
    }

    public static Map<String, Long> countFrequency(final Stream<String> lines) {
        return lines.parallel()
                .flatMap(WordCountHelper::processLine)
                .filter(word -> word != null && !word.isEmpty())
                .collect(
                        Collectors.groupingBy(
                                Function.identity(), Collectors.counting()
                        )
                );
    }

    public static Map<String, Long> sortMapByValue(final Map<String, Long> wordFreqMap) {
        return wordFreqMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public static void printMap(final Map<String, Long> orderedMap) {
        orderedMap.entrySet().forEach(System.out::println);
    }

    public static Stream<String> processLine(final String line) {
        return Arrays.stream(
                santizeAndsplitString(line, punctuationRegex())
        );
    }

    public static String punctuationRegex() {
        return "(?=[a-zA-Z])([a-zA-Z'-]+)";
    }

    public static String[] santizeAndsplitString(final String stringToTest, final String regex) {
        final List<String> allMatches = new ArrayList<>();
        final Matcher m = Pattern.compile(regex)
                .matcher(stringToTest);
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches.stream().filter(s -> s.matches("\\S+")).toArray(String[]::new);

    }
}
