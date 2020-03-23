package server;

/**
 * This class is used as a custom error to throw.
 */
public class InvalidActionException extends Exception{
    
    public InvalidActionException(String message){
        super(message);
    }
}