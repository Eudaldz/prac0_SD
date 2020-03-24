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
        return super.toString()+ " "+id + " "+Arrays.toString(diceIndexList);
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
