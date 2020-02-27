package common.server_actions;

import common.server_actions.ServerAction;

public class ServerWins extends ServerAction{
    public final byte value;
    
    public static final byte CLIENT = 0;
    public static final byte SERVER = 1;
    public static final byte TIE = 2;
    
    public ServerWins(byte value){
        super(ServerCommand.Wins);
        this.value = value;
    }
}
