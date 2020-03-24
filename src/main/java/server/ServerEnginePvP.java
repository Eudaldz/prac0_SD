/**
 * This class is the engine of the Server in the mode Player vs Player.
 * This class will communicate with both clients acting according to its state machine.
 */
package server;

import common.*;
import common.server_actions.*;
import common.client_actions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerEnginePvP implements Runnable {

    private CommunicationInterface ci1;
    private CommunicationInterface ci2;
    private PlayerGame player1game, player2game;
    private ClientAction ca1;
    private ClientAction ca2;
    private String client1Address,client2Address;
    private Socket socket1;
    private Socket socket2;

    private final static int CLIENT_1 = 0;
    private final static int CLIENT_2 = 1;
    private int CLIENT_1_ID=CLIENT_1;
    private int CLIENT_2_ID=CLIENT_2;

    private final static int START = 0;
    private final static int LOBBY = 1;
    private final static int PLAY = 2;
    private final static int CLIENT_1_PLAY = 3;
    private final static int CLIENT_2_PLAY = 4;
    private final static int GAME_END = 5;
    
    private final static int CLIENT1_TURN = 1;
    private final static int CLIENT2_TURN = 2;

    private FileWriter logger;

    private Random rand = new Random();

    public ServerEnginePvP(Socket socket1, Socket socket2, String client1Address, String client2Address)throws IOException{
        this.socket1 = socket1;
        this.socket2 = socket2;
        this.ci1 = new EloisProtocolComms(socket1.getInputStream(), socket1.getOutputStream());
        this.ci2 = new EloisProtocolComms(socket2.getInputStream(), socket2.getOutputStream());
        this.player1game = new PlayerGame();//PLAYER 1
        this.player2game = new PlayerGame();//PLAYER 2
        this.client1Address = client1Address;
        this.client2Address = client2Address;
    }

    @Override
    public void run(){
        try{
            System.out.println("Game started with "+client1Address + " and "+client2Address);
            loggerConfig();
            run_game();
            System.out.println("Game ended with "+client1Address + " and "+client2Address);
            close_comms();
            System.out.println("Connection ended with "+client1Address + " and "+client2Address);
        }catch(Exception e){
            System.out.println("Game aborted with "+client1Address + " and "+client2Address);
            e.printStackTrace();
            close_comms();
        }
    }

    private void close_comms(){
        try{
            ci1.close();
            ci2.close();
            logger.close();
        }catch(IOException e){}
    }

    private void run_game() {
        boolean END = false;
        int last_loser=0;//0 means tie or first game, 1 means CLIENT_1 lost the last game, 2 means CLIENT_2 lost the last game
        int gameLoot = 0;
        int sessionState = START;
        int first_turn = 0;//1 --> CLIENT_1 starts | 2 --> CLIENT_2 starts
        boolean current_turn=false;//false --> CLIENT_1 turn | true --> CLIENT_2 turn

        main_loop : while (!END){
            switch(sessionState){
                case START:{
                    ca1 = receiveActionC1();
                    ca2 = receiveActionC2();
                    if (ca1 == null) {END = true;break main_loop;}
                    if (ca2 == null) {END = true;break main_loop;}
                    
                    if(ca1.command == ClientCommand.Start && ca2.command == ClientCommand.Start){
                        CLIENT_1_ID=((ClientStart)ca1).id;
                        CLIENT_2_ID=((ClientStart)ca2).id;
                        if(CLIENT_1_ID == CLIENT_2_ID){
                            sendErrorMessageBoth("ID already taken");
                            continue main_loop;
                        }
                        if (!sendActionC1(new ServerCash(player1game.getGems()))) {END = true;break main_loop;}
                        if (!sendActionC2(new ServerCash(player2game.getGems()))) {END = true;break main_loop;}
                        sessionState = LOBBY;
                    
                    }else if(ca1.command == ClientCommand.Exit || ca2.command == ClientCommand.Exit){
                        if(ca1.command == ClientCommand.Exit && ca2.command != ClientCommand.Exit){
                            sendErrorMessageC2("Game terminated");;
                        }else if(ca1.command != ClientCommand.Exit && ca2.command == ClientCommand.Exit){
                            sendErrorMessageC1("Game terminated");
                        }
                        END = true;
                        break main_loop;
                    }else{
                        sendErrorMessageBoth("Invalid response from certain client");
                        continue main_loop;
                    }
                    break;
                }
                case LOBBY:{
                    ca1 = receiveActionC1();
                    ca2 = receiveActionC2();
                    if (ca1 == null) {END = true; break main_loop;}
                    if (ca2 == null) {END = true; break main_loop;}
                    if (ca1.command == ClientCommand.Bett && ca2.command == ClientCommand.Bett){
                        player1game.newGame();
                        player2game.newGame();
                        if(player1game.hasGems() && player2game.hasGems()){
                            gameLoot += 2;
                            player1game.addGems(-1);
                            player2game.addGems(-1);
                            sessionState = PLAY;
                            if( !sendActionBoth(new ServerLoot(gameLoot))){
                                END = true;
                                break main_loop;
                            }
                        }else{
                            String c1msg = "Adversary has not enough gems";
                            String c2msg = "Adversary has not enough gems";
                            if(!player1game.hasGems())c1msg = "Not enough gems";
                            if(!player2game.hasGems())c2msg = "Not enough gems";
                            sendErrorMessageC1(c1msg);
                            sendErrorMessageC2(c2msg);
                            continue main_loop;
                        }
                        
                    }else if(ca1.command == ClientCommand.Exit || ca2.command == ClientCommand.Exit){
                        if(ca1.command == ClientCommand.Exit && ca2.command != ClientCommand.Exit){
                            sendErrorMessageC2("Game terminated");;
                        }else if(ca1.command != ClientCommand.Exit && ca2.command == ClientCommand.Exit){
                            sendErrorMessageC1("Game terminated");
                        }
                        END = true;
                        break main_loop;
                    }else{
                        sendErrorMessageBoth("Invalid response from certain client");
                        continue main_loop;
                    }
                    break;
                }
                case PLAY:{
                    first_turn = rand.nextInt(2)+1;
                    if(last_loser == 1) first_turn=1;
                    else if (last_loser == 2)first_turn=2;

                    if (first_turn == 1){//Case CLIENT_1 first
                        if(!sendActionC1(new ServerPlay((byte) 0)) || !sendActionC2(new ServerPlay((byte) 1))) {END=true;break main_loop;}
                        sessionState=CLIENT_1_PLAY;
                    }else {//Case CLIENT_2 first
                        if (!sendActionC1(new ServerPlay((byte) 1)) || !sendActionC2(new ServerPlay((byte) 0))) {END = true;break main_loop;}
                        sessionState=CLIENT_2_PLAY;
                    }
                    break;
                }
                case CLIENT_1_PLAY:{
                    player1game.reroll();
                    if(!sendActionBoth(new ServerDice(CLIENT_1_ID,player1game.getDiceValues()))) {END=true;break main_loop;}
                    play_loop: while(player1game.newTurnAvailable()){
                        ca1 = receiveActionC1();
                        if(ca1 == null){END = true; break main_loop;}
                        if(ca1.command == ClientCommand.Pass){
                            if(!sendActionC2(new ServerPass(CLIENT_1_ID))) {END=true;break main_loop;}//Notify CLIENT_2 of PASS
                            break play_loop;
                        }else if(ca1.command == ClientCommand.Take){
                            byte[] take = ((ClientTake)ca1).diceIndexList;
                            if(!sendActionC2(new ServerTake(CLIENT_1_ID,take))) {END=true;break main_loop;}//Notify CLIENT_2 of TAKE
                            take = decreaseIndeces(take);
                            player1game.take(take);
                            player1game.reroll();
                            if(!sendActionBoth(new ServerDice(CLIENT_1_ID,player1game.getDiceValues()))) {END=true;break main_loop;}//We send DICE to both players
                        
                        }else if(ca1.command == ClientCommand.Exit){
                            sendErrorMessageC2("Game terminated");
                            END = true;
                            break main_loop;
                        }else{
                            sendErrorMessageBoth("Invalid response from certain client");
                            continue main_loop;
                        }
                    }
                    int points = player1game.getPoints();
                    if(!sendActionBoth(new ServerPoints(CLIENT_1_ID,points))){END=true;break main_loop;}//We send PNTS to both players
                    if(first_turn == CLIENT1_TURN){
                        sessionState = CLIENT_2_PLAY;
                    }else{
                        sessionState = GAME_END;
                    }
                    break;
                }
                case CLIENT_2_PLAY:{
                    player2game.reroll();
                    if(!sendActionBoth(new ServerDice(CLIENT_2_ID,player2game.getDiceValues()))) {END=true;break main_loop;}
                    play_loop: while(player2game.newTurnAvailable()){
                        ca2 = receiveActionC2();
                        if(ca2 == null){END = true; break main_loop;}
                        if(ca2.command == ClientCommand.Pass){
                            if(!sendActionC1(new ServerPass(CLIENT_2_ID))) {END=true;break main_loop;}//Notify CLIENT_2 of PASS
                            break play_loop;
                        }else if(ca2.command == ClientCommand.Take){
                            byte[] take = ((ClientTake)ca2).diceIndexList;
                            if(!sendActionC1(new ServerTake(CLIENT_2_ID,take))) {END=true;break main_loop;}//Notify CLIENT_2 of TAKE
                            take = decreaseIndeces(take);
                            player2game.take(take);
                            player2game.reroll();
                            if(!sendActionBoth(new ServerDice(CLIENT_2_ID,player2game.getDiceValues()))) {END=true;break main_loop;}//We send DICE to both players
                        }else if(ca2.command == ClientCommand.Exit){
                            sendErrorMessageC1("Game terminated");
                            END = true;
                            break main_loop;
                        }else{
                            sendErrorMessageBoth("Invalid response from certain client");
                            continue main_loop;
                        }
                    }
                    int points = player2game.getPoints();
                    if(!sendActionBoth(new ServerPoints(CLIENT_2_ID,points))){END=true;break main_loop;}//We send PNTS to both players
                    if(first_turn == CLIENT2_TURN){
                        sessionState = CLIENT_1_PLAY;
                    }else{
                        sessionState = GAME_END;
                    }
                    break;
                }
                case GAME_END:{
                    int winnerC1 = 2;
                    int winnerC2 = 2;
                    last_loser = 0;
                    if(player1game.getPoints() > player2game.getPoints()){//Wins player1
                        player1game.addGems(gameLoot);
                        winnerC1 = 0;
                        winnerC2 = 1;
                        last_loser = 2;
                        gameLoot = 0;
                    }else if (player1game.getPoints() < player2game.getPoints()){//Wins player2
                        player2game.addGems(gameLoot);
                        winnerC1 = 1;
                        winnerC2 = 0;
                        last_loser = 1;
                        gameLoot = 0;
                    }
                    if(!sendActionC1(new ServerWins((byte) winnerC1))){END=true;break main_loop;}
                    if(!sendActionC2(new ServerWins((byte) winnerC2))){END=true;break main_loop;}
                    if (!sendActionC1(new ServerCash(player1game.getGems()))) {END = true;break main_loop;}
                    if (!sendActionC2(new ServerCash(player2game.getGems()))) {END = true;break main_loop;}
                    
                    sessionState = LOBBY;
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
    
    private void sendErrorMessageBoth(String msg){
        sendErrorMessageC1(msg);
        sendErrorMessageC2(msg);
    }
    
    private void sendErrorMessageC1(String msg){
        try{
            ci1.sendErrorMessage(new ProtocolErrorMessage(msg));
            logger.write("S->C1: ERRO "+msg+"\n");
            logger.flush();
        }catch(IOException e2){}
    }

    private void sendErrorMessageC2(String msg){
        try{
            ci2.sendErrorMessage(new ProtocolErrorMessage(msg));
            logger.write("S->C2: ERRO "+msg+"\n");
            logger.flush();
        }catch(IOException e2){}
    }

    private ClientAction receiveActionC1(){
        try {
            while(true){    
                try{
                    ClientAction ca = ci1.recieveClientAction();
                    logger.write("C1: "+ca.protocolPrint()+"\n");
                    logger.flush();
                    return ca;
                }catch(SocketTimeoutException e){
                    if(!socket1.getInetAddress().isReachable(1000))return null;
                } 
            }
        } catch (IOException e) {
            System.out.println("Communication failed");
        } catch (ProtocolException e) {
            sendErrorMessageC1(e.getMessage());
        } catch (ProtocolErrorMessage e) {
            //sendErrorMessage(e.getMessage());
        }
        return null;
    }

    private ClientAction receiveActionC2(){
        try {
            while(true){    
                try{
                    ClientAction ca = ci2.recieveClientAction();
                    logger.write("C2: "+ca.protocolPrint()+"\n");
                    logger.flush();
                    return ca;
                }catch(SocketTimeoutException e){
                    if(!socket2.getInetAddress().isReachable(1000))return null;
                } 
            }
        } catch (IOException e) {
            System.out.println("Communication failed");
        } catch (ProtocolException e) {
            sendErrorMessageC2(e.getMessage());
        } catch (ProtocolErrorMessage e) {
            //sendErrorMessage(e.getMessage());
        }
        return null;
    }
    
    private boolean sendActionBoth(ServerAction sa){
        boolean r1 = sendActionC1(sa);
        boolean r2 = sendActionC2(sa);
        return r1 && r2;
    }

    private boolean sendActionC1(ServerAction sa){
        try {
            ci1.sendServerAction(sa);
            logger.write("S->C1: "+sa.protocolPrint()+"\n");
            logger.flush();
            return true;
        } catch (IOException e) {
            sendErrorMessageC1("Communication failed, could not send action");
            return false;
        }
    }

    private boolean sendActionC2(ServerAction sa){
        try {
            ci2.sendServerAction(sa);
            logger.write("S->C2: "+sa.protocolPrint()+"\n");
            logger.flush();
            return true;
        } catch (IOException e) {
            sendErrorMessageC2("Communication failed, could not send action");
            return false;
        }
    }
    private boolean loggerConfig(){
        try{
            logger = new FileWriter(Thread.currentThread().getName()+".log", false);
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}