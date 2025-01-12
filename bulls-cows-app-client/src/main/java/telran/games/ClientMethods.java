package telran.games;

import java.time.LocalDate;

import org.json.JSONArray;
import org.json.JSONObject;

import telran.net.NetworkClient;
import telran.view.InputOutput;
import telran.view.Item;
import telran.view.Menu;

public class ClientMethods {

    private final NetworkClient netClient;
    private String username;

    public ClientMethods(NetworkClient netClient) {
        this.netClient = netClient;

    }

    public Item[] getItems() {
        return new Item[] {
                Item.of("Sign In", this::signIn),
                Item.of("Sign Up", this::signUp)
        };
    }

    private void signIn(InputOutput io) {
        username = io.readString("Enter your login");
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", username);
        String jsonResponse = netClient.sendAndReceive("isUserExists", requestJson.toString());
        if (jsonResponse.equals("false")) {
            io.writeLine("User not found. Please sign up");
        } else {
            getMainMenu(io);
        }
    }

    private void signUp(InputOutput io) {
        username = io.readString("Enter your login");
        LocalDate birthDate = io.readIsoDate("Enter your birthdate in format yyyy-MM-dd",
                "Wrong date format (yyyy-MM-dd)");
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", username);
        requestJson.put("birthDate", birthDate.toString());
        String jsonResponse = netClient.sendAndReceive("addUser", requestJson.toString());
        io.writeLine(jsonResponse);
        getMainMenu(io);
    }

    private void createGame(InputOutput io) {
        String jsonResponse = netClient.sendAndReceive("createGame", "");
        printResponse(io, jsonResponse);
    }

    private void joinGame(InputOutput io) {
        String id = io.readInt("Enter ID of the existing game. ", "Wrong ID format (Example: 123)")
                .toString();
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", username);
        requestJson.put("gameId", id);
        String jsonResponse = netClient.sendAndReceive("joinGame", requestJson.toString());
        printResponse(io, jsonResponse);
    }

    private void startGame(InputOutput io) {
        String id = io.readInt("Enter ID of the existing game. ", "Wrong ID format (Example: 123)")
                .toString();
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", username);
        requestJson.put("gameId", id);
        String jsonResponse = netClient.sendAndReceive("startGame", requestJson.toString());
        printResponse(io, jsonResponse);
    }

 private void performMove(InputOutput){
    String sequence = io.readInt("Enter 4 numbers. ", "Wrong ID format (Example: 1234)")
                .toString();
    JSONObject requestJson = new JSONObject();
    requestJson.put("username", username);
 }

    private void getUnstartedGames(InputOutput io) {
        sendReceiveProcessListOfGames(io, "getUnstartedGames");
    }

    private void getGamesToJoin(InputOutput io) {
        sendReceiveProcessListOfGames(io, "getGamesToJoin");
    }

    private void sendReceiveProcessListOfGames(InputOutput io, String methodName) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", username);
        String jsonResponse = netClient.sendAndReceive(methodName, requestJson.toString());
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray gamesArray = jsonObject.getJSONArray("games");

        if (gamesArray.isEmpty()) {
            printResponse(io, Params.NO_GAMES_FOUND);
        } else {
            io.writeLine(Params.LINE);
            gamesArray.toList().stream()
                    .map(Object::toString)
                    .map(Long::valueOf)
                    .forEach(gameId -> io.writeLine("- Game ID: " + gameId));
            io.writeLine(Params.LINE);
        }
    }

    private void printResponse(InputOutput io, String jsonResponse) {
        io.writeLine(Params.LINE);
        io.writeLine(jsonResponse);
        io.writeLine(Params.LINE);
    }

    private void getMainMenu(InputOutput io) {
        Item[] mainMenuItems = {
                Item.of("Create New Game", this::createGame),
                Item.of("Join Game", this::joinGame),
                Item.of("Start Game", this::startGame),
                Item.of("Make Move", this::performMove),
                Item.of("View Games Available to Join",
                        this::getGamesToJoin),
                Item.of("View Unstarted Games", this::getUnstartedGames),

        };
        mainMenuItems = Main.addExitItem(mainMenuItems, netClient);
        Menu mainMenu = new Menu("Hello, " + username + "! Lets play Bulls & Cows", mainMenuItems);
        mainMenu.perform(io);
    }

}
