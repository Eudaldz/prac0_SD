package common.server_actions;
<<<<<<< HEAD

import common.server_actions.ServerAction;
=======
>>>>>>> 5ac96039029b084a8fcc7961142fcc0ac6aed422

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
