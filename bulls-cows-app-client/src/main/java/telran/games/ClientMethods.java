package telran.games;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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

    private void getMainMenu(InputOutput io) {
        Item[] mainMenuItems = {
                Item.of("Create Game", this::createGame),
                Item.of("Join Game", this::selectUnjoinedGames),
                Item.of("Start Game", this::selectUnstartedGame),
                Item.of("Play Game", this::selectGameToPlay),

        };
        mainMenuItems = addExitItem(mainMenuItems, netClient);
        Menu mainMenu = new Menu("Hello, " + username + "! Lets play Bulls & Cows", mainMenuItems);
        mainMenu.perform(io);
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

    private void selectGamesMenu(InputOutput io, String requestType,
            BiConsumer<List<Long>, InputOutput> menuAction) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", username);
        List<Long> gameIds = getGamesList(io, requestType);
        if (gameIds != null) {
            menuAction.accept(gameIds, io);
        }
    }

    private void selectGameToPlay(InputOutput io) {
        selectGamesMenu(io, "getGamesToPlay", this::getPlayGamesMenu);
    }

    private void selectUnstartedGame(InputOutput io) {
        selectGamesMenu(io, "getUnstartedGames", this::getUnstartedGamesMenu);
    }

    private void selectUnjoinedGames(InputOutput io) {
        selectGamesMenu(io, "getGamesToJoin", this::getGamestoJoinMenu);
    }

    private List<Long> getGamesList(InputOutput io, String methodName) {
        List<Long> gameIds = null;
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", username);
        String jsonResponse = netClient.sendAndReceive(methodName, requestJson.toString());
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray gamesArray = jsonObject.getJSONArray("games");
        if (gamesArray.isEmpty()) {
            printResponse(io, Params.NO_GAMES_FOUND);
        } else {
            gameIds = gamesArray.toList().stream()
                    .map(Object::toString)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }
        return gameIds;
    }

    private void getGamesMenu(String title, List<Long> gameIds, InputOutput io,
            BiConsumer<Long, InputOutput> gameAction) {
        Item[] gamesMenuItems = gameIds.stream()
                .map(gameId -> Item.of("Game ID: " + gameId, i -> gameAction.accept(gameId, i), true))
                .toArray(Item[]::new);
        gamesMenuItems = addExitItem(gamesMenuItems, netClient);
        Menu gamesMenu = new Menu(title, gamesMenuItems);
        gamesMenu.perform(io);
    }

    private void getGamestoJoinMenu(List<Long> gameIds, InputOutput io) {
        getGamesMenu("Select game to join", gameIds, io, this::joinSelectedGame);
    }

    private void getPlayGamesMenu(List<Long> gameIds, InputOutput io) {
        getGamesMenu("Select game to play", gameIds, io, this::playSelectedGame);
    }

    private void getUnstartedGamesMenu(List<Long> gameIds, InputOutput io) {
        getGamesMenu("Select game to start", gameIds, io, this::startSelectedGame);
    }

    private void startSelectedGame(Long gameId, InputOutput io) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", username);
        requestJson.put("gameId", gameId);
        String jsonResponse = netClient.sendAndReceive("startGame", requestJson.toString());
        printResponse(io, jsonResponse);
    }

    private void playSelectedGame(Long gameId, InputOutput io) {
        String sequence = io.readInt("Enter 4 numbers to perform move. ", "Wrong ID format (Example: 1234)")
                .toString();
        JSONObject requestJson = new JSONObject();
        requestJson.put("gameId", gameId);
        requestJson.put("username", username);
        requestJson.put("sequence", sequence);
        String jsonResponse = netClient.sendAndReceive("performMove", requestJson.toString());
        printGameResults(io, jsonResponse);
    }

    private void joinSelectedGame(Long gameId, InputOutput io) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", username);
        requestJson.put("gameId", gameId);
        String jsonResponse = netClient.sendAndReceive("joinGame", requestJson.toString());
        printResponse(io, jsonResponse);
    }

    private void printResponse(InputOutput io, String jsonResponse) {
        io.writeLine(Params.LINE);
        io.writeLine(jsonResponse);
        io.writeLine(Params.LINE);
    }

    public static void printGameResults(InputOutput io, String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray movesArray = jsonObject.getJSONArray("moves");
        io.writeLine("Moves:");
        for (int i = 0; i < movesArray.length(); i++) {
            JSONObject moveJson = movesArray.getJSONObject(i);
            int bulls = moveJson.getInt("bulls");
            int cows = moveJson.getInt("cows");
            String sequence = moveJson.getString("sequence");
            String gameGamer = moveJson.getString("gameGamer");
            io.writeLine("Move " + (i + 1) + ": bulls=" + bulls + ", cows=" + cows + ", sequence='" + sequence
                    + "', gameGamer='" + gameGamer + "'");
        }
        boolean isWin = jsonObject.getBoolean("isWin");

        io.writeLine(isWin
                ? "Congratulations! You are the winner!"
                : "Better luck next time. Keep trying!");
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
