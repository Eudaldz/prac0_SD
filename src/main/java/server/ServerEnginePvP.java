/**
 * This class is the engine of the Server in the mode Player vs Player.
 * This class will communicate with both clients acting according to its state machine.
 */
package server;

import common.*;
import common.server_actions.*;
import common.client_actions.*;
import java.io.IOException;
import java.util.Random;

public class ServerEnginePvP implements Runnable {

    private CommunicationInterface ci1;
    private CommunicationInterface ci2;
    private PlayerGame player1game, player2game;
    private ClientAction ca1;
    private ClientAction ca2;
    private String client1Address,client2Address;

    private final static int ROUND_FEE = 1;

    private final static int CLIENT_1 = 0;
    private final static int CLIENT_2 = 1;
    private int CLIENT_1_ID=CLIENT_1;
    private int CLIENT_2_ID=CLIENT_2;

    private final static int START = 0;
    private final static int LOBBY = 1;
    private final static int PLAY = 2;
    private final static int CLIENT_1_PLAY = 3;
    private final static int CLIENT_2_PLAY = 4;

    private Random rand = new Random();

    public ServerEnginePvP(CommunicationInterface ci1, CommunicationInterface ci2, int mode, String client1Address, String client2Address){
        this.ci1 = ci1;//Communication interface for player 1.
        this.ci2 = ci2;//Communication interface for player 2.
        this.player1game = new PlayerGame();//PLAYER 1
        this.player2game = new PlayerGame();//PLAYER 2
        this.client1Address = client1Address;
        this.client2Address = client2Address;
    }

    @Override
    public void run(){
        run_game();
        close_comms();
    }

    private void close_comms(){
        try{
            ci1.close();
            ci2.close();
        }catch(IOException e){}
    }

