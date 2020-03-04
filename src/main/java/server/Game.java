package server;

import java.util.*;


private class Game{
    ArrayList<DiceValue> rolled_dice = new ArrayList<DiceValue>();
    ArrayList<DiceValue> reserved_dice = new ArrayList<DiceValue>();
    
    private Random rand = new Random();

    public Game(){
        roll();
    }

    public roll(){
        int num_roll = getRollableDiceCount();
        for (int i=0; i<num_roll; 1++){
            rolled_dice.add(DiceValue.fromInt(rand.nextInt(5)+1));
        }
    }

    public ArrayList<DiceValue> showDices(){
        return (new ArrayList<DiceValue>().addAll(reserved_dice)).addAll(rolled_dice);
    }
    
    public void reserve(ArrayList<int> idx_taken_dice) throws InvalidActionException{
        ArrayList<DiceValue> auxDiceList = new ArrayList<DiceValue>;

        for (int i : idx_taken_dice){
            i_rolled = i -(reserved_dice.size()-1);
            if (i>=0){
                auxDiceList.add(rolled_dice[i_rolled]);
            }
        }

        while(auxDiceList.size>0){
            DiceValue iter_max = Collections.max(auxDiceList);
            reserveDice(iter_max);
            auxDiceList.remove(auxDiceList.indexOf(iter_max));
        }
    }

    private void reserveDice(DiceValue d_value) throws InvalidActionException{
        int maxAuxDice = d_value.number;

        if(reserved_dice.size()==0){
            if(maxAuxDice==6){
                reserved_dice.add(d_value);
            }else{
                throw new InvalidActionException("NO ES POT FER TAKE D'AQUEST DAU.");
            }
        }
        else{
            int minReservedDice = reserved_dice.min().number;
            if(minReservedDice-maxAuxDice != 1){
                throw new InvalidActionException("NO ES POT FER TAKE D'AQUEST DAU.");
            }else{
                reserved_dice.add(d_value);
            }

        }

    }

    private int getRollableDiceCount(){
        return 5-reserved_dice.size();
    }
}

