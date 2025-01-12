package telran.games.repository;

import java.time.LocalDate;
import java.util.List;

public interface BullsCowsRepository {
    boolean isUserExists(String username) throws Exception;

    void addUser(String username, LocalDate birthDate) throws Exception;

    Long createGame() throws Exception;

    void startGame(Long gameId, String username) throws Exception;

    void joinGame(String username, Long gameId) throws Exception;

    void performMove(Long gameId, String username, String move);

    List<Long> getUnstartedGames(String username)  throws Exception;

    List<Long> getGamesToJoin(String username)  throws Exception;

    String getWinnerOfGame(Long gameId) throws Exception;

}
