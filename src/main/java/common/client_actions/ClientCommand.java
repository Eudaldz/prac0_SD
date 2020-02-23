package common.client_actions;

import java.util.Map;
import java.util.HashMap;

public enum ClientCommand{
    Start("STRT"), Bett("BETT"), Take("TAKE"), Pass("PASS"), Exit("EXIT");
    
    private String key;
    
    private static final Map<String, ClientCommand> lookup = new HashMap<String, ClientCommand>();
    
    static{
        for(ClientCommand c: ClientCommand.values()){
            lookup.put(c.toString(), c);
        }
    }
    
    ClientCommand(String key){
        this.key = key;
    }
    
    public static ClientCommand fromString(String key){
        return lookup.get(key);
    }
    
    public static String toString(ClientCommand c){
        return c.key;
    }
    
    public String toString(){
        return this.key;
    }
}
