package common.server_actions;

import common.server_actions.ServerCommand;

/**
 * Represents a server action, that will always consist of a ServerCommand.
 */
public abstract class ServerAction{
    public final ServerCommand command;
    
    protected ServerAction(ServerCommand c){
        this.command = c;
    }
    
    public String toString(){
        return command.toString();
    }
    
    public String protocolPrint(){
        return command.toString();
    }
}
