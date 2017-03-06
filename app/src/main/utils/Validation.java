/**
 * Outcome of the validation of some condition.
 *
 * @param <T> type of the outcome of the validation, usually a plain string will suffice
 */
public class Validation<T> {
    private final ValidationStatus status;
    private final T message;

    /**
     * Creates a new immutable validation instance.
     *
     * @param status the status of this validation
     * @param message message that provides additional information, usually the reason why the validation failed
     */
    public Validation(ValidationStatus status, T message) {
        this.status = status;
        this.message = message;
    }

    /**
     * @return the status of this validation
     */
    public ValidationStatus getStatus() {
        return status;
    }

    /**
     * Returns the message that provides additional information about the outcome of the validation. It can be a simple
     * string message for the user, or an object that the client code might use to apply corrective measure or build the
     * final message.
     *
     * @return the message that provides additional information
     */
    public T getOutcome() {
        return message;
    }
}

/**
 * Status of the validation of a condition.
 */
public enum ValidationStatus {

    /** The validation was fine (e.g., the value was within the limits, the data was consistent). */
    OK,

    /** The validation was fine, but there might be additional information worth examining. */
    HINT,

    /** The validation failed. See the additional information, if any, to understand why. */
    ERROR;
}