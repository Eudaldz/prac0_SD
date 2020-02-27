package common.server_actions;

import common.server_actions.ServerAction;

public class ServerPoints extends ServerAction{
    public final int id;
    public final int points;
    
    public ServerPoints(int id, int points){
        super(ServerCommand.Points);
        this.id = id;
        this.points = points;
    }
}
