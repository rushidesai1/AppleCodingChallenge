package code.spliterator

import code.api.WordCount
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Stream

/**
 * Created by Rushi Desai on 11/10/2017
 */
class WordCountSpec extends Specification {
    WordCount wordCount

    void setup() {
        wordCount = new WordCount(10)
    }

    void cleanup() {
    }

    @Shared
    String dummyPath = '/dummyPath'

    //Doesnt test anything. Just executes api on the file given.
    def "test countFrequencyAndPrint from file"() {
        given: "File with certain text"
        String pathString = "src/test/resources/paragraph.txt"

        when:
        wordCount.countFrequencyAndPrint(pathString)

        then:
        true
    }

    def "test countFrequencyAndPrint"() {
        given: "instance of a WordCount with a fixed batchSize and WordCountDelegate is mocked"
        wordCount.wordCountDelegate = Mock(WordCount.WordCountDelegate)
        WordCount.WordCountDelegate wordCountDelegate = wordCount.wordCountDelegate

        when: "countFrequencyAndPrint is called"
        wordCount.countFrequencyAndPrint(dummyPath)

        then:
        1 * wordCountDelegate.countAndSortByFrequency(*_) >> { println("I am called"); return [:] }
        1 * wordCountDelegate.printMap(_) >> { println "printMap am called" }
    }

    @Unroll
    def "test countAndSortByFrequency"() {

        given: "A a path to file"
        String path = "\\DummyPath"

        and: "wordCountDelegateMocked"
        wordCount.wordCountDelegate = Spy(WordCount.WordCountDelegate)

        when: "countAndSortByFrequency is called"
        Map<String, Long> map = wordCount.wordCountDelegate.countAndSortByFrequency(path, 10)

        then:
        1 * wordCount.wordCountDelegate.fetchStream(_, _) >> Stream.of(line)
        1 * wordCount.wordCountDelegate.countFrequency(_) >> Mock(Map)
        1 * wordCount.wordCountDelegate.sortMapByValue(_) >> Mock(Map)

        where:
        line                                                   || expectedMap
        "This is a String I want to test since it is a String" || ["This": 1, "is": 2, "a": 2, "I": 1, "to": 1, "String": 1, "want": 1, "since": 1, "test": 1].sort {
            it.value
        }

    }

    def "test fetchStream"() {

    }

    @Unroll
    def "test countFrequency"() {
        given: "Stream of strings"
        Stream<String> stringStream = Stream.of(values)

        when: "countFrequency is called"
        Map<String, Long> returnMap = wordCount.wordCountDelegate.countFrequency(stringStream)

        then:
        returnMap == expected

        where:
        values                                            || expected
        ["Hello", "How", "are", "you"] as String[]        || ["Hello": 1, "How": 1, "are": 1, "you": 1]
        ["you", "How", "are", "you"] as String[]          || ["How": 1, "are": 1, "you": 2]
        ["you", "How", "are", "you-and"] as String[]      || ["you": 1, "How": 1, "are": 1, "you-and": 1]
        ["can't", "How", "are", "you-and"] as String[]    || ["How": 1, "are": 1, "you-and": 1, "can't": 1]
        ["'", ".", "hello!", "-and", "and1!"] as String[] || ["hello": 1, "and": 1, "and1": 1]
        ["hello!hello", "-", "hi,\"", "hi,"] as String[]  || ["hi": 2, "hello": 2]
        [" ", "   ", "\n", "\t"] as String[]              || [:]
        [] as String[]                                    || [:]
        null                                              || [:]

    }

    def "test sortMapByValue"() {
        given: "map with string key and long value"
        Map<String, Long> mapInput = map

        when:
        Map<String, Long> sorted = wordCount.wordCountDelegate.sortMapByValue(mapInput)

        then:
        sorted == expected

        where:
        map              || expected
        [:]              || [:]
        ['a': 3, 'b': 6] || ['b': 6, 'a': 3]
        ['a': 6, 'b': 3] || ['a': 6, 'b': 3]
        ['a': 6, '': 3]  || ['a': 6, '': 3]
        ['': 6, 'b': 3]  || ['': 6, 'b': 3]
        null             || null

    }

    def "test printMap"() {
        given: "Mock map"
        Map<String, Long> mapMock = Mock()
        Set<Map.Entry> mockEntry = Mock()

        when: "printMap is called"
        wordCount.wordCountDelegate.printMap(mapMock)

        then: "map.entrySet should be called once"
        1 * mapMock.entrySet() >> mockEntry//[Mock(Set)] as HashSet
        then: "forEach should be called once"
        1 * mockEntry.forEach(_) >> {}
    }

    @Unroll
    def "test punctuationRegex"() {

        when:
        String regex = wordCount.wordCountDelegate.punctuationRegex()

        then:
        regex == "(?=[a-zA-Z0-9])([a-zA-Z0-9'-]+)"

    }

    @Unroll
    def "test santizeAndsplitString"() {

        given:
        final String regex = wordCount.wordCountDelegate.punctuationRegex()

        when:
        String[] splitString = wordCount.wordCountDelegate.santizeAndsplitString(stringToTest, regex)

        then:
        (expected == null && splitString == null) || Arrays.equals(splitString, expected)

        where:
        stringToTest                                                                || expected
        null                                                                        || null
        ""                                                                          || [] as String[]
        "This, "                                                                    || ["This"] as String[]
        "This, is a :test"                                                          || ["This", "is", "a", "test"] as String[]
        "This, is a : test"                                                         || ["This", "is", "a", "test"] as String[]
        "This is a : , . / ; ' { } + = - _ ) ( * & ^ % \$ # @ ! ` ~ < > ? : \" [ ]" || ["This", "is", "a"] as String[]
        "This is a Don't ' I"                                                       || ["This", "is", "a", "Don't", "I"] as String[]
        "This is a Dont-Test ' I"                                                   || ["This", "is", "a", "Dont-Test", "I"] as String[]
    }
}
