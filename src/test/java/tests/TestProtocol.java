/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import common.DiceValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import common.EloisProtocolComms;
import common.ProtocolErrorMessage;
import common.ProtocolException;
import common.client_actions.*;
import common.server_actions.*;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author eudald
 */
public class TestProtocol {
    
    /*
    *   CLIENT
    */
    public static final byte[] clientStartD1 = new byte[]{'S', 'T', 'R', 'T', ' ', 0x1, 0x2, 0x3, 0x4};
    public static final ClientStart clientStartO1 = new ClientStart(0x01020304);
    public static final byte[] clientStartDE1 = new byte[]{'S', 'T', 'R', 'T', 'X', 0x1, 0x1, 0x1, 0x1};
    
    public static final byte[] clientBettD1 = new byte[]{'B', 'E', 'T', 'T'};
    public static final ClientBett clientBettO1 = new ClientBett();
    
    public static final byte[] clientTakeD1 = new byte[]{'T', 'A', 'K', 'E', ' ', (byte)0x80, (byte)0x81, 0x0, 0x0, ' ', 0x5, 
        ' ', 0x1, ' ', 0x2, ' ', 0x3, ' ', 0x4, ' ', 0x5};
    public static final ClientTake clientTakeO1 = new ClientTake(0x80810000, new byte[]{1, 2, 3, 4, 5});
    public static final byte[] clientTakeD2 = new byte[]{'T', 'A', 'K', 'E', ' ', 0, 0, (byte)0x80, (byte)0x81, ' ', 0};
    public static final ClientTake clientTakeO2 = new ClientTake(0x80810000, new byte[]{});
    public static final byte[] clientTakeDE1 = new byte[]{'T', 'A', 'K', 'E', ' ', 0x1, 0x1, 0x1, 0x1, ' ', 32, 0};
    public static final byte[] clientTakeDE2 = new byte[]{'T', 'A', 'K', 'E', ' ', 0x1, 0x1, 0x1, 0x1, 'X', 32, 0};
    
    public static final byte[] clientPassD1 = new byte[]{'P', 'A', 'S', 'S', ' ', 0x1, 0x2, 0x3, 0x4};
    public static final ClientPass clientPassO1 = new ClientPass(0x01020304);
    
    public static final byte[] clientExitD1 = new byte[]{'E', 'X', 'I', 'T'};
    public static final ClientExit clientExitO1 = new ClientExit();
    
    /*
    *   SERVER
    */
    public static final byte[] serverCashD1 = new byte[]{'C', 'A', 'S', 'H', ' ', (byte)0xFF, (byte)0xFE, (byte)0xFD, (byte)0xFC};
    public static final ServerCash serverCashO1 = new ServerCash(0xFFFEFDFC);
    
    public static final byte[] serverLootD1 = new byte[]{'L', 'O', 'O', 'T', ' ', (byte)0xFC, (byte)0xFD, (byte)0xFE, (byte)0xFF};
    public static final ServerLoot serverLootO1 = new ServerLoot(0xFCFDFEFF);
    
    public static final byte[] serverPlayD1 = new byte[]{'P', 'L', 'A', 'Y', ' ', '0'};
    public static final ServerPlay serverPlayO1 = new ServerPlay(ServerPlay.CLIENT);
    public static final byte[] serverPlayD2 = new byte[]{'P', 'L', 'A', 'Y', ' ', '1'};
    public static final ServerPlay serverPlayO2 = new ServerPlay(ServerPlay.SERVER);
    public static final byte[] serverPlayDE1 = new byte[]{'P', 'L', 'A', 'Y', ' ', 'X'};
    
    public static final byte[] serverDiceD1 = new byte[]{'D', 'I', 'C', 'E', ' ', 0x70, 0x00, 0x00, 0x71, 
        ' ', '1', ' ', '5', ' ', '2', ' ', '4', ' ', '3' };
    public static final ServerDice serverDiceO1 = new ServerDice(0x70000071, 
            new DiceValue[]{DiceValue.One, DiceValue.Five, DiceValue.Two, DiceValue.Four, DiceValue.Three});
    public static final byte[] serverDiceD2 = new byte[]{'D', 'I', 'C', 'E', ' ', 0x70, 0x00, 0x00, 0x00, 
        ' ', '6', ' ', '6', ' ', '6', ' ', '6', ' ', '6' };
    public static final ServerDice serverDiceO2 = new ServerDice(0x70000000, 
            new DiceValue[]{DiceValue.Six, DiceValue.Six, DiceValue.Six, DiceValue.Six, DiceValue.Six});
    public static final byte[] serverDiceDE1 = new byte[]{'D', 'I', 'C', 'E', ' ', 0x70, 0x00, 0x00, 0x71, 
        'X', '1', ' ', '5', ' ', '2', ' ', '5', ' ', '3' };
    public static final byte[] serverDiceDE2 = new byte[]{'D', 'I', 'C', 'E', 'X', 0x70, 0x00, 0x00, 0x71, 
        ' ', '0', ' ', 'X', ' ', '2', ' ', '5', ' ', '3' };
    public static final byte[] serverDiceDE3 = new byte[]{'D', 'I', 'C', 'E', ' ', 0x70, 0x00, 0x00, 0x71, 
        ' ', '1', 'X', '5', ' ', '2', ' ', '5', ' ', '3' };
    
