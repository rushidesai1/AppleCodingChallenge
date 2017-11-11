package code.test

import exceptions.BatchSizeCannotBeNullException
import exceptions.SpliteratorCannotBeNullException
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Consumer
import java.util.stream.Stream

/**
 * Created by Rushi Desai on 11/9/2017
 */
class FixedBatchSpliteratorSpec extends Specification {
    void setup() {
    }

    void cleanup() {
    }

    @Unroll
    def "test batchedSpliterator"() {
        Spliterator mockSpliterator = Mock()
        when:
        FixedBatchSpliterator batchedSpliterator = FixedBatchSpliterator.batchedSpliterator(mockSpliterator, 10)

        then: "retiurned object shouldnt be null"
        batchedSpliterator != null

        and: "spliterator should be the same as provided"
        batchedSpliterator.spliterator == mockSpliterator
    }

    def "test batchedSpliterator null"() {
        when: "spliterator is null"
        FixedBatchSpliterator.batchedSpliterator(null, 10)

        then: "Null pointer exception is thrown"
        def e = thrown(SpliteratorCannotBeNullException)
        e.message == "Spliterator Cannot be null"
    }

    def "test batchedSpliterator batchsize 0"() {
        when: "spliterator is null"
        FixedBatchSpliterator.batchedSpliterator(null, 0)

        then: "Null pointer exception is thrown"
        def e = thrown(BatchSizeCannotBeNullException)
        e.message == "Batch Size Cannot be null"
    }

    def "test withBatchSize"() {
        final Stream mockStream = Mock()
        when:
        Stream stream = FixedBatchSpliterator.withBatchSize(mockStream, 10)

        then: "retiurned object shouldnt be null"
        1 * mockStream.spliterator() >> Mock(Spliterator)
        stream != null
    }

    def "test withBatchSize null"() {
        when: "spliterator is null"
        FixedBatchSpliterator.withBatchSize(null, 10)

        then: "Null pointer exception is thrown"
        def e = thrown(SpliteratorCannotBeNullException)
        e.message == "Spliterator Cannot be null"
    }

    def "test withBatchSize batchsize 0"() {
        when: "spliterator is null"
        FixedBatchSpliterator.withBatchSize(null, 0)

        then: "Null pointer exception is thrown"
        def e = thrown(BatchSizeCannotBeNullException)
        e.message == "Batch Size Cannot be null"
    }

    def "test tryAdvance"() {
        given: "Instance of FixedBtachSpliterator"
        Spliterator mockSpliterator = Mock()
        FixedBatchSpliterator fixedBatchSpliterator = new FixedBatchSpliterator(mockSpliterator, 10)

        when:
        fixedBatchSpliterator.tryAdvance(Mock(Consumer))

        then: "spliterator.tryAdvance should be called once"
        1 * fixedBatchSpliterator.spliterator.tryAdvance(_) >> true
    }

    def "test forEachRemaining"() {
        given: "Instance of FixedBtachSpliterator"
        Spliterator mockSpliterator = Mock()
        FixedBatchSpliterator fixedBatchSpliterator = new FixedBatchSpliterator(mockSpliterator, 10)

        when:
        fixedBatchSpliterator.forEachRemaining(Mock(Consumer))

        then: "spliterator.forEachRemaining should be called once"
        1 * fixedBatchSpliterator.spliterator.forEachRemaining(_)
    }
}
