package common.server_actions;

import common.server_actions.ServerAction;

/**
 * Represents the serer LOOT command.
 */
public class ServerLoot extends ServerAction{
    public final int coins;
    
    public ServerLoot(int coins){
        super(ServerCommand.Loot);
        this.coins = coins;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ServerLoot))return false;
        ServerLoot a = (ServerLoot)o;
        return this.command == a.command && this.coins == a.coins;
    }
    
    public String toString(){
        return super.toString()+" "+coins;
    }
}
