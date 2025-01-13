package telran.games.db.jpa.exceptions;

public class UserNotInGameException extends RuntimeException {
    public UserNotInGameException(String username, Long gameId) {
        super("User " + username + " is not part of the game with ID " + gameId + ".");
    }
}
