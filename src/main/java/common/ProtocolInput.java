/*
package comms;

import comms.datagram.client.*;
import java.io.InputStream;

public class ProtocolInput{
    private InputStream is;
    
    private byte[] buf;
    private final int bufSize = 4;
    
    
    public ClientInput(InputStream is){
        this.is = is;
        buf = new byte[4];
    } 

    public ClientDatagram readClientDatagram(){
        String commandString = readWord();
        ClientCommand command = ClientCommand.fromString(commandString);
        
        switch(command){
            case Start:
                int id = nextInt();
                return new ClientStart(id);
            
            case Bett:
                return new ClientBett();
                
            case Take:
                int id = nextInt();
                int len = nextByte();
                byte[] diceIndex = nextByteList(len);
                return new ClientTake(id, diceIndex);
                
            case Pass:
                int id = nextInt();
                return new ClientPass(id);
            
            case Exit:
                return new ClientExit();
            
            default:
        }
        
    }
    
    public ServerDatagram readServerDatagram(){
        String commandString = readWord();
        ServerCommand command = ServerCommand.fromString(commandString);
        
        switch(command){
            case Cash:
                int cash = nextInt();
                return new ServerCash(cash);
                
            case Loot:
                int coins = nextInt();
                return new ServerLoot(coins);
                
            case Play:
                byte c = nextByte();
                return new ServerPlay(c);
                
            case Dice:
                int id = nextInt();
                byte[] diceList = nextByteList();
                return new ServerDice(id, diceList);
                
            case Take: 
                int id = nextInt();
                int len = nextByte();
                byte[] diceIndex = nextByteList(len);
                return new ServerTake(id, diceIndex);
            
            case Pass:
                int id = nextInt();
                return new ServerPass(id);
                
            case Points: 
                int id = nextInt();
                int points = nextInt();
                return new ServerPoints(id, points);
            case Wins:
                byte v = nextByte();
                return new ServerWins(v); 
        }
    }
    
    private void skipByte(){
        is.read();
    }
    
    private byte nextByte(){
        skipByte();
        return is.read();
    }
    
    private int nextInt(){
        skipByte();
        is.read(buf, 0, 4);
        return bytesToInt(buf);
    }
    
    private int[] nextIntList(int len){
        int[] l = new int[len];
        for(int i = 0; i < len; i++){
            l[i] = nextInt();
        }
        return l;
    }
    
    private byte[] nextByteList(int len){
        byte[] l = new byte[len];
        for(int i = 0; i < len; i++){
            l[i] = nextByte();
        }
        return l;
    }
    
    private String readWord(){
        is.read(buf, 0, 4);
        return new String(buf, "UTF-8");
    }
    
    private static int bytesToInt(byte[] bytes) {
        int number;
        number=((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        return number;
    }
}*/
