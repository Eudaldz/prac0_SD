package common.server_actions;

import common.server_actions.ServerAction;
import common.DiceValue;

public class ServerDice extends ServerAction{
    public final int id;
    public final DiceValue[] diceList;
    
    public ServerDice(int id, DiceValue[] dices){
        super(ServerCommand.Dice);
        this.id = id;
        diceList = dices.clone();
    }
}
