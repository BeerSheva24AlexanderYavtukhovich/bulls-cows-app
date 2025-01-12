package telran.games.exceptions;

public class GameAlreadyStartedException extends RuntimeException {

    public GameAlreadyStartedException(Long id) {
        super("Game " + id.toString() + " already started");
    }
}