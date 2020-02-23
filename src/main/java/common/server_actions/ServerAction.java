package common.server_action;

public abstract class ServerAction{
    public final ServerCommand command;
    
    protected ServerAction(ServerCommand c){
        this.command = c;
    }
}