    public static final byte[] serverTakeD1 = new byte[]{'T', 'A', 'K', 'E', ' ', (byte)0x80, (byte)0x81, 0x0, 0x0, ' ', 5, 
        ' ', 1, ' ', 1, ' ', 5, ' ', 5, ' ', 5};
    public static final ServerTake serverTakeO1 = new ServerTake(0x80810000, new byte[]{1, 1, 5, 5, 5});
    public static final byte[] serverTakeD2 = new byte[]{'T', 'A', 'K', 'E', ' ', 0, 0, (byte)0x80, (byte)0x81, ' ', 0};
    public static final ServerTake serverTakeO2 = new ServerTake(0x00008081, new byte[]{});
    public static final byte[] serverTakeDE1 = new byte[]{'T', 'A', 'K', 'E', ' ', 0x1, 0x1, 0x1, 0x1, ' ', 32, 0};
    public static final byte[] serverTakeDE2 = new byte[]{'T', 'A', 'K', 'E', ' ', 0x1, 0x1, 0x1, 0x1, 'X', 32, 0};
    
    public static final byte[] serverPassD1 = new byte[]{'P', 'A', 'S', 'S', ' ', 0x10, 0x20, 0x30, 0x40};
    public static final ServerPass serverPassO1 = new ServerPass(0x10203040);
    
    public static final byte[] serverPointsD1 = new byte[]{'P', 'N', 'T', 'S', ' ', 0x1, 0x2, 0x3, 0x4, ' ', (byte)0xFF};
    public static final ServerPoints serverPointsO1 = new ServerPoints(0x01020304, (byte)0xFF);
    public static final byte[] serverPointsDE1 = new byte[]{'P', 'N', 'T', 'S', ' ', 0x1, 0x2, 0x3, 0x4, 'X', (byte)0xFF};
    
    public static final byte[] serverWinsD1 = new byte[]{'W', 'I', 'N', 'S', ' ', '0'};
    public static final ServerWins serverWinsO1 = new ServerWins(ServerWins.CLIENT);
    public static final byte[] serverWinsD2 = new byte[]{'W', 'I', 'N', 'S', ' ', '1'};
    public static final ServerWins serverWinsO2 = new ServerWins(ServerWins.SERVER);
    public static final byte[] serverWinsD3 = new byte[]{'W', 'I', 'N', 'S', ' ', '2'};
    public static final ServerWins serverWinsO3 = new ServerWins(ServerWins.TIE);
    public static final byte[] serverWinsDE1 = new byte[]{'W', 'I', 'N', 'S', ' ', 'X'};
    
    
    
    private EloisProtocolComms comms;
    private ByteArrayOutputStream os;
    private ByteArrayInputStream is;
    
    
    public TestProtocol() {
        
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
    }
    
    @After
    public void tearDown() {
    }

    
    /*
     * SEND CLIENT TESTS
     */
    
