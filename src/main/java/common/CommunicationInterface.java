package common;
import common.client_actions.*;
import common.server_actions.*;

public interface CommunicationProtocol{
    public void sendClientAction(ClientAction ca);
    public void sendServerAction(ServerAction sa);
    public ClientAction recieveClientAction();
    public ServerAction recieveServerAction();
}
