package common;

public enum DiceValue{
    One(1), Two(2), Three(3), Four(4), Five(5), Six(6);
    
    public final int number;
    
    private static final Map<Int, ClientCommand> lookup = new HashMap<String, ClientCommand>();
    
    static{
        for(ClientCommand c: ClientCommand.values()){
            lookup.put(c.number, c);
        }
    }
    
    private DiceValue(int n){
         this.number = n;
    }
    
    public static DiceValue fromInt(int d){
        return lookup.get(d);
    }
}
