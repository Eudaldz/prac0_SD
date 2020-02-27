package common.server_actions;

public class ServerTake extends ServerAction{
    public final int id;
    public final byte[] diceIndexList;
    
    public ServerTake(int id, byte[] dices){
        super(ServerCommand.Take);
        this.id = id;
        diceIndexList = dices.clone();
    }
}
