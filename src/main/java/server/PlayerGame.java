
package server;

import common.DiceValue;

import java.util.*;

/**
 * This class represents the game variables and actions a player can do in a game. And gives tools to check whether the actions are legal or not.
 */
public class PlayerGame {
    ArrayList<DiceValue> dice_array;

    private ArrayList<DiceValue> reserved_dice;

    private ArrayList<Integer> not_reserved;
    private int current_turn;
    private int gems = 100;

    private Random rand = new Random();

    public PlayerGame(){
        dice_array = new ArrayList<DiceValue>();
        reserved_dice = new ArrayList<>();
        not_reserved = new ArrayList<Integer>();
        not_reserved.add(0);
        not_reserved.add(1);
        not_reserved.add(2);
        not_reserved.add(3);
        not_reserved.add(4);
        current_turn=0;
        roll_first();
    }

    /**
     * Restarts the game but not the gems.
     */
    public void newGame(){
        dice_array = new ArrayList<DiceValue>();
        reserved_dice = new ArrayList<>();
        not_reserved = new ArrayList<Integer>();
        not_reserved.add(0);
        not_reserved.add(1);
        not_reserved.add(2);
        not_reserved.add(3);
        not_reserved.add(4);
        current_turn=0;
        roll_first();
    }

    /**
     * First roll, we populate the dice_array
     */
    public void roll_first(){
        for (int i=0; i<5; i++){
            dice_array.add(DiceValue.fromInt(rand.nextInt(5)+1));
        }
        current_turn+=1;
    }

    /**
     * We reroll those dices that are not yet reserved.
     */
    public void roll() throws InvalidActionException{
        if (current_turn==3){
            throw new InvalidActionException("Maximum number of rolls exceded");
        }else {
            for (int index : not_reserved) {
                dice_array.set(index, DiceValue.fromInt(rand.nextInt(5) + 1));
            }
            current_turn += 1;
        }
    }

    /**
     *
     * @return dice_array
     */
    public DiceValue[] getDiceValues(){
        DiceValue[] d_arr = new DiceValue[dice_array.size()];
        return dice_array.toArray(d_arr);
    }

    public boolean newTurnAvailable(){
        return current_turn<3;
    }

    /**
     * Given an index array we reserve those dices. We must check the dices are reserved only when meeting with the game rules.
     * @param idx_taken_dice
     * @throws InvalidActionException
     */
    public void reserve(ArrayList<Integer> idx_taken_dice) throws InvalidActionException {
        ArrayList<Integer> not_reserved_copy = (ArrayList<Integer>) not_reserved.clone();
        ArrayList<DiceValue> reserved_dice_copy = (ArrayList<DiceValue>) reserved_dice.clone();

        for (int idx : idx_taken_dice){
            not_reserved_copy.remove(not_reserved_copy.indexOf(idx));
            reserved_dice_copy.add(dice_array.get(idx));
        }
        if (checkReservedDice(reserved_dice_copy)){
            not_reserved=not_reserved_copy;
            reserved_dice=reserved_dice_copy;
        }else{
            throw new InvalidActionException("Cannot reserve such dices");
        }
    }


    public void reserve(byte[] idx_taken_dice) throws InvalidActionException {
        reserve(takeConversor(idx_taken_dice));
    }



    /**
     * This function checks that we are not reserving a 4 without a 5 and not a 5 without a 6 and not a random dice without the 654 sequence already reserved.
     */
    private boolean checkReservedDice(ArrayList<DiceValue> dicesToCheck){
        boolean isSix = false, isFive=false,isFour=false;
        for (DiceValue d : dicesToCheck){
            if (d.number==6)isSix=true;
            else if (d.number==5)isFive=true;
            else if(d.number==4)isFour=true;
        }
        return isSix || (isSix && isFive) || (isSix && isFive && isFour);
    }

    public boolean canPass(){
        boolean isSix = false, isFive=false,isFour=false;
        for (DiceValue d : dice_array){
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
        for (DiceValue d : dice_array){
            if (d.number==6)isSix=true;
            else if (d.number==5)isFive=true;
            else if(d.number==4)isFour=true;
            total_sum+=d.number;
        }
        if (isSix && isFive && isFour) return total_sum-15;
        else return 0;
    }

    /**
     *
     * @param gems
     */
    public void updateGems(int gems){
        this.gems=gems;
    }

    public int getGems(){
        return this.gems;
    }


    /**
     * For the server AI
     */
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
    }

    public boolean shouldTakeServerAI() {
        int minDice = 7;

        ArrayList<Integer> shouldTakeList = new ArrayList<Integer>();

        if (!reserved_dice.isEmpty()) {
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
        return !shouldTakeList.isEmpty();
    }

    private ArrayList<Integer> takeConversor(byte[] dice_values) {
        ArrayList<Integer> darray = new ArrayList<Integer>();
        for (byte b : dice_values){
            darray.add((int)b);
        }
        return darray;
    }

    private byte[] takeConversor(ArrayList<Integer> myArr){
        byte[] myByte = new byte[myArr.size()];
        for (int i=0; i<myArr.size();i++){
            myByte[i]=(myArr.get(0)).byteValue();
        }
        return myByte;
    }
}

