package common.client_actions;

public class ClientPass extends ClientAction{
    public final int id;
    
    public ClientPass(int id){
        super(ClientCommand.Pass);
        this.id = id;
    }
}
