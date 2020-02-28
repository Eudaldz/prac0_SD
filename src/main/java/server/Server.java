package server;

import java.net.ServerSocket;
import java.net.Socket;

class Server{
    
    ServerSocket serverSocket;
    public static int MAX_CONNECTIONS = 50;
    
    
    public Server(){
        try{
            serverSocket = new ServerSocket( 80, MAX_CONNECTIONS );
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public static void main(String[] args){
        Server s = new Server();
        s.run();
    }
    
    public void run(){
        while(true){
            try{
                Socket client = serverSocket.accept();
                createNewGame(client);
            }catch(Exception e){
                //TODO
                continue;
            }
        }
    }
    
    private void createNewGame(Socket client){
        
        //Thread nt = new Thread(new ServerEngine());
        //nt.start();
    }
}
