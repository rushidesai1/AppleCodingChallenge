package exceptions;

/**
 * Created by Rushi Desai on 11/9/2017
 *
 * <p>
 * This should ideally extend some root exception of our applciation
 * Also whether to extend checked or unchecked exception should be open to discussion
 */
public class BatchSizeCannotBeNullException extends RuntimeException {
    public BatchSizeCannotBeNullException() {
        super();
    }

    public BatchSizeCannotBeNullException(String message) {
        super(message);
    }

    public BatchSizeCannotBeNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public BatchSizeCannotBeNullException(Throwable cause) {
        super(cause);
    }
}
