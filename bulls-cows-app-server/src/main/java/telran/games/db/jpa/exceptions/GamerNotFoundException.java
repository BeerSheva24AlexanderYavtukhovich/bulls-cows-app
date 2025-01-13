package telran.games.db.jpa.exceptions;

public class GamerNotFoundException extends RuntimeException {

    public GamerNotFoundException(String username) {
        super("Gamer " + username + " not found");
    }

}
