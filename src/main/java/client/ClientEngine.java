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
    private UserInterface ui;
    
    private final static int START = 0;
    private final static int LOBBY = 1;
    private final static int PLAY = 2;
    private final static int CLIENT_TURN = 3;
    private final static int SERVER_TURN = 10;
    private final static int GAME_END = 4;
    
    
    private final static int CLIENT = 1;
    private final static int SERVER = 2;
    private final static int NULL = -1;
    
    public ClientEngine(CommunicationInterface ci, UserInterface ui){
        this.ci = ci;
        this.ui = ui;
    }
    
    public void run(){
        run_game();
        close_comms();
    }
    
    private void close_comms(){
        try{
            ci.close();
        }catch(IOException e){}
    }
    
    private void run_game(){
        int sessionState = START;
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
                            sessionState = CLIENT_TURN;
                            firstPlayer = CLIENT;
                            break;
                        case ServerPlay.SERVER:
                            sessionState = SERVER_TURN;
                            firstPlayer = SERVER;
                            break;
                    }
                    break;
                }
                
                case CLIENT_TURN:{
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Dice){sendErrorMessage("Expected DICE command."); END = true; break;}
                    ui.showServerAction(sa, UserState.INGAME);
                    diceRoll = ((ServerDice)sa).diceList;
                    play_loop: while(turnRound < 2){
                        ClientAction ca = null;
                        boolean repeat = false;
                        do{
                            ca = ui.queryUserAction(UserState.INGAME);
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
                            break play_loop;
                        }
                        if(!sendAction(ca) || ca.command == ClientCommand.Exit){END = true; break;}
                        turnRound++;
                    }
                    sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Points){sendErrorMessage("Expected PNTS command."); END = true; break;}
                    ui.showServerAction(sa, UserState.INGAME);
                    if(firstPlayer == CLIENT){
                        sessionState = SERVER_TURN;
                    }else{
                        sessionState = GAME_END;
                    }
                    break;
                    
                }
                
                case SERVER_TURN:{
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Dice){sendErrorMessage("Expected DICE command."); END = true; break;}
                    ui.showServerAction(sa, UserState.INGAME);
                    diceRoll = ((ServerDice)sa).diceList;
                    play_loop: while(turnRound < 2){
                        sa = recieveAction(); 
                        if(sa == null){END = true; break;}
                        if(sa.command != ServerCommand.Take || sa.command != ServerCommand.Pass){sendErrorMessage("Expected TAKE or PASS command."); END = true; break;}
                        ui.showServerAction(sa, UserState.INGAME);
                        if(sa.command == ServerCommand.Pass){
                            break play_loop;
                        }
                        turnRound++;
                    }
                    sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa.command != ServerCommand.Points){sendErrorMessage("Expected PNTS command."); END = true; break;}
                    ui.showServerAction(sa, UserState.INGAME);
                    if(firstPlayer == SERVER){
                        sessionState = CLIENT_TURN;
                    }else{
                        sessionState = GAME_END;
                    }
                    break;
                    
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
                    break;
                }
            }
        }
        ui.goodbyMessage();
    }
    
    private boolean checkLegalMove(DiceValue[] roll, boolean[] taken){
        boolean[] step = new boolean[]{false, false, false, false};
        for(int i = 0; i < 5; i++){
            if(taken[i]){
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
            System.out.println(ca + " to server");
            return true;
        }catch(IOException e){
            ui.showInternalError("Communication with server failed");
            e.printStackTrace();
            return false;
        }
    }
    
    private ServerAction recieveAction(){
        //System.out.println("Waiting action from client");
        try{
            ServerAction sa = ci.recieveServerAction();
            System.out.println(sa + " from server");
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
    
}
