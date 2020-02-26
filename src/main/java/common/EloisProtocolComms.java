package comms;

import comms.datagram.client.*;
import java.io.OutputStream;

public class EloisProtocolComms{
    private Socket s;
    private InputStream is;
    private OutputStream os;
    
    byte asciiSpace = 32;
    
    
    public EloisProtocolComms(Socket s){
        is = s.getInputStream();
        os = s.getOutputStream();
    }
    
    public void sendClientAction(ClientAction ca){
        ClientCommand command = ca.command;
        String word = command.key;
        writeWord(word);
        switch(command){
            case Start:
                putInt(d.id);
                return;
            
            case Bett:
                return;
                
            case Take:
                putInt(d.id);
                putByte((byte)d.diceIndex.length);
                putByteList(d.diceIndexList);
                return;
                
            case Pass:
                putInt(d.id);
                return new ClientPass(id);
            
            case Exit:
                return;
        }
    }
    
    public void sendServerAction(ServerAction sa){
        ClientCommand command = sa.command;
        String word = command.key;
        writeWord(word);
        switch(command){
            case Cash:
                ServerCash a = (ServerCash)sa;
                putInt(d.cash);
                return;
                
            case Loot:
                ServerLoot a = (ServerLoot)sa;
                putInt(a.coins);
                return;
                
            case Play:
                ServerPlay a = (ServerPlay)sa;
                if(a.value == ServerPlay.CLIENT){
                    putByte((byte)'0');
                }else{
                    putByte((byte)'1');
                }
                return;
                
            case Dice:
                ServerDice a = (ServerDice)sa;
                putInt(a.id);
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
    
    
    

    
    
    
    
    private void putSpace(){
        os.write(asciiSpace);
    }
    
    private void putByte(byte b){
        putSpace();
        os.write(b);
    }
    
    private void putInt(int c){
        putSpace();
        os.write(intToBytes(c), 0, 4);
    }
    
    private void putByteList(byte[] l){
        for(int i = 0; i < l.length; l++){
            putByte(l[i]);
        }
    }
    
    private void putIntList(int[] l){
        for(int i = 0; i < l.length; l++){
            putInt(l[i]);
        }
    }
    
    private void writeWord(String w){
        byte[] b = string.getBytes(Charset.forName("UTF-8"));
        os.write(b, 0, 4);
    }
    
    private static byte[] intToBytes(int c){
        byte[] bytes = new byte[4];
        bytes[0] = (byte)((number >> 24) & 0xFF);
        bytes[1] = (byte)((number >> 16) & 0xFF);
        bytes[2] = (byte)((number >> 8) & 0xFF);
        bytes[3] = (byte)(number & 0xFF);
        return bytes;
    }
}
