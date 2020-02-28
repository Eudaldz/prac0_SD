package common.server_actions;

import common.server_actions.ServerAction;
import java.util.Arrays;

public class ServerTake extends ServerAction{
    public final int id;
    public final byte[] diceIndexList;
    
    public ServerTake(int id, byte[] dices){
        super(ServerCommand.Take);
        this.id = id;
        diceIndexList = dices.clone();
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ServerTake))return false;
        ServerTake a = (ServerTake)o;
        return this.command == a.command && this.id == a.id && Arrays.equals(diceIndexList, a.diceIndexList);
    }
}
