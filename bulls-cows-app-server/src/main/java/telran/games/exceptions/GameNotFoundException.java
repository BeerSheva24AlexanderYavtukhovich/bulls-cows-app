package telran.games.exceptions;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(Long id) {
        super("Game " + id.toString() + " does not exist");
    }
}
