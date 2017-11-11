package code.exceptions;

/**
 * Created by Rushi Desai on 11/10/2017
 *
 * <p>
 * This should ideally extend some root exception of our applciation
 * Also whether to extend checked or unchecked exception should be open to discussion
 */
public class SpliteratorCannotBeNullException extends RuntimeException {
    public SpliteratorCannotBeNullException() {
        super("Spliterator Cannot be null");
    }

    public SpliteratorCannotBeNullException(String message) {
        super(message);
    }

    public SpliteratorCannotBeNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpliteratorCannotBeNullException(Throwable cause) {
        super(cause);
    }

    protected SpliteratorCannotBeNullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
