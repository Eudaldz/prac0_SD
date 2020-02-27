package common.server_actions;

import common.server_actions.ServerAction;

public class ServerLoot extends ServerAction{
    public final int coins;
    
    public ServerLoot(int coins){
        super(ServerCommand.Loot);
        this.coins = coins;
    }
}
