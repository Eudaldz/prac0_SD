package common.server_actions;

import common.server_actions.ServerCommand;

public abstract class ServerAction{
    public final ServerCommand command;
    
    protected ServerAction(ServerCommand c){
        this.command = c;
    }
}
