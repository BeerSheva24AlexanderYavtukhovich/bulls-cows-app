package telran.games.service;

import java.time.LocalDate;
import java.util.List;

import telran.games.service.BullsCowsServiceImpl.GameResult;

public interface BullsCowsService {
    void startGame(Long gameId, String username) throws Exception;
    Long createGame() throws Exception;
    int[] calculateBullsCows(String secretSequence, String sequence)throws Exception;
    boolean isUserExists(String username) throws Exception;

    void addUser(String username, LocalDate birthDate) throws Exception;

    void joinGame(String username, Long gameId) throws Exception;

    GameResult performMove(Long gameId, String username, String move) throws Exception;

    List<Long> getUnstartedGames(String username) throws Exception;

    List<Long> getGamesToJoin(String username) throws Exception;

    List<Long> getGamesToPlay(String username) throws Exception;


}
