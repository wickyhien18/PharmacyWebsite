package Pharmacy.Exceptions;

/**
 * Class ResourceNotFoundException.
 * Provides functionality and data modeling for ResourceNotFoundException.
 */
public class ResourceNotFoundException extends RuntimeException{
    /**
     * Resource not found exception.
     *
     * @param message the message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Long id) {
        return new ResourceNotFoundException(resource + "not found with id:" + id);
    }

    public static ResourceNotFoundException of(String resource, String field, Object value) {
        return new ResourceNotFoundException(
                resource + "not found with" + field + ": " + value);
    }
}
