package telran.games.exceptions;

public class GamerAlreadyInGameException extends RuntimeException {

    public GamerAlreadyInGameException(String username, Long gameId) {
        super("Gamer " + username + " already in game " + gameId);
    }

}
