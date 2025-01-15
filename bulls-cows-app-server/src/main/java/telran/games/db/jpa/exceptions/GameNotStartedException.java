package telran.games.db.jpa.exceptions;

public class GameNotStartedException extends RuntimeException {
    
    public GameNotStartedException(Long gameId) {
        super("Game with ID " + gameId + " has not started yet.");
    }
}
