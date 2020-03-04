package client;

import common.CommunicationInterface;
import common.EloisProtocolComms;
import common.client_actions.ClientAction;
import java.net.Socket;


public class ClientEngine{
    
    private CommunicationInterface ci;
    private Socket s;
    
    public ClientEngine(Socket s){
        this.s = s;
    }
    
    public void run(){
        if(!initStream()){
            System.out.println("Fatal error while opening the communication streams");
            return;
        }
    }
    
    public boolean initStream(){
        try{
            ci = new EloisProtocolComms(s.getInputStream(), s.getOutputStream());
            return true;
        }catch(Exception e){}
        return false;
    }
    
}
