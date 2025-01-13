package telran.games.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import telran.games.db.jpa.entities.Move;
import telran.games.db.repository.BullsCowsRepository;

public class BullsCowsServiceImpl implements BullsCowsService {

    private BullsCowsRepository repository;

    public BullsCowsServiceImpl( BullsCowsRepository repository) {
        this.repository = repository;
    }

    public record GameResult(List<Move> moves, boolean isWin) {

        @Override
        public String toString() {
            return "{" +
                    "\"moves\": [" +
                    moves.stream()
                            .map(Move::toString)
                            .reduce((move1, move2) -> move1 + ", " + move2)
                            .orElse("")
                    +
                    "], " +
                    "\"isWin\": " + isWin +
                    "}";
        }
    }

    @Override
    public int[] calculateBullsCows(String secretSequence, String sequence) {
        int length = sequence.length();

        boolean[] secretUsed = new boolean[length];
        boolean[] guessUsed = new boolean[length];
        int bulls = (int) IntStream.range(0, length)
                .filter(i -> {
                    boolean isBull = sequence.charAt(i) == secretSequence.charAt(i);
                    if (isBull) {
                        secretUsed[i] = true;
                        guessUsed[i] = true;
                    }
                    return isBull;
                })
                .count();

        int cows = (int) IntStream.range(0, length)
                .filter(i -> !guessUsed[i])
                .flatMap(i -> IntStream.range(0, length)
                        .filter(j -> !secretUsed[j] && sequence.charAt(i) == secretSequence.charAt(j))
                        .limit(1)
                        .peek(j -> secretUsed[j] = true))
                .count();

        return new int[] { bulls, cows };
    }

    @Override
    public void startGame(Long gameId, String username) throws Exception {
        repository.startGame(gameId, username);
    }

    @Override
    public Long createGame() throws Exception {
        return repository.createGame();
    }

    @Override
    public boolean isUserExists(String username) throws Exception {
        return repository.isUserExists(username);
    }

    @Override
    public void addUser(String username, LocalDate birthDate) throws Exception {
        repository.addUser(username, birthDate);
    }

    @Override
    public void joinGame(String username, Long gameId) throws Exception {
        repository.joinGame(username, gameId);
    }

    @Override
    public GameResult performMove(Long gameId, String username, String move) throws Exception {
        return repository.performMove(gameId, username, move);
    }

    @Override
    public List<Long> getUnstartedGames(String username) throws Exception {
        return repository.getUnstartedGames(username);
    }

    @Override
    public List<Long> getGamesToJoin(String username) throws Exception {
        return repository.getGamesToJoin(username);
    }
    @Override
    public List<Long> getGamesToPlay(String username) throws Exception {
        return repository.getGamesToPlay(username);
    }
}
