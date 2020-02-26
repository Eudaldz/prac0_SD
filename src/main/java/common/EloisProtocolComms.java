package comms;

import comms.datagram.client.*;
import java.io.OutputStream;

public class EloisProtocolComms implements CommunicationInterface{
    private Socket s;
    private InputStream is;
    private OutputStream os;
    
    byte asciiSpace = 32;
    
    
    public EloisProtocolComms(Socket s){
        is = s.getInputStream();
        os = s.getOutputStream();
    }
    
    public void sendClientAction(ClientAction ca)throws IOException{
        ClientCommand command = ca.command;
        String word = command.key;
        writeWord(word);
        switch(command){
            case Start:
                ClientStart a = (ClientStart)ca;
                putInt(a.id);
                return;
            
            case Bett:
                return;
                
            case Take:
                ClientTake a = (ClientTake)ca;
                putInt(a.id);
                putByteList(a.diceIndexList);
                return;
                
            case Pass:
                ClientPass a = (ClientPass)ca;
                putInt(a.id);
                return new ClientPass(id);
            
            case Exit:
                return;
        }
    }
    
    public void sendServerAction(ServerAction sa)throws IOException{
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
                putDiceList(a.diceList);
                return;
                
            case Take:
                ServerDice a = (ServerDice)sa;
                putInt(a.id);
                putByteList(a.diceIndexList);
                return;
            
            case Pass:
                ServerPass a = (ServerPass)sa;
                putInt(a.id);
                return;
                
            case Points: 
                ServerPoints a = (ServerPoints)sa;
                putInt(a.id);
                putInt(a.points);
                return;
            case Wins:
                ServerWins a = (ServerWins)sa;
                if(a.value == ServerWins.CLIENT){
                    putByte((byte)'0');
                }else if(a.value == ServerWins.SERVER){
                    putByte((byte)'1');
                }else{
                    putByte((byte)'2');
                }
                return; 
    }
    
    
    public ClientAction recieveClientAction() throws IOException{
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
    
    
    public ServerAction recieveServerAction() throws IOException{
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
                byte v = 0;
                if(c == '0'){
                    v = ServerPlay.CLIENT;
                }else if(c == '1'){
                    v = ServerPlay.SERVER;
                }
                return new ServerPlay(v);
                
            case Dice:
                int id = nextInt();
                DiceValue[] diceList = nextDiceList();
                return new ServerDice(id, diceList);
                
            case Take: 
                int id = nextInt();
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
                byte c = nextByte();
                byte v = 0;
                if(c == '0'){
                    v = ServerWins.CLIENT;
                }else if(c == '1'){
                    v = ServerWins.SERVER;
                }else if(c == '2'){
                    v = ServerWins.TIE;
                }
                return new ServerWins(v); 
        }
    }
    
    private void skipByte() throws IOException{
        is.read();
    }
    
    private void putSpace() throws IOException{
        os.write(asciiSpace);
    }
    
    private byte nextByte() throws IOException{
        skipByte();
        return is.read();
    }
    
    private void putByte(byte b) throws IOException{
        putSpace();
        os.write(b);
    }
    
    private int nextInt() throws IOException{
        skipByte();
        is.read(buf, 0, 4);
        return bytesToInt(buf);
    }
    
    private void putInt(int c) throws IOException{
        putSpace();
        os.write(intToBytes(c), 0, 4);
    }
    
    private byte[] nextByteList() throws IOException{
        byte len = nextByte();
        byte[] l = new byte[len];
        for(int i = 0; i < len; i++){
            l[i] = nextByte();
        }
        return l;
    }
    
    private void putByteList(byte[] l) throws IOException{
        putByte((byte)l.length);
        for(int i = 0; i < l.length; l++){
            putByte(l[i]);
        }
    }
    
    private DiceValue[] nextDiceList() throws IOException{
        byte len = nextByte();
        DiceValue[] diceList = new DiceValue[len];
        for(int i = 0; i < len; i++){
            byte v = nextByte() - (byte)'0';
            diceList[i] = DiceValue.fromInt(v);
            
        }
        return diceList;
    }
    
    private void putDiceList(DiceValue[] l) throws IOException{
        putByte((byte)l.length);
        for(int i = 0; i < l.length; l++){
            byte = (byte)'0' + l[i].number;
            putByte(l[i]);
        }
    }
    
    private String readWord() throws IOException{
        is.read(buf, 0, 4);
        return new String(buf, "UTF-8");
    }
    
    private void writeWord(String w) throws IOException{
        byte[] b = string.getBytes(Charset.forName("UTF-8"));
        os.write(b, 0, 4);
    }
    
    private static int bytesToInt(byte[] bytes) {
        int number;
        number=((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        return number;
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
