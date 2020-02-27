package common.server_actions;
<<<<<<< HEAD

import common.server_actions.ServerAction;
=======
>>>>>>> 5ac96039029b084a8fcc7961142fcc0ac6aed422

public class ServerPass extends ServerAction{
    public final int id;
    
    public ServerPass(int id){
        super(ServerCommand.Pass);
        this.id = id;
    }
}
