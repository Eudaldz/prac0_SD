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
    int mode;
    int port1,port2;

    /**
     *
     * @param port1
     * @param mode
     */
    public Server(int mode, int port1){
        this.mode = mode;
        this.port1 = port1;
    }

    /**
     *
     * @param port1
     * @param mode
     * @param port2
     */
    public Server(int mode, int port1, int port2){
        this.mode = mode;
        this.port1 = port1;
        this.port2 = port2;
    }


    /**
     *
     * @param args
     */
    public static void main(String[] args){
        if(args.length<4){
            System.out.println("Invalid command format 1");
            return;
        }

        String p1 = args[0];
        String mode_string = args[1];


        if(!p1.equals("-m")) {
            System.out.println("Invalid command format 2");
            return;
        }
        Server s;
        int mode = 0;
        switch(mode_string){
            case "1":
                if(args.length!=5){
                    System.out.println("Invalid command format 3");
                    return;
                }
                String p2 = args[2];
                String port_string1 = args[3];
                String port_string2 = args[4];

                if(!p2.equals("-p")) {
                    System.out.println("Invalid command format 4");
                    return;
                }
                mode = 1;
                s = new Server(mode,Integer.parseInt(port_string1),Integer.parseInt(port_string2));

                break;
            case "2":
                if(args.length!=4){
                    System.out.println("Invalid command format 5");
                    return;
                }
                String p3 = args[2];
                String port_string3 = args[3];
                if(!p3.equals("-p")) {
                    System.out.println("Invalid command format 6");
                    return;
                }
                mode = 2;
                s = new Server(mode, Integer.parseInt(port_string3));

                break;
            default:
                System.out.println("Invalid command format 7");
                return;
        }
        s.run();
    }

    /**
     *
     */
    public void run(){
        if (mode==2){
            System.out.println(port1);
            try(ServerSocket serverSocket = new ServerSocket(port1)){
                while(true){
                    System.out.println("Server is listening on port " + port1);
                    Socket socket = serverSocket.accept();
                    System.out.println("Accepted connection with: "+socket.getInetAddress());
                    try{
                        createNewGame(socket);
                    }catch(Exception e){}
                }

            }catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }else if (mode == 1){
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
