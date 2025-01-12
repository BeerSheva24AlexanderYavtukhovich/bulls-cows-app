package telran.games.exceptions;

public class GamerNotFoundException extends RuntimeException {

    public GamerNotFoundException(String username) {
        super("Gamer " + username + " not found");
    }

}
