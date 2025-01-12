package telran.games;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    private void getGames(InputOutput io, String command, String noGamesMessage, String gamesTitle) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", username);
        String jsonResponse = netClient.sendAndReceive(command, requestJson.toString());

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray gamesArray = jsonObject.getJSONArray("games");
            if (gamesArray.isEmpty()) {
                io.writeLine(noGamesMessage);
            } else {
                io.writeLine(ClientConfig.LINE);
                io.writeLine(gamesTitle);
                gamesArray.toList().stream()
                        .map(Object::toString)
                        .map(Long::valueOf)
                        .forEach(gameId -> io.writeLine("- Game ID: " + gameId));
                io.writeLine(ClientConfig.LINE);
            }
        } catch (Exception e) {
            io.writeLine("Error processing response: " + e.getMessage());
        }
    }

    private void getUnstartedGamesWithoutUser(InputOutput io) {
        getGames(io, "getUnstartedGamesWithoutUser", "No unstarted games found.", "Unstarted games:");
    }

    private void getGamesToJoin(InputOutput io) {
        getGames(io, "getGamesToJoin", "No unstarted games found.", "Unstarted games:");
    }

    private boolean isValidUniqueDigits(String sequence) {
        boolean res;
        if (sequence == null || sequence.length() == 0) {
            res = false;
        }

        if (!sequence.matches("\\d+")) {
            res = false;
        }

        Set<Character> uniqueDigits = new HashSet<>();
        for (char digit : sequence.toCharArray()) {
            uniqueDigits.add(digit);
        }
        res = uniqueDigits.size() == sequence.length();
        return res;
    }

    private void printResponse(InputOutput io, String jsonResponse) {
        io.writeLine(ClientConfig.LINE);
        io.writeLine(jsonResponse);
        io.writeLine(ClientConfig.LINE);
    }

    private void getMainMenu(InputOutput io) {
        Item[] mainMenuItems = {
                Item.of("Create New Game", this::createGame),
                Item.of("Join Game", this::joinGame),
                Item.of("Start Game", this::startGame),
                Item.of("View Unstarted Games Available to Join",
                        this::getGamesToJoin),
                Item.of("View Unstarted Games", this::getUnstartedGamesWithoutUser),

        };
        mainMenuItems = Main.addExitItem(mainMenuItems, netClient);
        Menu mainMenu = new Menu("Hello, " + username + "! Lets play Bulls & Cows", mainMenuItems);
        mainMenu.perform(io);
    }

}
