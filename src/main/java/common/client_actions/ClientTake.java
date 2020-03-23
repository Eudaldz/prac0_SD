package common.client_actions;

import java.util.Arrays;

public class ClientTake extends ClientAction{
    public final int id;
    public final byte[] diceIndexList;
    
    public ClientTake(int id, byte[] dices){
        super(ClientCommand.Take);
        diceIndexList = dices.clone();
        this.id = id;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ClientTake))return false;
        ClientTake a = (ClientTake)o;
        return this.command == a.command && this.id == a.id && Arrays.equals(diceIndexList, a.diceIndexList);
    }
    
    public String toString(){
        return super.toString()+ " "+id + Arrays.toString(diceIndexList);
    }
}
