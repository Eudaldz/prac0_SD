package common.server_actions;

import common.server_actions.ServerAction;
import java.util.Arrays;

/**
 * Represents the server TAKE command.
 */
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
    
    public String toString(){
        return super.toString()+" "+id+ " "+Arrays.toString(diceIndexList);
    }
    
    @Override
    public String protocolPrint(){
        return super.toString()+ " "+id + " "+diceListToString();
    }
    
    private String diceListToString(){
        StringBuilder sb = new StringBuilder();
        sb.append(byteString((byte)diceIndexList.length));
        for(int i = 0; i < diceIndexList.length; i++){
            sb.append(" ");
            sb.append(byteString(diceIndexList[i]));
        }
        return sb.toString();
    }
    
    private String byteString(byte b){
        return "0x"+String.format("%02x", b);
    }
}
