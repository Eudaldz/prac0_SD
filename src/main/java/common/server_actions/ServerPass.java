package common.server_actions;

public class ServerPass extends ServerAction{
    public final int id;
    
    public ServerPass(int id){
        super(ServerCommand.Pass);
        this.id = id;
    }
}
