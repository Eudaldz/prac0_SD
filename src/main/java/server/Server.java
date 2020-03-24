package server;

import common.CommunicationInterface;
import common.EloisProtocolComms;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class represents the Server. Establishes communication and runs the ServerEngine.
 */
class Server{
    public static int MAX_CONNECTIONS = 50;
    public static final int VS_SERVER = 1;
    public static final int VS_PLAYER = 2;
    int mode;
    int port;

    /**
     *
     * @param port
     * @param mode
     */
    public Server(int port, int mode){
        this.mode = mode;
        this.port = port;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args){
        if(args.length !=4 ){
            System.out.println("Invalid command format");
            return;
        }

        String p1 = args[0];
        String port_string = args[1];
        String p2 = args[2];
        String mode_string = args[3];

        if( !(p1.equals("-p") && p2.equals("-m")) ){
            System.out.println("Invalid command format");
            return;
        }

        int port = Integer.parseInt(port_string);
        int mode = 0;
        switch(mode_string){
            case "1":
                mode = Server.VS_SERVER;
                break;
            case "2":
                mode = Server.VS_PLAYER;
                break;
            default:
                System.out.println("Invalid command format");
                return;
        }
        Server s = new Server(port, mode);
        s.run();
    }

    /**
     *
     */
    public void run(){
        if(mode == VS_SERVER){
            run1();
        }else{
            run2();
        }
    }
    
    private void run1(){
        System.out.println(port);
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while(true){
                System.out.println("Server is listening on port " + port);
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(5000);
                System.out.println("Accepted connection with: "+socket.getInetAddress());
                try{
                    createNewGame1(socket);
                }catch(Exception e){
                    System.out.println("Could not start game with: "+socket.getInetAddress());
                }
            }

        }catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void run2(){
        System.out.println(port);
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while(true){
                System.out.println("Server is listening on port " + port);
                Socket socket1 = serverSocket.accept();
                Socket socket2 = serverSocket.accept();
                socket1.setSoTimeout(5000);
                socket2.setSoTimeout(5000);
                System.out.println("Accepted connection with: "+socket1.getInetAddress() + " and "+socket2.getInetAddress());
                try{
                    createNewGame2(socket1, socket2);
                }catch(Exception e){
                    System.out.println("Could not start game with: "+socket1.getInetAddress() + " and "+socket2.getInetAddress());
                }
            }

        }catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    /**
     *
     * @param client
     * @throws Exception
     */
    private void createNewGame1(Socket client)throws Exception{
        ServerEngine se =  new ServerEngine(client, client.getInetAddress().toString());
        Thread nt = new Thread(se);
        nt.start();
    }
    
    private void createNewGame2(Socket client1, Socket client2)throws Exception{
        ServerEnginePvP se =  new ServerEnginePvP(client1, client2, client1.getInetAddress().toString(), client1.getInetAddress().toString());
        Thread nt = new Thread(se);
        nt.start();
    }
}