    @Test
    public void sendClientStart()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendClientAction(clientStartO1);
        assertArrayEquals(clientStartD1, os.toByteArray());
    }
    
    @Test
    public void sendClientBett()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendClientAction(clientBettO1);
        assertArrayEquals(clientBettD1, os.toByteArray());
    }
    
    @Test
    public void sendClientTake()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendClientAction(clientTakeO1);
        assertArrayEquals(clientTakeD1, os.toByteArray());
        os.reset();
        comms.sendClientAction(clientTakeO2);
        assertArrayEquals(clientTakeD2, os.toByteArray());
    }
    
    @Test
    public void sendClientPass()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendClientAction(clientPassO1);
        assertArrayEquals(clientPassD1, os.toByteArray());
    }
    
    @Test
    public void sendClientExit()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendClientAction(clientExitO1);
        assertArrayEquals(clientExitD1, os.toByteArray());
    }
    
    
    /*
     * SEND SERVER TESTS
     */
   
    @Test
    public void sendServerCash()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendServerAction(serverCashO1);
        assertArrayEquals(serverCashD1, os.toByteArray());
    }
    
    @Test
    public void sendServerLoot()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendServerAction(serverLootO1);
        assertArrayEquals(serverLootD1, os.toByteArray());
    }
    
    @Test
    public void sendServerPlay()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendServerAction(serverPlayO1);
        assertArrayEquals(serverPlayD1, os.toByteArray());
        os.reset();
        comms.sendServerAction(serverPlayO2);
        assertArrayEquals(serverPlayD2, os.toByteArray());
    }
    
    @Test
    public void sendServerDice()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendServerAction(serverDiceO1);
        assertArrayEquals(serverDiceD1, os.toByteArray());
        os.reset();
        comms.sendServerAction(serverDiceO2);
        assertArrayEquals(serverDiceD2, os.toByteArray());
    }
    
    @Test
    public void sendServerTake()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendServerAction(serverTakeO1);
        assertArrayEquals(serverTakeD1, os.toByteArray());
        os.reset();
        comms.sendServerAction(serverTakeO2);
        assertArrayEquals(serverTakeD2, os.toByteArray());
    }
    
    @Test
    public void sendServerPass()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendServerAction(serverPassO1);
        assertArrayEquals(serverPassD1, os.toByteArray());
    }
    
    @Test
    public void sendServerPoints()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendServerAction(serverPointsO1);
        assertArrayEquals(serverPointsD1, os.toByteArray());
    }
    
    @Test
    public void sendServerWins()throws IOException{
        os = new ByteArrayOutputStream(32);
        comms = new EloisProtocolComms(null, os);
        comms.sendServerAction(serverWinsO1);
        assertArrayEquals(serverWinsD1, os.toByteArray());
        os.reset();
        comms.sendServerAction(serverWinsO2);
        assertArrayEquals(serverWinsD2, os.toByteArray());
        os.reset();
        comms.sendServerAction(serverWinsO3);
        assertArrayEquals(serverWinsD3, os.toByteArray());
    }
    
    
    /*
     * RECIEVE CLIENT TESTS
     */
    
    @Test
    public void recieveClientStart()throws IOException, ProtocolException, ProtocolErrorMessage{
        byte[] msg = clientStartD1.clone();
        is = new ByteArrayInputStream(msg);
        comms = new EloisProtocolComms(is, null);
        ClientAction a = comms.recieveClientAction();
        assertEquals(clientStartO1, a);
        
        msg = clientStartDE1.clone();
        is = new ByteArrayInputStream(msg);
        comms = new EloisProtocolComms(is, null);
        try{
            a = comms.recieveClientAction();
            fail("Expected protocol exception");
        }catch(ProtocolException pe){
            
        }
    }
    
    @Test
    public void recieveClientBett()throws IOException, ProtocolException, ProtocolErrorMessage{
        byte[] msg = clientBettD1.clone();
        is = new ByteArrayInputStream(msg);
        comms = new EloisProtocolComms(is, null);
        ClientAction a = comms.recieveClientAction();
        assertEquals(clientBettO1, a);
    }
    
    @Test
    public void recieveClientTake()throws IOException, ProtocolException, ProtocolErrorMessage{
        byte[] msg = clientTakeD1.clone();
        is = new ByteArrayInputStream(msg);
        comms = new EloisProtocolComms(is, null);
        ClientAction a = comms.recieveClientAction();
        assertEquals(clientTakeO1, a);
        
        msg = clientTakeD2.clone();
        is = new ByteArrayInputStream(msg);
        comms = new EloisProtocolComms(is, null);
        a = comms.recieveClientAction();
        assertEquals(clientTakeO2, a);
        
        msg = clientTakeDE1.clone();
        is = new ByteArrayInputStream(msg);
        comms = new EloisProtocolComms(is, null);
        try{
            a = comms.recieveClientAction();
            fail();
        }catch(ProtocolException e){}
        
        msg = clientTakeDE2.clone();
        is = new ByteArrayInputStream(msg);
        comms = new EloisProtocolComms(is, null);
        try{
            a = comms.recieveClientAction();
            fail();
        }catch(ProtocolException e){}
    }
    
    @Test
    public void recieveClientPass()throws IOException, ProtocolException, ProtocolErrorMessage{
        byte[] msg = clientPassD1.clone();
        is = new ByteArrayInputStream(msg);
        comms = new EloisProtocolComms(is, null);
        ClientAction a = comms.recieveClientAction();
        assertEquals(clientPassO1, a);
    }
    
    @Test
    public void recieveClientExit()throws IOException, ProtocolException, ProtocolErrorMessage{
        byte[] msg = clientExitD1.clone();
        is = new ByteArrayInputStream(msg);
        comms = new EloisProtocolComms(is, null);
        ClientAction a = comms.recieveClientAction();
        assertEquals(clientExitO1, a);
    }
    
    
    /*
     * RECIEVE SERVER TESTS
     */
    
    @Test
    public void recieveServerCash()throws IOException, ProtocolException, ProtocolErrorMessage{
        
    }
    
    @Test
    public void recieveServerLoot()throws IOException, ProtocolException, ProtocolErrorMessage{
        
    }
    
    @Test
    public void recieveServerPlay()throws IOException, ProtocolException, ProtocolErrorMessage{
        
    }
    
    @Test
    public void recieveServerDice()throws IOException, ProtocolException, ProtocolErrorMessage{
        
    }
    
    @Test
    public void recieveServerTake()throws IOException, ProtocolException, ProtocolErrorMessage{
        
    }
    
    @Test
    public void recieveServerPass()throws IOException, ProtocolException, ProtocolErrorMessage{
        
    }
    
    @Test
    public void recieveServerPoints()throws IOException, ProtocolException, ProtocolErrorMessage{
        
    }
    
    @Test
    public void recieveServerWins()throws IOException, ProtocolException, ProtocolErrorMessage{
        
    }
}
