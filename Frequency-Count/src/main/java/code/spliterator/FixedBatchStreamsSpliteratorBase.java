package code.spliterator;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Spliterators.spliterator;

/**
 * Created by Rushi Desai on 11/10/2017
 * <p>
 *
 * @param <T>
 */
public abstract class FixedBatchStreamsSpliteratorBase<T> implements Spliterator<T> {
    private final int batchSize;
    private final int characteristics;
    private long est;

    /**
     * We know that since we always split input to a constant batch size, this split and all further splits will
     * be of same size, we want the characteristics to have subsized.
     *
     * @param characteristics
     * @param batchSize
     * @param est
     */
    public FixedBatchStreamsSpliteratorBase(int characteristics, int batchSize, long est) {
        this.characteristics = characteristics | SUBSIZED;
        this.batchSize = batchSize;
        this.est = est;
    }

    public FixedBatchStreamsSpliteratorBase(int characteristics, int batchSize) {
        this(characteristics, batchSize, Long.MAX_VALUE);
    }

    public FixedBatchStreamsSpliteratorBase(int batchSize) {
        this(IMMUTABLE | ORDERED | NONNULL, batchSize);
    }

    public FixedBatchStreamsSpliteratorBase() {
        this(128);
    }

    /**
     * Streams api will try to split the input and when it wants to it calls this method. Our objective is to try and
     * split into as balanced as possible. That is the reason we want to split if into equal batch sizes. This will
     * make splits evenly balanced
     *
     * @return
     */
    @Override
    public Spliterator<T> trySplit() {
        final HoldingConsumer<T> holdingConsumer = new HoldingConsumer<>();
        if (!tryAdvance(holdingConsumer)) return null;
        final Object[] a = new Object[batchSize];
        int j = 0;
        do a[j] = holdingConsumer.value; while (++j < batchSize && tryAdvance(holdingConsumer));
        if (est != Long.MAX_VALUE) est -= j;
        return spliterator(a, 0, j, characteristics() | SIZED);
    }

    /**
     * From javadocs If this Spliterator's source is SORTED by a Comparator, returns that Comparator.
     * If the source is SORTED in natural order, returns null. Otherwise, if the source is not SORTED, throws IllegalStateException.
     */
    @Override
    public Comparator<? super T> getComparator() {
        if (hasCharacteristics(SORTED)) return null;
        throw new IllegalStateException();
    }

    @Override
    public long estimateSize() {
        return est;
    }

    @Override
    public int characteristics() {
        return characteristics;
    }

    static final class HoldingConsumer<T> implements Consumer<T> {
        Object value;

        @Override
        public void accept(T value) {
            this.value = value;
        }
    }
}
