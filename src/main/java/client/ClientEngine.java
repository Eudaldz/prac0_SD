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
import java.net.SocketTimeoutException;
import java.util.Arrays;


public class ClientEngine{
    
    private class ErrorAction extends ServerAction{
        public String message;
        public ErrorAction(){
            super(null);
        }
    }
    
    ErrorAction errorSignal = new ErrorAction();
    
    private Socket socket;
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
    
    public ClientEngine(Socket socket, UserInterface ui)throws IOException{
        this.socket = socket;
        this.ci = new EloisProtocolComms(socket.getInputStream(), socket.getOutputStream());
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
        
        
        boolean END = false;
        
        int gems = 0;
        
        
        main_loop: while(!END){
            switch(sessionState){
                case START:{
                    ui.welcomeMessage();
                    ClientAction ca = ui.queryUserAction(UserState.START);
                    if(!sendAction(ca) || ca.command == ClientCommand.Exit){END = true; break main_loop;}
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break main_loop;}
                    if(sa == errorSignal){break main_loop;}
                    if(sa.command != ServerCommand.Cash){sendErrorMessage("Expected CASH command."); END = true; break main_loop;}
                    gems = ((ServerCash)sa).cash;
                    ui.showServerAction(sa, UserState.START);
                    sessionState = LOBBY;
                    break;
                }
                    
                case LOBBY:{
                    ClientAction ca = null;
                    boolean repeat = false;
                    do{
                        repeat = false;
                        ca = ui.queryUserAction(UserState.LOBBY);
                        if(ca.command == ClientCommand.Bett && gems <= 0){
                            ui.showInputError("Not enough gems to bett");
                            repeat = true;
                        }
                    }while(repeat);
                    
                    if(!sendAction(ca) || ca.command == ClientCommand.Exit){END = true; break main_loop;}
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break main_loop;}
                    if(sa == errorSignal){break main_loop;}
                    if(sa.command != ServerCommand.Loot){sendErrorMessage("Expected LOOT command."); END = true; break main_loop;}
                    ui.showServerAction(sa, UserState.LOBBY);
                    sessionState = PLAY;
                    break;
                }
                
                case PLAY:{
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break main_loop;}
                    if(sa == errorSignal){break main_loop;}
                    if(sa.command != ServerCommand.Play){sendErrorMessage("Expected PLAY command."); END = true; break main_loop;}
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
                    DiceValue[] diceRoll;
                    boolean[] diceTaken = new boolean[]{false, false, false, false, false};
                    int turnRound = 0;
                    
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break main_loop;}
                    if(sa == errorSignal){break main_loop;}
                    if(sa.command != ServerCommand.Dice){sendErrorMessage("Expected DICE command."); END = true; break main_loop;}
                    ui.showServerAction(sa, UserState.INGAME);
                    diceRoll = ((ServerDice)sa).diceList;
                    play_loop: while(turnRound < 2){
                        ClientAction ca = null;
                        boolean repeat = false;
                        do{
                            repeat = false;
                            ca = ui.queryUserAction(UserState.INGAME);
                            if(ca.command == ClientCommand.Take){
                                byte[] diceIndex = ((ClientTake)ca).diceIndexList;
                                boolean[] tmpTaken = diceTaken.clone();
                                for(int i = 0; i < diceIndex.length; i++){
                                    tmpTaken[diceIndex[i]-1] = true;
                                }
                                if(checkLegalMove(diceRoll, tmpTaken)){
                                    System.arraycopy(tmpTaken, 0, diceTaken, 0, diceTaken.length);
                                }else{
                                    ui.showInputError("Invalid dice selection. Please try again.");
                                    repeat = true;
                                }
                            }
                        }while(repeat);
                        
                        if(!sendAction(ca) || ca.command == ClientCommand.Exit){END = true; break main_loop;}
                        if(ca.command == ClientCommand.Pass){
                            break play_loop;
                        }
                        sa = recieveAction();
                        if(sa == null){END = true; break main_loop;}
                        if(sa == errorSignal){break main_loop;}
                        if(sa.command != ServerCommand.Dice){sendErrorMessage("Expected DICE command."); END = true; break main_loop;}
                        diceRoll = ((ServerDice)sa).diceList;
                        ui.showServerAction(sa, UserState.INGAME);
                        
                        turnRound++;
                        
                    }
                    sa = recieveAction();
                    if(sa == null){END = true; break main_loop;}
                    if(sa == errorSignal){break main_loop;}
                    if(sa.command != ServerCommand.Points){sendErrorMessage("Expected PNTS command."); END = true; break main_loop;}
                    ui.showServerAction(sa, UserState.INGAME);
                    if(firstPlayer == CLIENT){
                        sessionState = SERVER_TURN;
                    }else{
                        sessionState = GAME_END;
                    }
                    break;
                    
                }
                
                case SERVER_TURN:{
                    DiceValue[] diceRoll;
                    boolean[] diceTaken = new boolean[]{false, false, false, false, false};
                    int turnRound = 0;
                    
                    ServerAction sa = recieveAction();
                    if(sa == null){END = true; break main_loop;}
                    if(sa == errorSignal){break main_loop;}
                    if(sa.command != ServerCommand.Dice){sendErrorMessage("Expected DICE command."); END = true; break main_loop;}
                    ui.showServerAction(sa, UserState.INGAME);
                    diceRoll = ((ServerDice)sa).diceList;
                    play_loop: while(turnRound < 2){
                        sa = recieveAction(); 
                        if(sa == null){END = true; break main_loop;}
                        if(sa == errorSignal){break main_loop;}
                        if(sa.command != ServerCommand.Take && sa.command != ServerCommand.Pass){sendErrorMessage("Expected TAKE or PASS command."); END = true; break main_loop;}
                        ui.showServerAction(sa, UserState.INGAME);
                        if(sa.command == ServerCommand.Pass){
                            break play_loop;
                        }
                        sa = recieveAction();
                        if(sa == null){END = true; break main_loop;}
                        if(sa == errorSignal){break main_loop;}
                        if(sa.command != ServerCommand.Dice){sendErrorMessage("Expected DICE command."); END = true; break main_loop;}
                        ui.showServerAction(sa, UserState.INGAME);
                        turnRound++;
                    }
                    sa = recieveAction();
                    if(sa == null){END = true; break main_loop;}
                    if(sa == errorSignal){break main_loop;}
                    if(sa.command != ServerCommand.Points){sendErrorMessage("Expected PNTS command."); END = true; break main_loop;}
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
                    if(sa == errorSignal){break main_loop;}
                    if(sa.command != ServerCommand.Wins){sendErrorMessage("Expected WINS command."); END = true; break main_loop;}
                    ui.showServerAction(sa, UserState.INGAME);
                    
                    sa = recieveAction();
                    if(sa == null){END = true; break;}
                    if(sa == errorSignal){break main_loop;}
                    if(sa.command != ServerCommand.Cash){sendErrorMessage("Expected CASH command."); END = true; break main_loop;}
                    gems = ((ServerCash)sa).cash;
                    ui.showServerAction(sa, UserState.INGAME);
                    
                    sessionState = LOBBY;
                    break;
                }
            }
        }
        ui.goodbyMessage();
    }
    
    private static boolean checkLegalMove(DiceValue[] roll, boolean[] taken){
        boolean[] step = new boolean[]{false, false, false, false};
        for(int i = 0; i < 5; i++){
            if(taken[i]){
                if(roll[i] == DiceValue.Six && !step[0])step[0] = true;
                else if(roll[i] == DiceValue.Five && !step[1])step[1] = true;
                else if(roll[i] == DiceValue.Four && !step[2])step[2] = true;
                else step[3] = true;
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
            System.out.println("send "+ca);
            ci.sendClientAction(ca);
            return true;
        }catch(IOException e){
            ui.showInternalError("Communication with server failed");
            return false;
        }
    }
    
    private ServerAction recieveAction(){
        //System.out.println("Waiting action from client");
        try{
            while(true){    
                try{
                    ServerAction sa = ci.recieveServerAction();
                    return sa;
                }catch(SocketTimeoutException e){
                    if(!socket.getInetAddress().isReachable(1000))return null;
                } 
            }
        }catch(IOException e){
            ui.showInternalError("Communication with server failed");
        }catch(ProtocolErrorMessage e){
            errorSignal.message = e.getMessage();
            ui.showServerError(e);
            return errorSignal;
        }catch(ProtocolException e){
            sendErrorMessage(e.getMessage());
        }
        return null;
    }
}
