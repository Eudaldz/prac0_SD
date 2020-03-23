package common;

/**
 * Custom exception to be thrown when there is a protocol related error.
 */
public class ProtocolException extends Exception{
    
    public ProtocolException(String message){
        super(message);
    }
}
