package telran.games;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import org.json.JSONObject;

import telran.games.repository.BullsCowsRepository;
import telran.games.service.BullsCowsService;
import telran.net.Protocol;
import telran.net.Request;
import telran.net.Response;
import telran.net.ResponseCode;

public class BullsCowsProtocol implements Protocol {

    private final BullsCowsService service;
    private final BullsCowsRepository repository;

    public BullsCowsProtocol(BullsCowsService service, BullsCowsRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @Override
    public Response getResponse(Request request) {
        String type = request.requestType();
        String data = request.requestData();
        Response response;
        try {
            Method method = BullsCowsProtocol.class.getDeclaredMethod(type, String.class);
            method.setAccessible(true);
            response = (Response) method.invoke(this, data);
        } catch (NoSuchMethodException e) {
            response = new Response(ResponseCode.WRONG_TYPE, type + " is wrong type");
        } catch (InvocationTargetException e) {
            Throwable causeExc = e.getCause();
            String message = causeExc == null ? e.getMessage() : causeExc.getMessage();
            response = new Response(ResponseCode.WRONG_DATA, "Error: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    Response getOkResponse(String responseData) {
        return new Response(ResponseCode.OK, responseData);
    }

    private Response isUserExists(String data) throws Exception {
        try {
            JSONObject requestObj = new JSONObject(data);
            String username = requestObj.getString("username");
            return getOkResponse(String.valueOf(repository.isUserExists(username)));
        } catch (Exception e) {
            return new Response(ResponseCode.WRONG_DATA, e.getMessage());
        }
    }

    private Response addUser(String data) throws Exception {
        try {
            JSONObject requestObj = new JSONObject(data);
            String username = requestObj.getString("username");
            LocalDate birthDate = LocalDate.parse(requestObj.getString("birthDate"));
            repository.addUser(username, birthDate);
            return getOkResponse(String.format("User %s added successfully", username));
        } catch (Exception e) {
            return new Response(ResponseCode.WRONG_DATA, e.getMessage());
        }
    }

    private Response createGame(String data) throws Exception {
        Long id = repository.createGame();
        return getOkResponse("Game #" + id + " created.");
    }

    private Response startGame(String data) throws Exception {
        try {
            JSONObject requestObj = new JSONObject(data);
            Long id = requestObj.getLong("gameId");
            String username = requestObj.getString("username");
            repository.startGame(id, username);
            return getOkResponse("Game started successfully");
        } catch (Exception e) {
            return new Response(ResponseCode.WRONG_DATA, e.getMessage());
        }

    }

    private Response joinGame(String data) throws Exception {
        try {
            JSONObject requestObj = new JSONObject(data);
            String username = requestObj.getString("username");
            Long gameId = requestObj.getLong("gameId");
            repository.joinGame(username, gameId);
            return getOkResponse("Game joined successfully");
        } catch (Exception e) {
            return new Response(ResponseCode.WRONG_DATA, e.getMessage());
        }
    }

    private Response getUnstartedGames(String data) throws Exception {
        try {
            JSONObject requestObj = new JSONObject(data);
            String username = requestObj.getString("username");
            return getOkResponse(
                    new JSONObject().put("games", repository.getUnstartedGames(username)).toString());

        } catch (Exception e) {
            return new Response(ResponseCode.WRONG_DATA, e.getMessage());
        }
    }

    private Response getGamesToJoin(String data) throws Exception {
        try {
            JSONObject requestObj = new JSONObject(data);
            String username = requestObj.getString("username");
            return getOkResponse(new JSONObject().put("games", repository.getGamesToJoin(username)).toString());
        } catch (Exception e) {
            return new Response(ResponseCode.WRONG_DATA, e.getMessage());
        }
    }

    private Response performMove(String data) throws Exception {
        try {
            JSONObject requestObj = new JSONObject(data);
            Long gameId = requestObj.getLong("gameId");
            String username = requestObj.getString("username");
            String moveStr = requestObj.getString("move");
            repository.performMove(gameId, username, moveStr);
            return getOkResponse("Move performed successfully");
        } catch (Exception e) {
            return new Response(ResponseCode.WRONG_DATA, e.getMessage());
        }
    }

}
