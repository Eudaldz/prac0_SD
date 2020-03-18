package common.client_actions;

public class ClientPass extends ClientAction{
    public final int id;
    
    public ClientPass(int id){
        super(ClientCommand.Pass);
        this.id = id;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ClientPass))return false;
        ClientPass a = (ClientPass)o;
        return this.command == a.command && this.id == a.id;
    }
}
