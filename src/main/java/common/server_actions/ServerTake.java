package common.server_actions;
<<<<<<< HEAD

import common.server_actions.ServerAction;
=======
>>>>>>> 5ac96039029b084a8fcc7961142fcc0ac6aed422

public class ServerTake extends ServerAction{
    public final int id;
    public final byte[] diceIndexList;
    
    public ServerTake(int id, byte[] dices){
        super(ServerCommand.Take);
        this.id = id;
        diceIndexList = dices.clone();
    }
}
