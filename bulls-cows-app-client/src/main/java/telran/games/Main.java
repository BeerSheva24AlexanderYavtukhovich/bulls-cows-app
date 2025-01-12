package telran.games;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

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

    public static Item[] addExitItem(Item[] items, NetworkClient netClient) {
        Item[] res = Arrays.copyOf(items, items.length + 1);
        res[items.length] = Item.of("Exit", io -> {
            try {
                if (netClient instanceof Closeable closeable) {
                    closeable.close();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }, true);
        return res;
    }
}
