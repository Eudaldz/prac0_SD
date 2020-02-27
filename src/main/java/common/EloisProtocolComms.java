package common;

import common.*;
import common.client_actions.*;
import common.server_actions.*;


import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;


public class EloisProtocolComms implements CommunicationInterface{
    private InputStream is;
    private OutputStream os;
    
    private static final byte ASCII_SPACE = 32;
    private static final String ERROR_COMMAND = "ERRO"
    
    private static final int MAX_LIST_LEN = 5;
    
    
    public EloisProtocolComms(InputStream is, OutputStream os){
        is = s.getInputStream();
        os = s.getOutputStream();
    }
    
    public void close(){
        is.close();
        os.close();
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
    
    public void sendErrorMessage(ProtocolErrorMessage pem) throws IOException{
        writeWord(ERROR_COMMAND);
        putMessage(pem.getMessage());
    }
    
    
    public ClientAction recieveClientAction() throws IOException, ProtocolException, ProtocolErrorMessage{
        String commandString = readWord();
        if(commandString.equals(ERROR_COMMAND)){
            String message = nextMessage();
            throw new ProtocolErrorMessage(message);
        }
        
        ClientCommand command = ClientCommand.fromString(commandString);
        
        if(command == null){
            throw new ProtocolException("Invalid command");
        }
        
        switch(command){
            case Start:
                int id = nextInt();
                return new ClientStart(id);
            
            case Bett:
                return new ClientBett();
                
            case Take:
                int id = nextInt();
                byte[] diceIndex = nextByteList(5);
                return new ClientTake(id, diceIndex);
                
            case Pass:
                int id = nextInt();
                return new ClientPass(id);
            
            case Exit:
                return new ClientExit();
            
            default:
        }
    }
    
    
    public ServerAction recieveServerAction() throws IOException, ProtocolException, ProtocolErrorMessage{
        String commandString = readWord();
        if(commandString.equals(ERROR_COMMAND)){
            String message = nextMessage();
            throw new ProtocolErrorMessage(message);
        }
        
        ServerCommand command = ServerCommand.fromString(commandString);
        if(command == null){
            throw new ProtocolException("Invalid command");
        }
        
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
                }else{
                    throw new ProtocolException("Invalid paramater value");
                }
                return new ServerPlay(v);
                
            case Dice:
                int id = nextInt();
                DiceValue[] diceList = nextDiceList(5);
                return new ServerDice(id, diceList);
                
            case Take: 
                int id = nextInt();
                byte[] diceIndex = nextByteList(5);
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
                }else{
                    throw new ProtocolException("Invalid paramater value");
                }
                return new ServerWins(v); 
        }
    }
    
    private void nextSpace() throws IOException, ProtocolException{
        byte sp = is.read();
        if(sp != ASCII_SPACE)throw new ProtocolException("Expected separator character");
    }
    
    private void putSpace() throws IOException{
        os.write(ASCII_SPACE);
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
        byte[] buf = new byte[4];
        is.read(buf, 0, 4);
        return bytesToInt(buf);
    }
    
    private void putInt(int c) throws IOException{
        putSpace();
        os.write(intToBytes(c), 0, 4);
    }
    
    private byte[] nextByteList(int max_len) throws IOException, ProtocolException{
        byte b = nextByte();
        int len = b & 0xFF;
        if(len > max_len){
            throw new ProtocolException("List length exceeds maximum allowed");
        }
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
    
    private DiceValue[] nextDiceList(int max_len) throws IOException, ProtocolException{
        byte b = nextByte();
        int len = b & 0xFF;
        if(len > max_len){
            throw new ProtocolException("List length exceeds maximum allowed");
        }
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
        byte[] buf = new byte[4];
        is.read(buf, 0, 4);
        return new String(buf, "UTF-8");
    }
    
    private void writeWord(String w) throws IOException{
        byte[] b = string.getBytes(Charset.forName("UTF-8"));
        os.write(b, 0, 4);
    }
    
    private String nextMessage() throws IOException, ProtocolException{
        skipByte();
        byte[] buf = new byte[2];
        is.read(buf, 0, 2);
        int len = 0;
        try{
            len = Integer.parseInt(new String(buf, "UTF-8"));
        }catch(NumberFormatException e){
            throw new ProtocolException("Invalid Error Message length format");
        }
        
        byte[] message = new byte[len];
        is.read(buf, 0, len);
        return new String(buf, "UTF-8");
    }
    
    private void putMessage(String message) throws IOException{
        putSpace();
        int len = message.length();
        if(len > 99)len = 99;
        byte[] ml = Integer.toString(len).getBytes(Charset.forName("UTF-8"));
        os.write(ml, 0, 2);
        byte[] msg = message.getBytes(Charset.forName("UTF-8"));
        os.write(msg, 0, len);
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
