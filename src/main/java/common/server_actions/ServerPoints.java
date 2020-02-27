package common.server_actions;
<<<<<<< HEAD

import common.server_actions.ServerAction;
=======
>>>>>>> 5ac96039029b084a8fcc7961142fcc0ac6aed422

public class ServerPoints extends ServerAction{
    public final int id;
    public final int points;
    
    public ServerPoints(int id, int points){
        super(ServerCommand.Points);
        this.id = id;
        this.points = points;
    }
}
