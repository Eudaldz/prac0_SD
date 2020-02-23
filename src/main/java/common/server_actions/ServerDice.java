package common.server_action;

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
