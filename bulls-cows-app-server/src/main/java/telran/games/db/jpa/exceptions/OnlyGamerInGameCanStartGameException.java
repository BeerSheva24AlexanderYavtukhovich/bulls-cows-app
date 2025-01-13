package telran.games.db.jpa.exceptions;

public class OnlyGamerInGameCanStartGameException extends RuntimeException {

    public OnlyGamerInGameCanStartGameException() {
        super("Only gamer that participate in game can start the game");
    }

}
