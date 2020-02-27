package common.server_actions;

import java.util.Map;
import java.util.HashMap;

public enum ServerCommand{
    Cash("CASH"), Loot("LOOT"), Play("PLAY"), Dice("DICE"), Take("TAKE"), Pass("PASS"), Points("PNTS"), Wins("WINS");
    
    private String key;
    
    private static final Map<String, ServerCommand> lookup = new HashMap<String, ServerCommand>();
    
    static{
        for(ServerCommand c: ServerCommand.values()){
            lookup.put(c.toString(), c);
        }
    }
    
    ServerCommand(String key){
        this.key = key;
    }
    
    public static ServerCommand fromString(String key){
        return lookup.get(key);
    }
    
    public static String toString(ServerCommand c){
        return c.key;
    }
    
    public String toString(){
        return this.key;
    }
}
