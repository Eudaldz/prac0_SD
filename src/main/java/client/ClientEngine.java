package client;

import common.CommunicationInterface;
import common.DiceValue;
import common.EloisProtocolComms;
import common.ProtocolErrorMessage;
import common.ProtocolException;
import common.client_actions.*;
import common.server_actions.*;
import java.io.IOException;
import java.net.Socket;


public class ClientEngine{
    
    private CommunicationInterface ci;
    private Socket s;
    private UserInterface ui;
    
    private final static int START = 0;
    private final static int LOBBY = 1;
    private final static int PLAY = 2;
    private final static int GAME_TURN = 3;
    private final static int TURN_END = 6;
    private final static int GAME_END = 4;
    
    
    private final static int CLIENT = 1;
    private final static int SERVER = 2;
    private final static int NULL = -1;
    
    public ClientEngine(Socket s, UserInterface ui){
        this.s = s;
        this.ui = ui;
    }
    
    public void run(){
        if(!initStream()){
            System.out.println("Fatal error while opening the communication streams");
            return;
        }
        int sessionState = START;
        int playerTurn = NULL;
        int firstPlayer = NULL;
        int turnRound = 0;
        DiceValue[] diceRoll;
        boolean[] diceTaken = new boolean[]{false, false, false, false, false};
        
        boolean END = false;
        
        
        while(!END){
            switch(sessionState){
                case START:{
                    ui.welcomeMessage();
                    ClientAction ca = ui.queryUserAction(UserState.START);
                    if(!sendAction(ca) || ca.command == ClientCommand.Exit){END = true; break;}
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Cash){sendErrorMessage("Expected CASH command."); END = true; break;}
                    ui.showServerAction(sa, UserState.START);
                    sessionState = LOBBY;
                    break;
                }
                    
                case LOBBY:{
                    ClientAction ca = ui.queryUserAction(UserState.LOBBY);
                    if(!sendAction(ca) || ca.command == ClientCommand.Exit){END = true; break;}
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Loot){sendErrorMessage("Expected LOOT command."); END = true; break;}
                    ui.showServerAction(sa, UserState.LOBBY);
                    sessionState = PLAY;
                    break;
                }
                
                case PLAY:{
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Play){sendErrorMessage("Expected PLAY command."); END = true; break;}
                    ui.showServerAction(sa, UserState.INGAME);
                    switch(((ServerPlay)sa).value){
                        case ServerPlay.CLIENT:
                            playerTurn = CLIENT;
                            firstPlayer = CLIENT;
                            break;
                        case ServerPlay.SERVER:
                            playerTurn = SERVER;
                            firstPlayer = SERVER;
                            break;
                    }
                    sessionState = GAME_TURN;
                    break;
                }
                
                case GAME_TURN:{
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Dice){sendErrorMessage("Expected DICE command."); END = true; break;}
                    ui.showServerAction(sa, UserState.INGAME);
                    turnRound++;
                    if(turnRound == 3){
                        sessionState = TURN_END;
                    }
                    diceRoll = ((ServerDice)sa).diceList;
                    
                    switch(playerTurn){
                        case CLIENT:{
                            ClientAction ca = null;
                            boolean repeat = false;
                            do{
                                ui.queryUserAction(UserState.INGAME);
                                if(ca.command == ClientCommand.Take){
                                    byte[] diceIndex = ((ClientTake)ca).diceIndexList;
                                    for(int i = 0; i < diceIndex.length; i++){
                                        diceTaken[diceIndex[i]] = true;
                                    }
                                    if(!checkLegalMove(diceRoll, diceTaken)){
                                        ui.showInputError("Invalid dice selection. Please try again.");
                                        repeat = true;
                                    }
                                }
                            }while(repeat);
                            if(ca.command == ClientCommand.Pass){
                                    sessionState = TURN_END;
                            }
                            if(!sendAction(ca) || ca.command == ClientCommand.Exit){END = true; break;}
                        }
                        case SERVER:{
                            sa = recieveAction();
                            if(sa == null){END = true; break;}
                            if(sa.command != ServerCommand.Pass && sa.command != ServerCommand.Take){sendErrorMessage("Expected TAKE or PASS command."); END = true; break;}
                            ui.showServerAction(sa, UserState.INGAME);
                            if(sa.command == ServerCommand.Pass){
                                sessionState = TURN_END;
                            }
                        }
                    }
                }
                
                case TURN_END:{
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Points){sendErrorMessage("Expected PNTS command."); END = true; break;}
                    ui.showServerAction(sa, UserState.INGAME);
                    if((playerTurn == CLIENT && firstPlayer == SERVER) || (playerTurn == SERVER && firstPlayer == CLIENT) ){
                        sessionState = GAME_END;
                    }else{
                        sessionState = GAME_TURN;
                        if(firstPlayer == CLIENT)playerTurn = SERVER;
                        else playerTurn = CLIENT;
                    }
                }
                
                case GAME_END:{
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Wins){sendErrorMessage("Expected WINS command."); END = true; break;}
                    ui.showServerAction(sa, UserState.INGAME);
                    
                    sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Cash){sendErrorMessage("Expected CASH command."); END = true; break;}
                    ui.showServerAction(sa, UserState.INGAME);
                    
                    sessionState = LOBBY;
                }
            }
        }
        ui.goodbyMessage();
    }
    
    private boolean checkLegalMove(DiceValue[] roll, boolean[] taken){
        boolean[] step = new boolean[]{false, false, false, false};
        for(int i = 0; i < 5; i++){
            switch(roll[i]){
                case Six:
                    step[0] = true;
                    break;
                case Five:
                    step[1] = true;
                    break;
                case Four:
                    step[2] = true;
                    break;
                default:
                    step[3] = true;
                    break;
            }
        }
        boolean under = false;
        for(int i = 3; i >= 0; i--){
            if(step[i])under = true;
            else if(under)return false;
        }
        return true;
    }
    
    private void sendErrorMessage(String msg){
        ui.showInternalError("Incorrect server response. ");
        try{
            ci.sendErrorMessage(new ProtocolErrorMessage(msg));
        }catch(IOException e2){}
    }
    
    private boolean sendAction(ClientAction ca){
        try{
            ci.sendClientAction(ca);
            return true;
        }catch(IOException e){
            ui.showInternalError("Communication with server failed");
            return false;
        }
    }
    
    private ServerAction recieveAction(){
        try{
            ServerAction sa = ci.recieveServerAction();
            return sa;
        }catch(IOException e){
            ui.showInternalError("Communication with server failed");
        }catch(ProtocolErrorMessage e){
            ui.showServerError(e);
        }catch(ProtocolException e){
            sendErrorMessage(e.getMessage());
        }
        return null;
    }
    
    private boolean initStream(){
        try{
            ci = new EloisProtocolComms(s.getInputStream(), s.getOutputStream());
            return true;
        }catch(Exception e){}
        return false;
    }
    
}
