package server;

//Codificacio dels possibles estats del joc.
// CSTART, 

/**
 * Previous gamestate philosophy. What is a GameState for? With that in mind, how should we define a GameState?
 * A GameState is used to keep the Server/Client engine turning. Since theese two engines have different states there will be different Specifications for what a ServerGameState and ClientGameState is.
 * In both cases the GameState has to be able to uniquely define every possible snap of the game.
 * Therefore, a ServerGameState will be an enum with a self-explanatory lable for every possible ServerGame snap.
 * 
 * There is annother variable to specify the current ServerState. It is defiined by the ComunicationState and the GameState.
 * The GameState is a codification for every possible GameState (which is shared the same for the client, this way the server can keep up with the client and check to see if the client is trying to cheat)
 * The ComunicationState is a codification that constrians the possible datagram types a server can recieve in the current state it is in.
 * 
 * ServerGame snaps:
 * START, CLIENTHASBETT, CLIENTHASSHIP, CLIENTHASCAPTAIN, CLIENTHASCREW, CLIENTPASS,
 * 
 */

public enum ServerGameState{
    CSTART, CBETT, CSHIP, CCAPTAIN, CCREW, CPASS, CTAKE, ALLEND, CNEXTTURN, CTURNENDED;
}
