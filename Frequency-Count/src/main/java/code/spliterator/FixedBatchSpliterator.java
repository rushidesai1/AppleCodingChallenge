package code.spliterator;

import code.exceptions.BatchSizeCannotBeNullException;
import code.exceptions.SpliteratorCannotBeNullException;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

/**
 * Created by Rushi Desai on 11/10/2017
 *
 * <p>
 * Just a impl for the base class. This takes in a stream and batchSize and wraps it with the base class.
 *
 * @param <T>
 */
public class FixedBatchSpliterator<T> extends FixedBatchSpliteratorBase<T> {
    private final Spliterator<T> spliterator;

    public FixedBatchSpliterator(Spliterator<T> toWrap, int batchSize) {
        super(toWrap.characteristics(), batchSize, toWrap.estimateSize());
        spliterator = toWrap;
    }

    /**
     * Like a factory method to get instance of this FixedBatchIterator
     *
     * @param toWrap
     * @param batchSize
     * @param <T>
     * @return
     */
    public static <T> FixedBatchSpliterator<T> batchedSpliterator(Spliterator<T> toWrap, int batchSize) {
        if (batchSize == 0) throw new BatchSizeCannotBeNullException("Batch Size Cannot be null");
        if (toWrap == null) throw new SpliteratorCannotBeNullException();
        return new FixedBatchSpliterator<>(toWrap, batchSize);
    }

    /**
     * Takes a stream in and batches it up with the batchsize provided
     *
     * @param in
     * @param batchSize
     * @param <T>
     * @return
     */
    public static <T> Stream<T> withBatchSize(Stream<T> in, int batchSize) {
        if (batchSize == 0) throw new BatchSizeCannotBeNullException("Batch Size Cannot be null");
        if (in == null) throw new SpliteratorCannotBeNullException();
        return stream(batchedSpliterator(in.spliterator(), batchSize), true);
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        return spliterator.tryAdvance(action);
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        spliterator.forEachRemaining(action);
    }
}