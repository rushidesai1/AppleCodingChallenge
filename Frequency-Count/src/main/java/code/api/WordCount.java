package code.api;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static code.spliterator.FixedBatchStreamsStreamsSpliterator.withBatchSize;

/**
 * Object client can use.
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

    public WordCount(final int batchSize1, String regex) {
        batchSize = batchSize1;
        wordCountDelegate = new WordCountDelegate(regex);
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

    private static class WordCountDelegate {

        final private Pattern COMPILED_PATTERN;


        WordCountDelegate() {
            COMPILED_PATTERN = precompilePattern(alphaNumericApostropheHyphenRegex());
        }

        WordCountDelegate(final String regex1) {
            COMPILED_PATTERN = precompilePattern(regex1);
        }

        private static Pattern precompilePattern(final String regex) {
            return Pattern.compile(regex);
        }

        public Map<String, Long> countAndSortByFrequency(final String path, final int batchSize) throws IOException {
            final Stream<String> stringStream = fetchBatchedStream(path, batchSize);
            try (final Stream<String> lines = stringStream) {
                final Map<String, Long> wordFreqMap = countFrequency(lines);
                final Map<String, Long> finalMap = sortMapByValue(wordFreqMap);
                return finalMap;

            }
        }

        /**
         * Given a valid file path, it returns a stream handle which is batched.
         *
         * @param path
         * @param batchSize
         * @return
         * @throws IOException
         */
        Stream<String> fetchBatchedStream(final String path, final int batchSize) throws IOException {
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
                    .entrySet()
                    .forEach(System.out::println);
        }

        private Stream<String> processLine(final String line) {
            if (line == null) return null;
            return Arrays.stream(
                    santizeAndSplitString(line)
            );
        }

        /**
         * https://swtch.com/~rsc/regexp/regexp1.html
         * Java regex uses recursive backtracking whose performance can be drastically improved. Thus I use google's
         * library re2j which uses https://github.com/google/re2j Thompson's NFA algorithm.
         */
        private static String alphaNumericApostropheHyphenRegex() {
//            https://regex101.com/r/2iihJA/1
            String alphaNumericAllowed = "[a-zA-Z0-9]+";
            String wordsWithApostropheInMiddle = "(?:'[a-zA-Z0-9]+)*";
            String wordsWithHypenInMiddle = "(?:-[a-zA-Z0-9]+)*";
            return alphaNumericAllowed + wordsWithApostropheInMiddle + wordsWithHypenInMiddle;
        }

        private String[] santizeAndSplitString(final String stringToTest) {
            if (stringToTest == null) return null;
            final List<String> allMatches = new ArrayList<>();
            final Matcher m = COMPILED_PATTERN
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
