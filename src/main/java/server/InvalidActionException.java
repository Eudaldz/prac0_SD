package server;

public class InvalidActionException extends Exception{
    
    public InvalidActionException(String message){
        super(message);
    }
}