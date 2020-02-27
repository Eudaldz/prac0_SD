package common.server_actions;

import common.server_actions.ServerAction;

public class ServerPass extends ServerAction{
    public final int id;
    
    public ServerPass(int id){
        super(ServerCommand.Pass);
        this.id = id;
    }
}
