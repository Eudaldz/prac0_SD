package common.client_actions;

public abstract class ClientAction{
    public final ClientCommand command;
    
    protected ClientAction(ClientCommand c){
        this.command = c;
    }
    
    public String toString(){
        return command.toString();
    }
    
    public String protocolPrint(){
        return command.toString();
    }
}
