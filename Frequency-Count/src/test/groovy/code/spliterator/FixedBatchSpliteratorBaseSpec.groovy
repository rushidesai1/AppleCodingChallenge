package code.spliterator

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by Rushi Desai on 11/10/2017
 *
 * We test FixedBatchSpliteratorBase via FixedBatchSpliterator. We only test those methods which are not overridden
 */
class FixedBatchSpliteratorBaseSpec extends Specification {
    void setup() {
    }

    void cleanup() {
    }

    @Unroll
    def "test trySplit"() {
        given: "Some Mock dummy spliterator to be wrapped"
        Spliterator mockSpliterator = Mock()
        and: "we wrap it with FixedBatchSpliterator"
        FixedBatchSpliterator batchedSpliterator = FixedBatchSpliterator.batchedSpliterator(mockSpliterator, batchSize)

        when: "call trySlpit as long as you can"
        int trySplitTimes = (elements.size() % batchSize == 0) ? (elements.size() / batchSize) : (elements.size() / batchSize + 1)
        Spliterator spliterator
        trySplitTimes.times {
            spliterator = batchedSpliterator.trySplit()
        }

        then: "try advanced should be called for elements.size times until "
        elements.size() * mockSpliterator.tryAdvance(_) >>> elements    //

        and:
        (spliterator == null) == isExpectedSpliteratorNull

        then: "No exception is thown"
        notThrown(Exception)

        where:
        elements                        | batchSize || isExpectedSpliteratorNull
        [true, true, true, false]       | 2         || false
        [true, true, true, true, false] | 2         || true
        [true, true, true, false]       | 3         || true
        []                              | 2         || true
    }
}
