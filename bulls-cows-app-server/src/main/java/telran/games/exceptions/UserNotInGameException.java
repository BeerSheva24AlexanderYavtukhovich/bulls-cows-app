package telran.games.exceptions;

public class UserNotInGameException extends RuntimeException {
    public UserNotInGameException(String username, Long gameId) {
        super("User " + username + " is not part of the game with ID " + gameId + ".");
    }
}
