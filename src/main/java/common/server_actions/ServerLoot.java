package common.server_actions;
<<<<<<< HEAD

import common.server_actions.ServerAction;
=======
>>>>>>> 5ac96039029b084a8fcc7961142fcc0ac6aed422

public class ServerLoot extends ServerAction{
    public final int coins;
    
    public ServerLoot(int coins){
        super(ServerCommand.Loot);
        this.coins = coins;
    }
}
