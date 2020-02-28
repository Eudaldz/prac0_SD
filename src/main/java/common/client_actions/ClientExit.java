package common.client_actions;

public class ClientExit extends ClientAction{
    public ClientExit(){
        super(ClientCommand.Exit);
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ClientExit))return false;
        ClientExit a = (ClientExit)o;
        return this.command == a.command;
    }
}
