package client;

import common.CommunicationInterface;
import common.EloisProtocolComms;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client{
    
    private String hostname;
    private int port;
    private int mode;
    
    private static final int MANUAL_MODE = 0;
    private static final int AUTOMATIC_MODE = 1;
    
    public static final int PORT = 80;
    
    public Client(String hostname, int port, int mode){
        this.hostname = hostname;
        this.port = port;
        this.mode = mode;
        
    }
    
    public void run(){
        System.out.println(hostname);
        System.out.println(port);
        try(Socket socket = new Socket(hostname,port)){
            CommunicationInterface comms = initStream(socket);
            if(comms != null){
                ClientEngine ce = new ClientEngine(comms, new TerminalUI());
                ce.run();
            }else{
                System.out.println("Unable to open communication streams.");
            }
            
        }catch(UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch(IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
    
    private CommunicationInterface initStream(Socket s){
        try{
            return new EloisProtocolComms(s.getInputStream(), s.getOutputStream());
        }catch(Exception e){}
        return null;
    }
    
    
    public static void main(String[] args){
        if(args.length < 4){
            System.out.println("Invalid command format");
            return;
        }
        String p1 = args[0];
        String hostname = args[1];
        String p2 = args[2];
        String port_string = args[3];
        
        if( !(p1.equals("-s") && p2.equals("-p")) ){
            System.out.println("Invalid command format");
            return;
        }
        
        int mode = MANUAL_MODE;
        int port = Integer.parseInt(port_string);
        
        if(args.length == 6){
            String p3 = args[4];
            String mode_s = args[5];
            if(!p3.equals("-i")){
                System.out.println("Invalid command format");
                return;
            }
            switch(mode_s){
                case "0":
                    mode = MANUAL_MODE;
                    break;
                case "1":
                    mode = AUTOMATIC_MODE;
                    break;
                default:
                    System.out.println("Invalid command format");
                    return;
            }
        }
        
        
        Client c = new Client(hostname, port, mode); 
        c.run();
    }
}
