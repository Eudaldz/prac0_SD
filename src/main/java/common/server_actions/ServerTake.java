package common.server_action;

public class ServerTake extends ServerAction{
    public final byte[] diceIndexList;
    
    public ServerTake(byte[] dices){
        super(ServerCommand.Take);
        diceIndexList = dices.clone();
    }
}
