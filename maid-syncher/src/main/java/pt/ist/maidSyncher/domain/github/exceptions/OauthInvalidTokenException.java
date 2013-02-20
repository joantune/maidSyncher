/**
 * 
 */
package pt.ist.maidSyncher.domain.github.exceptions;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 12 de Fev de 2013
 *
 * 
 */
public class OauthInvalidTokenException extends Exception {

    /**
     * 
     */
    public OauthInvalidTokenException() {
        super();
    }

    /**
     * @param message
     */
    public OauthInvalidTokenException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public OauthInvalidTokenException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public OauthInvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public OauthInvalidTokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
