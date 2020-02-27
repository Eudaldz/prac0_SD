package common.server_actions;
<<<<<<< HEAD

import common.server_actions.ServerCommand;
=======
>>>>>>> 5ac96039029b084a8fcc7961142fcc0ac6aed422

public abstract class ServerAction{
    public final ServerCommand command;
    
    protected ServerAction(ServerCommand c){
        this.command = c;
    }
}
