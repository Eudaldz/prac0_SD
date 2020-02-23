package common.client_actions;

public class ClientStart extends ClientAction{
    public final int id;
    
    public ClientStart(int id){
        super(ClientCommand.Start);
        this.id = id;
    }
}
