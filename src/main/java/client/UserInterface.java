
package client;
import common.ProtocolErrorMessage;
import common.client_actions.ClientAction;
import common.server_actions.ServerAction;

public interface UserInterface {
    
    public void welcomeMessage();
    public void goodbyMessage();
    public ClientAction queryUserAction(UserState us);
    public void showServerAction(ServerAction a, UserState us);
    public void showServerError(ProtocolErrorMessage e);
    public void showInputError(String msg);
    public void showInternalError(String msg);
}