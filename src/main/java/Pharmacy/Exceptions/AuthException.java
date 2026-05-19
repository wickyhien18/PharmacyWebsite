package Pharmacy.Exceptions;

/**
 * Class AuthException.
 * Provides functionality and data modeling for AuthException.
 */
public class AuthException extends RuntimeException{
    /**
     * Auth exception.
     *
     * @param msg the msg
     */
    public AuthException(String msg) {
        super(msg);
    }
}
