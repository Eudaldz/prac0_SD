//Aquesta classe és el motor del Server.  Tindrà la maquina d'estats que llegirà i escriurà de ComunicacionInterface.
/*
public class ServerEngine implements Runnable {
    private ComunicationInterface ci;

    private GameState gState;

    private int coins; //TODO dynamic coins.

    private ClientAction nextTurn;


    public ServerEngine(ComunicationInterface ci){
        this.ci = ci;
    }
 
    public void run() {

        nextTurn = ci.recieveClientAction(); // TODO S'ha de capturar l'ID (per a la segona fase)
        gState = GameState.CSTART;

        do{
            switch(gState){
                case CNEXTTURN:
                    nextTurn =  ci.recieveClientAction();
                    // Pensant si cm codificar millor els estatss, ara per ara no es gaire elegant ni tant sols complet. 
                    // Vaig pensant't-hi.
                case CSTART:
                    ci.sendServerAction(new ServerCash(coins));
                    gState = GameState.CNEXTTURN;
                case CBETT:
                case CSHIP:
                case CCAPTAIN:
                case CCREW:
                case CPASS:
                case CTAKE:
                case CTURNENDED:
                //case ALLEND
            }
        }while (gState != GameState.ALLEND);
        //Code
        //ClientStart
        //ServerCash

        //ClientBett
        //ServerLoot




        //ClientPass
        //ServerPnts

        
    }

    //La màquina d'estats llegirà una linia de Comunication Interface i respondrà adequadament.




}*/
