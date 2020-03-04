
package client;
import common.ProtocolErrorMessage;
import common.client_actions.ClientAction;
import common.client_actions.ClientStart;
import common.server_actions.ServerAction;

public interface UserInterface {
    
    public ClientAction queryUserAction();
    public void showServerAction(ServerAction a);
    public void showServerError(ProtocolErrorMessage e);
    public void showInputError(String msg);
    public void showInternalError(String msg);
}