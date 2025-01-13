package telran.games.db.jpa.exceptions;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(Long id) {
        super("Game " + id.toString() + " does not exist");
    }
}
