package server;

import common.CommunicationInterface;
import common.EloisProtocolComms;

import javax.swing.plaf.ComponentUI;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class represents the Server. Establishes communication and runs the ServerEngine.
 */
class ServerPVP{
    public static int MAX_CONNECTIONS = 50;
    int mode;
    int port1;
    int port2;

    /**
     *
     * @param port1
     * @param mode
     */
    public ServerPVP(int port1, int mode, int port2){
        this.mode = mode;
        this.port1 = port1;
        this.port2 = port2;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args){
        if(args.length !=6 ){
            System.out.println("Invalid command formatt");
            return;
        }

        String p1 = args[0];
        String port_string1 = args[1];
        String p2 = args[2];
        String port_string2 = args[3];
        String p3 = args[4];
        String mode_string = args[5];

        if( !(p1.equals("-p") && p2.equals("-p")&& p3.equals("-m")) ){
            System.out.println("Invalid command formattt");
            return;
        }

        int port1 = Integer.parseInt(port_string1);
        int port2 = Integer.parseInt(port_string2);
        int mode = 0;
        switch(mode_string){
            case "1":
                mode = ServerEngine.VS_PLAYER;
                break;
            case "2":
                mode = ServerEngine.VS_SERVER;
                break;
            default:
                System.out.println("Invalid command format");
                return;
        }
        ServerPVP s = new ServerPVP(port1, mode, port2);
        s.run();
    }

    /**
     *
     */
    public void run(){
        System.out.println(port1);
        System.out.println(port2);
        try(ServerSocket serverSocket1 = new ServerSocket(port1)){
            try(ServerSocket serverSocket2 = new ServerSocket(port2)) {
                while (true) {
                    System.out.println("Server is listening on port " + port1);
                    System.out.println("Server is listening on port " + port2);
                    Socket socket1 = serverSocket1.accept();
                    Socket socket2 = serverSocket2.accept();
                    System.out.println("Accepted connection with: " + socket1.getInetAddress());
                    System.out.println("Accepted connection with: " + socket2.getInetAddress());
                    try {
                        createNewGamePvP(socket1, socket2);
                    } catch (Exception e) {
                    }
                }
            }

        }catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param s
     * @return
     * @throws Exception
     */
    private CommunicationInterface initStream(Socket s)throws Exception{
        return new EloisProtocolComms(s.getInputStream(), s.getOutputStream());
    }

    /**
     *
     * @param client
     * @throws Exception
     */
    private void createNewGame(Socket client)throws Exception{
        CommunicationInterface comms = initStream(client);
        if(comms != null){
            System.out.println("Connection stablished with: "+client.getInetAddress());
            ServerEngine se =  new ServerEngine(comms, mode, client.getInetAddress().toString());
            se.run();
            //Thread nt = new Thread(new ServerEngine(comms, mode, client.getInetAddress().toString()));
            //nt.start();
        }
    }

    private void createNewGamePvP(Socket client1, Socket client2) throws Exception{
        CommunicationInterface comms1 = initStream(client1);
        CommunicationInterface comms2 = initStream(client2);
        if(comms1 != null && comms2 != null){
            System.out.println("Connection stablished with: "+client1.getInetAddress());
            ServerEnginePvP se =  new ServerEnginePvP(comms1, comms2, mode, client1.getInetAddress().toString(), client2.getInetAddress().toString());
            se.run();
        }
    }
}


