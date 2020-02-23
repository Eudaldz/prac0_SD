package common.client_actions;

public class ClientTake extends ClientAction{
    public final int id;
    public final byte[] diceIndexList;
    
    public ClientTake(int id, byte[] dices){
        super(ClientCommand.Take);
        diceIndexList = dices.clone();
        this.id = id;
    }
}
