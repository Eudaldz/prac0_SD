package common;
import common.client_actions.*;
import common.server_actions.*;

public interface CommunicationProtocol{
    public void sendClientAction(ClientAction ca) throws IOException;
    public void sendServerAction(ServerAction sa) throws IOException;
    public ClientAction recieveClientAction() throws IOException;
    public ServerAction recieveServerAction() throws IOException;
}
