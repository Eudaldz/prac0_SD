package common.client_actions;

public class ClientStart extends ClientAction{
    public final int id;
    
    public ClientStart(int id){
        super(ClientCommand.Start);
        this.id = id;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof ClientStart))return false;
        ClientStart a = (ClientStart)o;
        return this.command == a.command && this.id == a.id;
    }
    
    public String toString(){
        return super.toString()+" "+id;
    }
}
