package common.client_actions;

public class ClientBett extends ClientAction{
    public ClientBett(){
        super(ClientCommand.Bett);
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ClientBett))return false;
        ClientBett a = (ClientBett)o;
        return this.command == a.command;
    }
}
