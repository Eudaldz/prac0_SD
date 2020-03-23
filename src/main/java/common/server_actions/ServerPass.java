package common.server_actions;

import common.server_actions.ServerAction;

public class ServerPass extends ServerAction{
    public final int id;
    
    public ServerPass(int id){
        super(ServerCommand.Pass);
        this.id = id;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ServerPass))return false;
        ServerPass a = (ServerPass)o;
        return this.command == a.command && this.id == a.id;
    }
    
    public String toString(){
        return super.toString()+" "+id;
    }
}
