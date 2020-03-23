package common.server_actions;

import common.server_actions.ServerAction;

/**
 * Represents the server WINS command.
 */
public class ServerWins extends ServerAction{
    public final byte value;
    
    public static final byte CLIENT = 0;
    public static final byte SERVER = 1;
    public static final byte TIE = 2;
    
    public ServerWins(byte value){
        super(ServerCommand.Wins);
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ServerWins))return false;
        ServerWins a = (ServerWins)o;
        return this.command == a.command && this.value == a.value;
    }
    
    public String toString(){
        return super.toString()+" "+value;
    }
}
