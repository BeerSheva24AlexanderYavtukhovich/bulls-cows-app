package telran.games.db.jpa.exceptions;

public class GameFinishedException extends RuntimeException {
    
    public GameFinishedException(Long gameId) {
        super("Game with ID " + gameId + " has already finished.");
    }
}
