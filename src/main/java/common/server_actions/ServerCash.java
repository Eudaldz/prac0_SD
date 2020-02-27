package common.server_actions;
<<<<<<< HEAD

import common.server_actions.ServerAction;
=======
>>>>>>> 5ac96039029b084a8fcc7961142fcc0ac6aed422

public class ServerCash extends ServerAction{
    public final int cash;
    
    public ServerCash(int cash){
        super(ServerCommand.Cash);
        this.cash = cash;
    }
}
