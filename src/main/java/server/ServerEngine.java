package server;//Aquesta classe és el motor del Server.  Tindrà la maquina d'estats que llegirà i escriurà de ComunicacionInterface.

import common.PlayerGame;
import client.Client;
import client.UserState;
import common.CommunicationInterface;
import common.DiceValue;
import common.ProtocolErrorMessage;
import common.ProtocolException;
import common.server_actions.*;
import common.client_actions.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ServerEngine implements Runnable {
    
    public static final int VS_SERVER = 1;
    public static final int VS_PLAYER = 2;

    private CommunicationInterface ci;
    private PlayerGame clientGame, serverGame;
    private ClientAction ca;
    private String clientAddress;

    private final static int START = 0;
    private final static int PLAY = 2;
    private final static int CLIENT_PLAY = 3;
    private final static int SERVER_PLAY = 4;
    private final static int TURN_END = 6;
    private final static int GAME_END = 5;
    private final static int BETT = 7;

    private final static int SERVER_PLAY_START = 0;
    private final static int SERVER_PLAY_TAKE = 1;
    private final static int SERVER_PLAY_PASS = 2;
    private final static int SERVER_PLAY_END_TURN = 3;

    private final static int CLIENT_TAKE = 0;
    private final static int CLIENT_PASS = 1;
    private final static int CLIENT_PLAY_LOBBY = 2;
    private final static int CLIENT_END = 3;
    
    private final static boolean SERVER_TURN = true;
    private final static boolean CLIENT_TURN = false;


    private Random rand = new Random();


    public ServerEngine(CommunicationInterface ci, int mode, String clientAddress){
        this.ci = ci;
        this.clientGame = new PlayerGame();//CLIENT
        this.serverGame = new PlayerGame();//CLIENT OR SERVER
        this.clientAddress = clientAddress;
    }
    
    @Override
    public void run(){
        run_game();
        close_comms();
    }
    
    private void close_comms(){
        try{
            ci.close();
        }catch(IOException e){}
    }
 
    private void run_game() {
        boolean END = false;
        int sessionState = START;
        int SERVER_ID = 0;
        int CLIENT_ID = 0;
        boolean first_turn=false;
        System.out.println("Begin game");
        int gameLoot = 0;
        
        main_loop: while (!END){
            System.out.println("Begin game1");
            switch(sessionState){
                case START:{
                    ca = receiveAction(); // TODO S'ha de capturar l'ID (per a la segona fase)
                    if (ca == null){END = true; break main_loop;}
                    if (ca.command != ClientCommand.Start) {
                        sendErrorMessage("Expected STRT command");
                        continue main_loop;
                    }
                    CLIENT_ID = ((ClientStart)ca).id;
                    
                    if (!sendAction(new ServerCash(clientGame.getGems()))) {
                        END = true;
                        break main_loop;
                    }
                    sessionState = BETT;
                    break;
                }
                case BETT:{
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
                            if (!sendAction(new ServerTake(SERVER_ID, take))){END=true; break main_loop;}
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
                            byte[] take = ((ClientTake)ca).diceIndexList;
                            clientGame.take(take);
                            clientGame.reroll();
                            if(!sendAction(new ServerDice(CLIENT_ID,clientGame.getDiceValues()))) {END=true;break main_loop;}
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
                        winner=1;
                        clientGame.addGems(gameLoot);
                    }
                    else if (clientGame.getPoints()<serverGame.getPoints())winner=0;
                    
                    if(!sendAction(new ServerWins((byte) winner))){END=true;break main_loop;}
                    if(!sendAction(new ServerCash(clientGame.getGems()))){END=true;break main_loop;}

                    sessionState = BETT;
                    break;
                }
            }
        }
    }
    private void sendErrorMessage(String msg){
        try{
            ci.sendErrorMessage(new ProtocolErrorMessage(msg));
        }catch(IOException e2){}
    }

    private ClientAction receiveAction(){
        System.out.println("Waiting action from client...");
        try {
            ClientAction ca = ci.recieveClientAction();
            System.out.println(ca + " from "+clientAddress);
            return ca;
        } catch (IOException e) {
            System.out.println("Communication failed");
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
            System.out.println(sa + " to "+clientAddress);
            return true;
        } catch (IOException e) {
            sendErrorMessage("Communication failed, could not send action");
            return false;
        }
    }
}