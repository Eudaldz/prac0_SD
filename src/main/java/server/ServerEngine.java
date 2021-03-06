package server;//Aquesta classe és el motor del Server.  Tindrà la maquina d'estats que llegirà i escriurà de ComunicacionInterface.

import common.PlayerGame;
import client.Client;
import client.UserState;
import common.CommunicationInterface;
import common.DiceValue;
import common.EloisProtocolComms;
import common.ProtocolErrorMessage;
import common.ProtocolException;
import common.server_actions.*;
import common.client_actions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerEngine implements Runnable {
    
    private CommunicationInterface ci;
    private PlayerGame clientGame, serverGame;
    private ClientAction ca;
    private String clientAddress;
    private Socket socket;

    private final static int START = 0;
    private final static int LOBBY = 1;
    private final static int PLAY = 2;
    private final static int CLIENT_PLAY = 3;
    private final static int SERVER_PLAY = 4;
    private final static int GAME_END = 5;
    private int CLIENT_ID = -1;
    private int SERVER_ID = 0;

    private final static boolean SERVER_TURN = true;
    private final static boolean CLIENT_TURN = false;

    private Random rand = new Random();

    private FileWriter logger;

    public ServerEngine(Socket socket, String clientAddress)throws IOException{
        this.socket = socket;
        this.ci = new EloisProtocolComms(socket.getInputStream(), socket.getOutputStream());
        this.ci = ci;
        this.clientGame = new PlayerGame();//CLIENT
        this.serverGame = new PlayerGame();//CLIENT OR SERVER
        this.clientAddress = clientAddress;
    }
    
    @Override
    public void run(){
        try{
            System.out.println("Game started with "+clientAddress);
            loggerConfig();
            run_game();
            System.out.println("Game ended with "+clientAddress);
            close_comms();
            System.out.println("Connecion ended with "+clientAddress);
            
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Game aborted with "+clientAddress);
            close_comms();
        }
    }
    
    private void close_comms(){
        try{
            ci.close();
            logger.close();
            if(CLIENT_ID != -1){
                Server.connectedPlayers.remove(CLIENT_ID);
            }
        }catch(IOException e){}
    }
 
    private void run_game() {
        boolean END = false;
        int sessionState = START;
        String error_msg;
        boolean first_turn=false;
        int last_loser = 0; //0 -> tie | 1 ->client | 2 ->server
        int gameLoot = 0;
        
        main_loop: while (!END){
            switch(sessionState){
                case START:{
                    ca = receiveAction();
                    if (ca == null){END = true; break main_loop;}
                    if(ca.command == ClientCommand.Exit){
                        END = true;
                        break main_loop;
                    }
                    if (ca.command != ClientCommand.Start) {
                        sendErrorMessage("Expected STRT command");
                        continue main_loop;
                    }
                    CLIENT_ID = ((ClientStart)ca).id;
                    if(Server.connectedPlayers.contains(CLIENT_ID)){
                        sendErrorMessage("ID is being used by another player");
                        continue main_loop;
                    }
                    
                    Server.connectedPlayers.add(CLIENT_ID);
                    
                    if(Server.coinDatabase.containsKey(CLIENT_ID)){
                        clientGame.setGems(Server.coinDatabase.get(CLIENT_ID));
                    }else{
                        Server.coinDatabase.put(CLIENT_ID, clientGame.getGems());
                    }
                    
                    if(CLIENT_ID == 0)SERVER_ID = 1;
                    
                    if (!sendAction(new ServerCash(clientGame.getGems()))) {
                        END = true;
                        break main_loop;
                    }
                    sessionState = LOBBY;
                    break;
                }
                case LOBBY:{
                    ca = receiveAction();
                    if (ca == null){END = true; break main_loop;}
                    if (ca.command.equals(ClientCommand.Bett)){
                        clientGame.newGame();
                        serverGame.newGame();
                        if(!clientGame.hasGems()){
                            sendErrorMessage("Not enough gems");
                            continue main_loop;
                        }
                        gameLoot += 2;
                        clientGame.addGems(-1);
                        Server.coinDatabase.put(CLIENT_ID, clientGame.getGems());
                        if (!sendAction(new ServerLoot(gameLoot))) {
                            END = true;
                            break main_loop;
                        }
                        sessionState = PLAY;
                    }
                    else if (ca.command.equals(ClientCommand.Exit)) {
                        END = true;
                        break main_loop;
                    }
                    else{
                        sendErrorMessage("Expected BETT or EXIT command");
                        continue main_loop;
                    }
                    break;
                }
                case PLAY:{
                    first_turn = rand.nextBoolean();
                    if(last_loser == 1)first_turn = false;
                    else if(last_loser == 2)first_turn = true;
                    if (first_turn){//Case server first
                        if(!sendAction(new ServerPlay((byte) 1))) {END=true;break main_loop;}
                        sessionState=SERVER_PLAY;
                    }else{//Case player first
                        if(!sendAction(new ServerPlay((byte) 0))) {END=true;break main_loop;}
                        sessionState=CLIENT_PLAY;
                    }
                    break;
                }
                case SERVER_PLAY:{

                    serverGame.reroll();
                    if(!sendAction(new ServerDice(SERVER_ID,serverGame.getDiceValues()))) {END=true;break main_loop;}
                    play_loop: while(serverGame.newTurnAvailable()){
                        if(serverGame.shouldPlayerPass()){
                            if(!sendAction(new ServerPass(SERVER_ID))){END=true;break main_loop;}
                            break play_loop;
                        }else{
                            byte[] take = serverGame.takePlayerAuto();
                            serverGame.take(take);
                            if (!sendAction(new ServerTake(SERVER_ID, increaseIndeces(take)))){END=true; break main_loop;}
                            serverGame.reroll();
                            if(!sendAction(new ServerDice(SERVER_ID,serverGame.getDiceValues()))) {END=true;break main_loop;}
                        }
                    }
                    int points = serverGame.getPoints();
                    if(!sendAction(new ServerPoints(SERVER_ID,points))){END=true;break main_loop;}
                    if(first_turn == SERVER_TURN){
                        sessionState = CLIENT_PLAY;
                    }else{
                        sessionState = GAME_END;
                    }
                    break;
                }
                case CLIENT_PLAY:{
                    clientGame.reroll();
                    if(!sendAction(new ServerDice(CLIENT_ID,clientGame.getDiceValues()))) {END=true;break main_loop;}
                    play_loop: while(clientGame.newTurnAvailable()){
                        ca = receiveAction();
                        if(ca == null){END = true; break main_loop;}
                        if(ca.command == ClientCommand.Pass){
                            break play_loop;
                        }else if(ca.command == ClientCommand.Take){
                            byte[] take = decreaseIndeces(((ClientTake)ca).diceIndexList);
                            if(!clientGame.legalTake(take)){
                                sendErrorMessage("Invalid Take indices");
                                continue play_loop;
                            }
                            clientGame.take(take);
                            clientGame.reroll();
                            if(!sendAction(new ServerDice(CLIENT_ID,clientGame.getDiceValues()))) {END=true;break main_loop;}
                        }else if(ca.command == ClientCommand.Exit){
                            END = true;
                            break main_loop;
                        }else{
                            sendErrorMessage("Expected PASS or TAKE command");
                            continue main_loop;
                        }
                    }
                    int points = clientGame.getPoints();
                    if(!sendAction(new ServerPoints(CLIENT_ID,points))){END=true;break main_loop;}
                    if(first_turn == CLIENT_TURN){
                        sessionState = SERVER_PLAY;
                    }else{
                        sessionState = GAME_END;
                    }
                    break;
                }
                case GAME_END: {
                    int winner=2;
                    if(clientGame.getPoints()>serverGame.getPoints()){
                        winner=0;
                        clientGame.addGems(gameLoot);
                        Server.coinDatabase.put(CLIENT_ID, clientGame.getGems());
                        gameLoot = 0;
                    }
                    else if (clientGame.getPoints()<serverGame.getPoints()){
                        winner=1;
                        gameLoot = 0;
                    }
                    
                    if(!sendAction(new ServerWins((byte) winner))){END=true;break main_loop;}
                    if(!sendAction(new ServerCash(clientGame.getGems()))){END=true;break main_loop;}

                    sessionState = LOBBY;
                    break;
                }
            }
        }
    }
    
    private byte[] decreaseIndeces(byte[] idx){
        byte[] result = idx.clone();
        for(int i = 0; i < idx.length; i++){
            result[i]--;
        }
        return result;
    }
    
    private byte[] increaseIndeces(byte[] idx){
        byte[] result = idx.clone();
        for(int i = 0; i < idx.length; i++){
            result[i]++;
        }
        return result;
    }
    
    private void sendErrorMessage(String msg){
        try{
            ci.sendErrorMessage(new ProtocolErrorMessage(msg));
            logger.write("S: ERRO "+msg+"\n");
            logger.flush();
        }catch(IOException e2){}
    }

    private ClientAction receiveAction(){
        try {
            while(true){    
                try{
                    ClientAction ca = ci.recieveClientAction();
                    logger.write("C: "+ca.protocolPrint()+"\n");
                    logger.flush();
                    return ca;
                }catch(SocketTimeoutException e){
                    if(!socket.getInetAddress().isReachable(1000))return null;
                } 
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            sendErrorMessage(e.getMessage());
        } catch (ProtocolErrorMessage e) {
            //sendErrorMessage(e.getMessage());
        }
        return null;
    }

    private boolean sendAction(ServerAction sa){
        try {
            ci.sendServerAction(sa);
            logger.write("S: "+sa.protocolPrint()+"\n");
            logger.flush();
            //System.out.println(sa + " to "+clientAddress);
            return true;
        } catch (IOException e) {
            sendErrorMessage("Communication failed, could not send action");
            return false;
        }
    }

    private boolean loggerConfig(){
        try{
            logger = new FileWriter("Server"+Thread.currentThread().getName()+".log", false);
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

}