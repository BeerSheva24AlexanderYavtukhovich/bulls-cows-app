package telran.games.db.repository;

import java.time.LocalDate;
import java.util.List;

import telran.games.service.BullsCowsServiceImpl.GameResult;

public interface BullsCowsRepository {
    boolean isUserExists(String username) throws Exception;

    void addUser(String username, LocalDate birthDate) throws Exception;

    Long createGame() throws Exception;

    void startGame(Long gameId, String username) throws Exception;

    void joinGame(String username, Long gameId) throws Exception;

    GameResult performMove(Long gameId, String username, String move) throws Exception;

    List<Long> getUnstartedGames(String username) throws Exception;

    List<Long> getGamesToJoin(String username) throws Exception;

    List<Long> getGamesToPlay(String username) throws Exception;

}
