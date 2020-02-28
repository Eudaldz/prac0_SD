package common;

public class ProtocolErrorMessage extends Exception{
    
    public ProtocolErrorMessage(String message){
        super(message);
    }
    
    @Override
    public boolean equals(Object o){
        if(o == null)return false;
        if(!(o instanceof ProtocolErrorMessage))return false;
        ProtocolErrorMessage a = (ProtocolErrorMessage)o;
        return getMessage().equals(a.getMessage());
    }
}
