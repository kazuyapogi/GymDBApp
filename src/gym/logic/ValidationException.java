package gym.logic;

/** Thrown by the logic tier when an input or a business rule is not satisfied. */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    /** Throws with the message unless the condition holds. */
    static void check(boolean condition, String message) throws ValidationException {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    /** Throws if a required text field is empty. */
    static void required(String value, String label) throws ValidationException {
        check(value != null && !value.trim().isEmpty(), label + " is required.");
    }
}
