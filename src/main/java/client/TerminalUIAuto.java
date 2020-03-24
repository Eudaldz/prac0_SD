
package client;

import common.PlayerGame;
import common.client_actions.ClientAction;
import common.client_actions.ClientPass;
import common.client_actions.ClientTake;
import common.server_actions.ServerAction;
import common.server_actions.ServerCash;
import common.server_actions.ServerDice;
import java.util.Scanner;


public class TerminalUIAuto extends TerminalUI{
    
    PlayerGame client;
    
    public TerminalUIAuto(){
        this.client = new PlayerGame();
    }
    
    @Override
    public void showServerAction(ServerAction a, UserState state){
        switch(a.command){
            case Cash:{
                int gems = ((ServerCash)a).cash;
                client.setGems(gems);
                break;
            }
            case Dice:{
                client.setDice(((ServerDice)a).diceList );
                break;
            }
        }
        
        super.showServerAction(a, state);
    }
    
    @Override
    protected ClientAction lobbyQuery(){
        client.newGame();
        return super.lobbyQuery();
    }
    
    
    @Override
    protected ClientAction ingameQuery(){
        if(client.shouldPlayerPass()){
            return new ClientPass(currentId);
            
        }else{
            byte[] indx = client.takePlayerAuto();
            client.take(indx);
            for(int i = 0; i < indx.length; i++)indx[i]++;
            this.printDiceIndex(indx);
            return new ClientTake(currentId, indx);
        }
    }
}
