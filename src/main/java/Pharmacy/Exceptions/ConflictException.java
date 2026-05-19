package Pharmacy.Exceptions;

/**
 * Class ConflictException.
 * Provides functionality and data modeling for ConflictException.
 */
public class ConflictException extends RuntimeException {
    /**
     * Conflict exception.
     *
     * @param message the message
     */
    public ConflictException(String message) {
        super(message);
    }
}
