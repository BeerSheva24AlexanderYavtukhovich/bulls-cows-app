
package telran.games.db.jpa.exceptions;

public class UserAlreadyExist extends Exception {

    public UserAlreadyExist(String username) {
        super("User " + username + " already exists.");
    }

}
