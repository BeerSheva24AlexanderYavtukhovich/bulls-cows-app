package telran.games;

import static telran.games.ClientMethods.addExitItem;
import telran.net.NetworkClient;
import telran.net.TcpClient;
import telran.view.InputOutput;
import telran.view.Item;
import telran.view.Menu;
import telran.view.StandardInputOutput;

public class Main {
    static InputOutput io = new StandardInputOutput();
    static NetworkClient netClient = new TcpClient(Params.HOST, Params.PORT);

    public static void main(String[] args) {
        ClientMethods clientMethods = new ClientMethods(netClient);
        Item[] items = clientMethods.getItems();
        items = addExitItem(items, netClient);
        Menu menu = new Menu("Bulls & Cows Sign In", items);
        menu.perform(io);
        io.writeLine("Application is finished");
    }
}
