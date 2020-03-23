package common;

import common.DiceValue;

import java.util.*;
import server.InvalidActionException;


public class PlayerGame {
    private final int diceNum = 5;
    private DiceValue[] diceArray;
    private boolean[] taken;
    private int current_turn = 0;
    private int gems = 10;

    private Random rand = new Random();

    public PlayerGame(){
        diceArray = new DiceValue[5];
        taken = new boolean[5];
    }

    /**
     * We reroll those dices that are not yet reserved.
     */
    public void reroll(){
        for(int i = 0; i < diceNum; i++){
            if(!taken[i]){
                diceArray[i] = randomDice();
            }
        }        
    }
    
    public void take(byte[] indices){
        if(!checkIndices(indices)){
            return;
        }
        
        for(int i = 0; i < indices.length; i++){
            taken[indices[i]] = true;
        }
        current_turn += 1;
    }
    
    private boolean checkIndices(byte[] indices){
        if(indices.length > 5){
            return false;
        }
        for(int i = 0; i < indices.length; i++){
            if(indices[i] < 0 || indices[i] > 4)return false;
        }
        boolean[] newTaken = taken.clone();
        
        for(int i = 0; i < indices.length; i++){
            newTaken[indices[i]] = true;
        }
        
        boolean[] step = new boolean[]{false, false, false, false};
        for(int i = 0; i < 5; i++){
            if(newTaken[i]){
                if(diceArray[i] == DiceValue.Six && !step[0])step[0] = true;
                else if(diceArray[i] == DiceValue.Five && !step[1])step[1] = true;
                else if(diceArray[i] == DiceValue.Four && !step[2])step[2] = true;
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
    
    private DiceValue randomDice(){
        int r = rand.nextInt(6) + 1;
        return DiceValue.fromInt(r);
    }

    /**
     *
     * @return dice_array
     */
    public DiceValue[] getDiceValues(){
        return diceArray.clone();
    }

    public boolean newTurnAvailable(){
        return current_turn<2;
    }

    public boolean canPass(){
        boolean isSix = false, isFive=false,isFour=false;
        for (DiceValue d : diceArray){
            if (d.number==6)isSix=true;
            else if (d.number==5)isFive=true;
            else if(d.number==4)isFour=true;
        }
        return (isSix && isFive && isFour) && (current_turn<3);
    }


    /**
     * Returns the points.
     * @return
     */
    public int getPoints(){
        boolean isSix = false, isFive=false,isFour=false;
        int total_sum = 0;
        for (DiceValue d : diceArray){
            if (d.number==6)isSix=true;
            else if (d.number==5)isFive=true;
            else if(d.number==4)isFour=true;
            total_sum+=d.number;
        }
        if (isSix && isFive && isFour) return total_sum-15;
        else return 0;
    }

    public void addGems(int inc){
        this.gems += inc;
    }
    
    public boolean hasGems(){
        return gems > 0;
    }

    public int getGems(){
        return this.gems;
    }

    public void decreaseGems(int dec){this.gems -=dec;}

    /**
     * Restarts the game but not the gems.
     */
    public void newGame(){
        diceArray = new DiceValue[5];
        taken = new boolean[5];
        current_turn = 0;
    }
    
    
    public boolean shouldPlayerPass(){
        if(!canPass()){
            return false;
        }
        
        int points = getPoints();
        int halfPoints = 7;
        return points >= halfPoints;
    }
    
    public byte[] takePlayerAuto(){
        int minTaken = 7;
        ArrayList<Byte> newDiceIndex = new ArrayList<Byte>();
        for (int i = 0; i < 5; i++){
            DiceValue d = diceArray[i];
            if(taken[i]){
                if (d.number < minTaken)minTaken = d.number;
            }
            else{
                byte ind = (byte)i;
                newDiceIndex.add(ind);
            }
        }
        
        ArrayList<Byte> shouldTakeList = new ArrayList<Byte>();
        for(int reps = 0; reps < newDiceIndex.size(); reps++){
            for(byte idx: newDiceIndex){
                if(!shouldTakeList.contains(idx)){
                    DiceValue d = diceArray[idx];
                    if((minTaken <= 4 && d.number > 3) || d.number == minTaken-1){
                        shouldTakeList.add(idx);
                        if(d.number < minTaken)minTaken = d.number;
                    }
                }
            }
        }
        byte[] take_idx = new byte[shouldTakeList.size()];
        for(int i = 0; i < take_idx.length; i++){
            take_idx[i] = shouldTakeList.get(i);
        }
        return take_idx;
    }
    

    /**
     * For the server AI
     */
    
    /*
    public byte[] takeServerAI(){
        int minDice=7;

        ArrayList<Integer> shouldTakeList = new ArrayList<Integer>();

        if(!reserved_dice.isEmpty()) {
            for (DiceValue d : reserved_dice) {
                if (d.number < minDice) minDice = d.number;
            }
        }
        for (int time : not_reserved) {//We have to do the loop one time for each element in not_reserved to take into consideration cases like 4,5,6,3,2,1 where we have to take 4,5,6.
            for (int idx : not_reserved) {
                if (minDice == 4 && !shouldTakeList.contains(idx)) {
                    shouldTakeList.add(idx);
                } else if (dice_array.get(idx).number == minDice - 1 && !shouldTakeList.contains(idx)) {
                    shouldTakeList.add(idx);
                }
            }
        }
        try{
            reserve(shouldTakeList);
        } catch (InvalidActionException e) {
            e.printStackTrace();
        }
        return takeConversor(shouldTakeList);
    }*/
}

