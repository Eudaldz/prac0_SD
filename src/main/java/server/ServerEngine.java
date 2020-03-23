package server;//Aquesta classe és el motor del Server.  Tindrà la maquina d'estats que llegirà i escriurà de ComunicacionInterface.
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

    private boolean SERVER_ROLE=false;//false --> player vs server || true --> player vs player

    private CommunicationInterface ci;
    private PlayerGame player1game, player2game;
    private ClientAction ca;

    private final static int START = 0;
    private final static int LOBBY = 1;
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


    private Random rand = new Random();


    public ServerEngine(CommunicationInterface ci){
        this.ci = ci;
        this.player1game = new PlayerGame();//CLIENT
        this.player2game = new PlayerGame();//CLIENT OR SERVER
    }
 
    public void run() {
        boolean END = false;
        int sesssionState = START;
        int SERVER_ID = 0;
        boolean first_turn=false;
        if (SERVER_ROLE){//Case the server acts as a player.

            while (!END){
                switch(sesssionState){
                    case START:{
                        ca = receiveAction(); // TODO S'ha de capturar l'ID (per a la segona fase)
                        if (ca == null) END = true;
                        if (ca.command != ClientCommand.Start) {
                            sendErrorMessage("Expected STRT command");
                            END = true;
                            break;
                        }
                        if (!sendAction(new ServerCash(player1game.getGems()))) {
                            END = true;
                            break;
                        }
                        sesssionState = BETT;
                        break;
                    }
                    case LOBBY:{
                        ca = receiveAction();
                        if (ca == null) END = true;
                        if (ca.command.equals(ClientCommand.Bett)){
                            player2game.newGame();
                            player1game.newGame();
                            if (!sendAction(new ServerLoot(player1game.getGems()))) {
                                END = true;
                                break;
                            }
                            sesssionState = PLAY;
                        }
                        else if (ca.command.equals(ClientCommand.Exit)) {
                            END = true;
                            break;
                        }
                        else{
                            sendErrorMessage("Expected BETT or EXIT command");
                        }
                        break;
                    }
                    case PLAY:{
                        first_turn = rand.nextBoolean();
                        if (first_turn){//Case server first
                            if(!sendAction(new ServerPlay((byte) 1))) {END=true;break;}
                            sesssionState=SERVER_PLAY;
                        }else{//Case player first
                            if(!sendAction(new ServerPlay((byte) 0))) {END=true;break;}
                            sesssionState=CLIENT_PLAY;
                        }
                        break;
                    }
                    case SERVER_PLAY:{
                        if(!sendAction(new ServerDice(SERVER_ID,player2game.getDiceValues()))) {END=true;break;}
                        int server_play_state=SERVER_PLAY_START;
                        boolean END_PLAY=false;
                        while(!END_PLAY) {
                            switch (server_play_state) {
                                case SERVER_PLAY_START:{
                                    if (player2game.newTurnAvailable()) server_play_state = SERVER_PLAY_TAKE;
                                    else server_play_state = SERVER_PLAY_END_TURN;
                                    break;
                                }
                                case SERVER_PLAY_TAKE:{
                                    if(player2game.shouldTakeServerAI()) {
                                        if (!sendAction(new ServerTake(SERVER_ID, player2game.takeServerAI()))) {
                                            END = true;
                                            END_PLAY = true;
                                            break;
                                        }
                                        if(player2game.newTurnAvailable())server_play_state=SERVER_PLAY_PASS;
                                        else server_play_state=SERVER_PLAY_END_TURN;
                                    }
                                    try {
                                        player2game.roll();
                                        if(!sendAction(new ServerDice(SERVER_ID, player2game.getDiceValues()))){END=true;END_PLAY=true;break;}
                                    } catch (InvalidActionException e) {
                                        END_PLAY=true;
                                        END=true;
                                        sendErrorMessage(e.getMessage());
                                    }
                                    if(player2game.newTurnAvailable()) server_play_state=SERVER_PLAY_TAKE;
                                    else server_play_state=SERVER_PLAY_END_TURN;
                                    break;
                                }

                                case SERVER_PLAY_PASS:{
                                    if(!sendAction(new ServerPass(SERVER_ID))){END=true;END_PLAY=true;break;}
                                    server_play_state=SERVER_PLAY_END_TURN;
                                    break;
                                }
                                case SERVER_PLAY_END_TURN: {
                                    if(!sendAction(new ServerPoints(SERVER_ID,player2game.getPoints()))){END=true;END_PLAY=true;break;}
                                    END_PLAY=true;
                                    break;
                                }
                            }
                            if(first_turn){
                                sesssionState=CLIENT_PLAY;
                            }else{
                                sesssionState=GAME_END;
                            }
                        }
                        break;
                    }
                    case CLIENT_PLAY:{
                        if(!sendAction(new ServerDice(SERVER_ID,player1game.getDiceValues()))) {END=true;break;}
                        boolean END_PLAY=false;
                        int client_play_state=0;
                        while(!END_PLAY){
                            switch(client_play_state){
                                case CLIENT_PLAY_LOBBY:{
                                    ca = receiveAction();
                                    if (ca.equals(ClientCommand.Take) && player1game.newTurnAvailable()){
                                        client_play_state=CLIENT_TAKE;
                                    }else if (ca.equals(ClientCommand.Pass)){
                                        client_play_state=CLIENT_PASS;
                                    }else if (!player1game.newTurnAvailable()) {
                                        client_play_state=CLIENT_END;
                                    }else{
                                        sendErrorMessage("Not the message expected");
                                    }
                                    break;
                                }
                                case CLIENT_TAKE:{
                                    try {
                                        player1game.reserve(((ClientTake)ca).diceIndexList);
                                    } catch (InvalidActionException e) {
                                        sendErrorMessage(e.getMessage());
                                    }
                                    try {
                                        player1game.roll();
                                    } catch (InvalidActionException e) {
                                        e.printStackTrace();
                                    }
                                    if(!sendAction(new ServerDice(0, player1game.getDiceValues()))){END=true;END_PLAY=true;break;}
                                    client_play_state=CLIENT_PLAY_LOBBY;

                                }
                                case CLIENT_PASS:{
                                    if(!player1game.canPass()){
                                        sendErrorMessage("Invalid action, cannot PASS");
                                        END_PLAY=true;
                                        END=true;
                                    }
                                    client_play_state=CLIENT_END;
                                }
                                case CLIENT_END:{
                                    if(!sendAction(new ServerPoints(1,player1game.getPoints()))){END=true;END_PLAY=true;break;}
                                    END_PLAY=true;
                                    break;
                                }
                            }
                        }
                        ca = receiveAction();
                        
                        break;
                    }
                    case GAME_END: {
                        int winner=2;
                        if(player2game.getPoints()>player1game.getPoints())winner=1;
                        else if (player2game.getPoints()<player1game.getPoints())winner=0;
                        if(!sendAction(new ServerWins((byte) winner))){END=true;break;}
                        if(!sendAction(new ServerCash(player1game.getGems()))){END=true;break;}

                        sesssionState = LOBBY;
                    }
                }
            }



        }else{//Case the server only acts as the middle man between two players.

        }

        
    }
    private void sendErrorMessage(String msg){
        try{
            ci.sendErrorMessage(new ProtocolErrorMessage(msg));
        }catch(IOException e2){}
    }

    private ClientAction receiveAction(){
        try {
            ClientAction ca = ci.recieveClientAction();
            return ca;
        } catch (IOException e) {
            sendErrorMessage("Communication failed");
        } catch (ProtocolException e) {
            sendErrorMessage(e.getMessage());
        } catch (ProtocolErrorMessage e) {
            sendErrorMessage(e.getMessage());
        }
        return null;
    }

    private boolean sendAction(ServerAction sa){
        try {
            ci.sendServerAction(sa);
            return true;
        } catch (IOException e) {
            sendErrorMessage("Communication failed, could not send action");
            return false;
        }
    }
}