package client;

import common.DiceValue;
import common.ProtocolErrorMessage;
import common.server_actions.*;
import common.client_actions.*;
import java.util.Arrays;
import java.util.Scanner;


public class TerminalUI implements UserInterface{

    private Scanner sc;
    private int currentId;
    
    public TerminalUI(){
        sc = new Scanner(System.in);
        currentId = 0;
    }
    
    public ClientAction queryUserAction(UserState state) {
        switch(state){
            case START:
                printStartMessage();
                return startQuery();
            case LOBBY:
                printLobbyMessage();
                return lobbyQuery();
            case INGAME:
                printIngameMessage();
                return ingameQuery();
        }
        return null;
    }

    public void showServerAction(ServerAction a, UserState state) {
        switch(a.command){
            case Cash:{
                System.out.println("\n *TOTAL MONEY*: "+((ServerCash)a).cash );
                break;
            }
            case Loot:{
                System.out.println("\n *MONEY AT STAKE*: "+((ServerLoot)a).coins );
                break;
            }
            case Play:{
                if(((ServerPlay)a).value == ServerPlay.CLIENT)System.out.println("\n *YOU BEGIN*");
                else System.out.println("\n *SERVER BEGINS*");
                break;
            }
            case Dice:{
                int id = ((ServerDice)a).id;
                if(id == currentId)System.out.println("\n *YOUR ROLL*: ");
                else System.out.println("\n *SERVER ROLL*: ");
                printDiceList(((ServerDice)a).diceList);
                break;
            }
            case Take:{
                System.out.println("\n *SERVER TAKE*: ");
                printDiceIndex(((ServerTake)a).diceIndexList);
                break;
            }
            case Pass:{
                System.out.println("\n *SERVER PASS*: ");
                break;
            }
            case Points:{
                int id = ((ServerPoints)a).id;
                if(id == currentId)System.out.print("\n *YOUR GAINED POINTS*: ");
                else System.out.print("\n *SERVER GAINED POINTS*: ");
                System.out.println(((ServerPoints)a).points);
                break;
            }
            case Wins:{
                if(((ServerWins)a).value == ServerWins.CLIENT) System.out.println("\n *YOU WIN*");
                else if(((ServerWins)a).value == ServerWins.SERVER) System.out.println("\n *SERVER WINS*");
                else System.out.println("\n *TIE, NO ONE WINS*");
                break;
            }
                
        }
    }
    

    public void showServerError(ProtocolErrorMessage e) {
        System.out.println("\n***SERVER ERROR MESSAGE***: "+e.getMessage());
    }

    public void showInputError(String msg) {
        System.out.println("\n*Command error*: "+msg);
    }

    public void showInternalError(String msg) {
        System.out.println("\n***FATAL ERROR**: "+msg);
    }
    
    private void printStartMessage(){
        
        System.out.println("Please login...");
    }
    
    private void printLobbyMessage(){
        System.out.println("\nTo start a new game, bet some money...");
    }
    
    private void printIngameMessage(){
        System.out.println("\nYour turn...");
    }
    
    private ClientAction startQuery(){
        while(true){
            System.out.print("> ");
            Scanner lsc = new Scanner(sc.nextLine());
            String key = lsc.next();
            switch(key){
                case "STRT":
                    if(!lsc.hasNext())break;
                    String param = lsc.next();
                    if(param.matches("\\d+")){
                        int id = Integer.parseInt(param);
                        currentId = id;
                        return new ClientStart(id);
                    }
                    break;
                case "EXIT":
                    return new ClientExit();
            }
            System.out.println("Wrong input, try again."); 
        }
    }
    
    private ClientAction lobbyQuery(){
        while(true){
            System.out.print("> ");
            Scanner lsc = new Scanner(sc.nextLine());
            String key = lsc.next();
            switch(key){
                case "BETT":
                    return new ClientBett();
                case "EXIT":
                    return new ClientExit();
            }
            System.out.println("Wrong input, try again."); 
        }
    }
    
    private ClientAction ingameQuery(){
        while(true){
            System.out.print("> ");
            Scanner lsc = new Scanner(sc.nextLine());
            String key = lsc.next();
            s: switch(key){
                case "TAKE":
                    byte[] dl = new byte[5];
                    int i = 0;
                    while(lsc.hasNextByte() && i < 5){
                        byte v = lsc.nextByte();
                        if(v < 1 || v > 5){
                            break s;
                        }
                        dl[i] = v;
                        i++;
                    }
                    byte[] diceList = new byte[i]; 
                    System.arraycopy(dl, 0, diceList, 0, i);
                    return new ClientTake(currentId, diceList);
                
                case "PASS":
                    return new ClientPass(currentId);
                
                case "EXIT":
                    return new ClientExit();
            }
            System.out.println("Wrong input, try again."); 
        }
    }
    
    
    private void printDiceList(DiceValue[] dl){
        System.out.println(Arrays.toString(dl));
    }
    
    private void printDiceIndex(byte[] di){
        System.out.println(Arrays.toString(di));
    }

    @Override
    public void welcomeMessage() {
        System.out.println("\nWelcome to \"Ship, Captain & Crew\" game paltform.");
    }

    @Override
    public void goodbyMessage() {
        System.out.println("\nYou have been disconnected");
    }
    
}
