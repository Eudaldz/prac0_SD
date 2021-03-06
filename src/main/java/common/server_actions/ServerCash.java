package common.server_actions;

import common.server_actions.ServerAction;

/**
 * Represents the server CASH command.
 */
public class ServerCash extends ServerAction{
    public final int cash;
    
    public ServerCash(int cash){
        super(ServerCommand.Cash);
        this.cash = cash;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ServerCash))return false;
        ServerCash a = (ServerCash)o;
        return this.command == a.command && this.cash == a.cash;
    }
    
    public String toString(){
        return super.toString()+" "+cash;
    }
    
    @Override
    public String protocolPrint(){
        return super.toString()+" "+cash;
    }
}
