package common.server_actions;

public class ServerCash extends ServerAction{
    public final int cash;
    
    public ServerCash(int cash){
        super(ServerCommand.Cash);
        this.cash = cash;
    }
}
