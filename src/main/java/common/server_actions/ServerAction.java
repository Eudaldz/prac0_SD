package common.server_actions;

public abstract class ServerAction{
    public final ServerCommand command;
    
    protected ServerAction(ServerCommand c){
        this.command = c;
    }
}
