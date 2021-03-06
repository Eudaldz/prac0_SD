package common.server_actions;
import common.server_actions.ServerAction;

/**
 * Represents the server PLAY command.
 */
public class ServerPlay extends ServerAction{
    public final byte value;
    
    public static final byte SERVER =  1;
    public static final byte CLIENT = 0;
    
    public ServerPlay(byte value){
        super(ServerCommand.Play);
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ServerPlay))return false;
        ServerPlay a = (ServerPlay)o;
        return this.command == a.command && this.value == a.value;
    }
    
    public String toString(){
        return super.toString()+" "+value;
    }
    
    @Override
    public String protocolPrint(){
        return super.toString()+" \'"+value+"\'";
    }
}
