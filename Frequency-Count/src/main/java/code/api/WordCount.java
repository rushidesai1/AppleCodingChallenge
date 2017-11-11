package code.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static code.spliterator.FixedBatchSpliterator.withBatchSize;

/**
 * Object client can use.
 * <p>
 * While counting frequency I make certain assumptions since there wasnt any clear direction
 * 1. All words are case sensitive. i.e I count 'you' and 'You' are different words. (It is trivial to make then case insensitive)
 * 2. "can't" is taken as 1 word. Also, Delaney-Podmore is considered to be 1 word.
 * 3. In the worst case, count of a specific word is in the range of long.
 * 4. I ignore , . ? etc symbols. To consider as valid words I only count a-z, 0-9 and ' or - if its between a-z or/and 0-9.
 * 5. As long as above condition is satisfied, I dont check if its a valid dictionary word or not
 */
public class WordCount {

    private final int batchSize;
    private WordCountDelegate wordCountDelegate;

    public WordCount() {
        batchSize = 10;
        wordCountDelegate = new WordCountDelegate();
    }

    public WordCount(final int batchSize1) {
        batchSize = batchSize1;
        wordCountDelegate = new WordCountDelegate();
    }

    /**
     * Api exposed to client
     *
     * @param path
     * @throws IOException
     */
    public void countFrequencyAndPrint(final String path) throws IOException {
        final Map<String, Long> freq = wordCountDelegate.countAndSortByFrequency(path, batchSize);
        wordCountDelegate.printMap(freq);
    }

    static class WordCountDelegate {

        public Map<String, Long> countAndSortByFrequency(final String path, final int batchSize) throws IOException {
            final Stream<String> stringStream = fetchStream(path, batchSize);
            try (final Stream<String> lines = stringStream) {
                final Map<String, Long> wordFreqMap = countFrequency(lines);
                final Map<String, Long> finalMap = sortMapByValue(wordFreqMap);
                return finalMap;

            }
        }

        Stream<String> fetchStream(final String path, final int batchSize) throws IOException {
            return withBatchSize(Files.lines(Paths.get(path)), batchSize);
        }

        Map<String, Long> countFrequency(final Stream<String> lines) {
            if (lines == null) return null;
            return lines.parallel() //Here when we create parallel streams, each stream gets a batch of lines according to the batch size specified earlier.
                    .flatMap(this::processLine)
                    .filter(word -> word != null && !word.isEmpty())
                    .collect(
                            Collectors.groupingBy(
                                    Function.identity(), Collectors.counting()
                            )
                    );
        }

        Map<String, Long> sortMapByValue(final Map<String, Long> wordFreqMap) {
            if (wordFreqMap == null) return null;
            return wordFreqMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        }

        void printMap(final Map<String, Long> orderedMap) {
            orderedMap
                    .forEach((key, value) -> System.out.println(value + "\t" + key));
        }

        Stream<String> processLine(final String line) {
            if (line == null) return null;
            return Arrays.stream(
                    santizeAndsplitString(line, punctuationRegex())
            );
        }

        String punctuationRegex() {
            return "(?=[a-zA-Z0-9])([a-zA-Z0-9'-]+)";
        }

        String[] santizeAndsplitString(final String stringToTest, final String regex) {
            if (stringToTest == null) return null;
            final List<String> allMatches = new ArrayList<>();
            final Matcher m = Pattern.compile(regex)
                    .matcher(stringToTest);
            while (m.find()) {
                allMatches.add(m.group());
            }
            return allMatches
                    .stream()
                    .filter(s -> s.matches("\\S+"))
//                    .map(String::toLowerCase)     //Add this line to ignore case sensitivity
                    .toArray(String[]::new);

        }
    }
}
