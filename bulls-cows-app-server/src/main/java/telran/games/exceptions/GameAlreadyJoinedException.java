package telran.games.exceptions;

public class GameAlreadyJoinedException extends RuntimeException {
    public GameAlreadyJoinedException(Long gameId, String username) {
        super("Game with ID " + gameId + " has already been joined by " + username);
    }

}
