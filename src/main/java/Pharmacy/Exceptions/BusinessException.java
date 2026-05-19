package Pharmacy.Exceptions;

/**
 * Class BusinessException.
 * Provides functionality and data modeling for BusinessException.
 */
public class BusinessException extends RuntimeException {
    /**
     * Business exception.
     *
     * @param message the message
     */
    public BusinessException(String message) {
        super(message);
    }
}