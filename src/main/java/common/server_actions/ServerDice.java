package common.server_actions;

import common.server_actions.ServerAction;
import common.DiceValue;
import java.util.Arrays;

/**
 * Represents the server DICE command.
 */
public class ServerDice extends ServerAction{
    public final int id;
    public final DiceValue[] diceList;
    
    public ServerDice(int id, DiceValue[] dices){
        super(ServerCommand.Dice);
        this.id = id;
        diceList = dices.clone();
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ServerDice))return false;
        ServerDice a = (ServerDice)o;
        return this.command == a.command && this.id == a.id && Arrays.equals(diceList, a.diceList);
    }
    
    public String toString(){
        return super.toString()+" "+id+ " "+Arrays.toString(diceList);
    }
    
    @Override
    public String protocolPrint(){
        return super.toString()+" "+id+diceListToString();
    }
    
    private String diceListToString(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < diceList.length; i++){
            sb.append(" ");
            sb.append("\'");
            sb.append(diceList[i].toString());
            sb.append("\'" );
        }
        return sb.toString();
    }
}
