package common.client_actions;

public abstract class ClientAction{
    public final ClientCommand command;
    
    protected ClientAction(ClientCommand c){
        this.command = c;
    }
}