package common.server_actions;

/**
 * Represents the server PNTS command.
 */
public class ServerPoints extends ServerAction{
    public final int id;
    public final int points;
    
    public ServerPoints(int id, int points){
        super(ServerCommand.Points);
        this.id = id;
        this.points = points;
    }
    
    @Override
    public boolean equals(Object o){
        if(o == null)return false;
        if(!(o instanceof ServerPoints))return false;
        ServerPoints a = (ServerPoints)o;
        return this.command == a.command && this.id == a.id && this.points == a.points;
    }
    
    public String toString(){
        return super.toString()+" "+id+" "+points;
    }
    
    @Override
    public String protocolPrint(){
        return super.toString()+" "+id+" "+ byteString((byte)points);
    }
    
    private String byteString(byte b){
        return "0x"+String.format("%02x", b);
    }
}
