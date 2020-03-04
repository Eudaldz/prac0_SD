package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client{
    
    private String ip;
    private int port;
    private int mode;
    
    private static final int MANUAL_MODE = 0;
    private static final int AUTOMATIC_MODE = 1;
    
    public static final int PORT = 80;
    
    public Client(String ip, int port, int mode){
        this.ip = ip;
        this.port = port;
        this.mode = mode;
        
    }
    
    public void run(){
        Socket s = null;
        try{
            InetAddress serverAddr = InetAddress.getByName(ip);
            s = new Socket(serverAddr, PORT);
        }catch(UnknownHostException e){
            System.out.println("Unknown ip address.");
            return;
        }catch(IOException e){
            System.out.println("Unable to stablish connection.");
            return;
        }catch(SecurityException e){
            System.out.println("Permission denied.");
            return;
        }catch(Exception e){
            System.out.println("Fatal Error: "+e.getMessage());
            return;
        }
        
        //Socket succesfully opened
        ClientEngine ce = new ClientEngine(s);
        ce.run();
        
    }
    
    public static void main(String[] args){
        if(args.length < 4){
            //TODO print error
            return;
        }
        String p1 = args[0];
        String ip = args[1];
        String p2 = args[2];
        String port_string = args[3];
        
        if( !(p1.equals("-s") && p2.equals("-p")) ){
            //TODO print error
            return;
        }
        
        int mode = MANUAL_MODE;
        int port = Integer.parseInt(port_string);
        
        if(args.length == 6){
            String p3 = args[4];
            String mode_s = args[5];
            if(!p3.equals("-i")){
                //TODO print error
            }
            switch(mode_s){
                case "0":
                    mode = MANUAL_MODE;
                    break;
                case "1":
                    mode = AUTOMATIC_MODE;
                    break;
                default:
                    //TODO print error
                    break;
            }
        }
        
        
        Client c = new Client(ip, port, mode);
        c.run();
    }
}