    private void run_game() {
        boolean END = false;
        int last_loser=0;//0 means tie or first game, 1 means CLIENT_1 lost the last game, 2 means CLIENT_2 lost the last game
        int lobby_money=0;
        int sessionState = START;
        boolean first_turn=false;//false --> CLIENT_1 starts | true --> CLIENT_2 starts
        boolean current_turn=false;//false --> CLIENT_1 turn | true --> CLIENT_2 turn

        System.out.println("Begin game");
        main_loop : while (!END){
            System.out.println("Begin game");
            switch(sessionState){
                case START:{
                    ca1 = receiveActionC1();
                    CLIENT_1_ID=((ClientStart)ca1).id;

                    if (ca1 == null) {END = true;break main_loop;}
                    if (ca1.command != ClientCommand.Start) {
                        sendErrorMessageC1("Expected STRT command");
                        END = true;
                        break main_loop;
                    }
                    if (!sendActionC1(new ServerCash(player1game.getGems()))) {
                        END = true;
                        break main_loop;
                    }
                    ca2 = receiveActionC2();
                    CLIENT_2_ID=((ClientStart)ca2).id;
                    if (ca2 == null) {END = true;break main_loop;}
                    if (ca2.command != ClientCommand.Start) {
                        sendErrorMessageC2("Expected STRT command");
                        END = true;
                        break main_loop;
                    }
                    if (!sendActionC2(new ServerCash(player2game.getGems()))) {
                        END = true;
                        break main_loop;
                    }
                    sessionState = LOBBY;
                    break main_loop;
                }
                case LOBBY:{
                    ca1 = receiveActionC1();
                    if (ca1 == null) END = true;
                    if (ca1.command.equals(ClientCommand.Bett)){
                        player1game.newGame();
                        player1game.addGems(-ROUND_FEE);//We take 1 gem away.
                        lobby_money+=ROUND_FEE;
                    }
                    else if (ca1.command.equals(ClientCommand.Exit)) {
                        END = true;
                        lobby_money=0;
                        break main_loop;
                    }
                    else{
                        sendErrorMessageC1("Expected BETT or EXIT command");
                        END=true;
                        break main_loop;
                    }
                    ca2 = receiveActionC2();
                    if (ca2 == null) END = true;
                    if (ca2.command.equals(ClientCommand.Bett)){
                        player2game.newGame();
                        player2game.addGems(-ROUND_FEE);//We take 1 gem away.
                        lobby_money+=ROUND_FEE;
                    }
                    else if (ca2.command.equals(ClientCommand.Exit)) {
                        END = true;
                        lobby_money=0;
                        break main_loop;
                    }
                    else{
                        sendErrorMessageC2("Expected BETT or EXIT command");
                        END=true;
                        break;
                    }
                    //Case both ca1 and ca2 are BETT.
                    if (!sendActionC1(new ServerLoot(player1game.getGems())) || !sendActionC2(new ServerLoot(player1game.getGems())) ) {
                        END = true;
                        break main_loop;
                    }
                    sessionState = PLAY;
                    break main_loop;
                }
                case PLAY:{
                    if (last_loser == 0) first_turn = rand.nextBoolean();
                    else if(last_loser == 1) first_turn=false;
                    else first_turn=true;

                    if (first_turn){//Case CLIENT_1 first
                        if(!sendActionC1(new ServerPlay((byte) 0)) || !sendActionC2(new ServerPlay((byte) 1))) {END=true;break main_loop;}
                    }else {//Case CLIENT_2 first
                        if (!sendActionC1(new ServerPlay((byte) 1)) || !sendActionC2(new ServerPlay((byte) 0))) {
                            END = true;
                            break main_loop;
                        }
                    }
                    current_turn=first_turn;
                    if (!current_turn){
                        sessionState=CLIENT_1_PLAY;
                    }else{
                        sessionState=CLIENT_2_PLAY;
                    }
                    break main_loop;
                }
                case CLIENT_1_PLAY:{
                    player1game.reroll();
                    if(!sendActionC1(new ServerDice(CLIENT_1_ID,player1game.getDiceValues()))) {END=true;break main_loop;}
                    if(!sendActionC2(new ServerDice(CLIENT_2_ID,player1game.getDiceValues()))) {END=true;break main_loop;}
                    play_loop: while(player1game.newTurnAvailable()){
                        ca1 = receiveActionC1();
                        if(ca1 == null){END = true; break main_loop;}
                        if(ca1.command == ClientCommand.Pass){
                            if(!sendActionC2(new ServerPass(CLIENT_2_ID))) {END=true;break main_loop;}//Notify CLIENT_2 of PASS
                            break play_loop;
                        }else if(ca1.command == ClientCommand.Take){
                            byte[] take = ((ClientTake)ca1).diceIndexList;
                            if(!sendActionC2(new ServerTake(CLIENT_2_ID,take))) {END=true;break main_loop;}//Notify CLIENT_2 of TAKE
                            player1game.take(take);
                            player1game.reroll();
                            if(!sendActionC1(new ServerDice(CLIENT_1_ID,player1game.getDiceValues()))) {END=true;break main_loop;}//We send DICE to both players
                            if(!sendActionC2(new ServerDice(CLIENT_2_ID,player1game.getDiceValues()))) {END=true;break main_loop;}
                        }else{
                            sendErrorMessageC1("Expected PASS or TAKE command");
                            continue main_loop;
                        }
                    }
                    int points = player1game.getPoints();
                    if(!sendActionC2(new ServerPoints(CLIENT_2_ID,points))){END=true;break main_loop;}//We send PNTS to both players
                    if(!sendActionC1(new ServerPoints(CLIENT_1_ID,points))){END=true;break main_loop;}
                    if(first_turn == current_turn){
                        sessionState = CLIENT_2_PLAY;
                    }else{
                        if(player1game.getPoints() > player2game.getPoints()){//Wins player1
                            player1game.addGems(lobby_money);
                            if(!sendActionC1(new ServerWins((byte) 0))){END=true;break main_loop;}
                            if(!sendActionC1(new ServerWins((byte) 0))){END=true;break main_loop;}
                        }else if (player1game.getPoints() < player2game.getPoints()){//Wins player2
                            player2game.addGems(lobby_money);
                            if(!sendActionC1(new ServerWins((byte) 1))){END=true;break main_loop;}
                            if(!sendActionC1(new ServerWins((byte) 1))){END=true;break main_loop;}
                        }else{//Case of a tie
                            if(!sendActionC1(new ServerWins((byte) 2))){END=true;break main_loop;}
                            if(!sendActionC2(new ServerWins((byte) 2))){END=true;break main_loop;}
                        }
                        sessionState = LOBBY;
                    }
                    break;
                }case CLIENT_2_PLAY:{
                    player2game.reroll();
                    if(!sendActionC2(new ServerDice(CLIENT_2_ID,player2game.getDiceValues()))) {END=true;break main_loop;}
                    if(!sendActionC1(new ServerDice(CLIENT_1_ID,player2game.getDiceValues()))) {END=true;break main_loop;}
                    play_loop: while(player2game.newTurnAvailable()){
                        ca2 = receiveActionC2();
                        if(ca2 == null){END = true; break main_loop;}
                        if(ca2.command == ClientCommand.Pass){
                            if(!sendActionC1(new ServerPass(CLIENT_1_ID))) {END=true;break main_loop;}//Notify CLIENT_1 of PASS
                            break play_loop;
                        }else if(ca2.command == ClientCommand.Take){
                            byte[] take = ((ClientTake)ca2).diceIndexList;
                            if(!sendActionC1(new ServerTake(CLIENT_1_ID,take))) {END=true;break main_loop;}//Notify CLIENT_1 of TAKE
                            player2game.take(take);
                            player2game.reroll();
                            if(!sendActionC2(new ServerDice(CLIENT_2_ID,player2game.getDiceValues()))) {END=true;break main_loop;}//We send DICE to both players
                            if(!sendActionC1(new ServerDice(CLIENT_1_ID,player2game.getDiceValues()))) {END=true;break main_loop;}
                        }else{
                            sendErrorMessageC2("Expected PASS or TAKE command");
                            continue main_loop;
                        }
                    }
                    int points = player2game.getPoints();
                    if(!sendActionC2(new ServerPoints(CLIENT_2_ID,points))){END=true;break main_loop;}//We send PNTS to both players
                    if(!sendActionC1(new ServerPoints(CLIENT_1_ID,points))){END=true;break main_loop;}
                    if(first_turn == current_turn){
                        sessionState = CLIENT_1_PLAY;
                    }else{
                        if(player1game.getPoints() > player2game.getPoints()){//Wins player1
                            player1game.addGems(lobby_money);
                            if(!sendActionC1(new ServerWins((byte) 0))){END=true;break main_loop;}
                            if(!sendActionC2(new ServerWins((byte) 1))){END=true;break main_loop;}
                        }else if (player1game.getPoints() < player2game.getPoints()){//Wins player2
                            player2game.addGems(lobby_money);
                            if(!sendActionC1(new ServerWins((byte) 1))){END=true;break main_loop;}
                            if(!sendActionC2(new ServerWins((byte) 0))){END=true;break main_loop;}
                        }else{//Case of a tie
                            if(!sendActionC1(new ServerWins((byte) 2))){END=true;break main_loop;}
                            if(!sendActionC2(new ServerWins((byte) 2))){END=true;break main_loop;}
                        }
                        sessionState = LOBBY;
                    }
                    break;
                }
            }
        }
    }
    private void sendErrorMessageC1(String msg){
        try{
            ci1.sendErrorMessage(new ProtocolErrorMessage(msg));
        }catch(IOException e2){}
    }
    private void sendErrorMessageC2(String msg){
        try{
            ci2.sendErrorMessage(new ProtocolErrorMessage(msg));
        }catch(IOException e2){}
    }

    private ClientAction receiveActionC1(){
        System.out.println("Waiting action from client...");
        try {
            ClientAction ca = ci1.recieveClientAction();
            System.out.println(ca + " from "+client1Address);
            return ca;
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
        System.out.println("Waiting action from client...");
        try {
            ClientAction ca = ci2.recieveClientAction();
            System.out.println(ca + " from "+client2Address);
            return ca;
        } catch (IOException e) {
            System.out.println("Communication failed");
        } catch (ProtocolException e) {
            sendErrorMessageC2(e.getMessage());
        } catch (ProtocolErrorMessage e) {
            //sendErrorMessage(e.getMessage());
        }
        return null;
    }

    private boolean sendActionC1(ServerAction sa){
        try {
            ci1.sendServerAction(sa);
            System.out.println(sa + " to "+client1Address);
            return true;
        } catch (IOException e) {
            sendErrorMessageC1("Communication failed, could not send action");
            return false;
        }
    }

    private boolean sendActionC2(ServerAction sa){
        try {
            ci2.sendServerAction(sa);
            System.out.println(sa + " to "+client2Address);
            return true;
        } catch (IOException e) {
            sendErrorMessageC2("Communication failed, could not send action");
            return false;
        }
    }
}