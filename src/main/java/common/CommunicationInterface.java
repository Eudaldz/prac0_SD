package common;
import common.client_actions.ClientAction;
import common.server_actions.ServerAction;

import java.io.IOException;
import java.io.Closeable;

public interface CommunicationInterface extends Closeable, AutoCloseable{
    public void sendClientAction(ClientAction ca) throws IOException;
    public void sendServerAction(ServerAction sa) throws IOException;
    public void sendErrorMessage(ProtocolErrorMessage em) throws IOException;
    public ClientAction recieveClientAction() throws IOException, ProtocolException, ProtocolErrorMessage;
    public ServerAction recieveServerAction() throws IOException, ProtocolException, ProtocolErrorMessage;
}
