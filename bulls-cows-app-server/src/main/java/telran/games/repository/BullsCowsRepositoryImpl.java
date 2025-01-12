package telran.games.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import telran.games.entities.Game;
import telran.games.entities.GameGamer;
import telran.games.entities.Gamer;
import telran.games.entities.Move;
import telran.games.exceptions.GameAlreadyStartedException;
import telran.games.exceptions.GameFinishedException;
import telran.games.exceptions.GameNotFoundException;
import telran.games.exceptions.GameNotStartedException;
import telran.games.exceptions.GamerAlreadyInGameException;
import telran.games.exceptions.GamerNotFoundException;
import telran.games.exceptions.OnlyGamerInGameCanStartGameException;
import telran.games.exceptions.UserNotInGameException;

public class BullsCowsRepositoryImpl implements BullsCowsRepository {

    private final EntityManager em;

    public BullsCowsRepositoryImpl(EntityManager entityManager) {
        this.em = entityManager;
    }

    @Override
    public Long createGame() throws Exception {
        var transaction = em.getTransaction();
        try {
            transaction.begin();
            Game game = new Game();
            game.setSequence(new Random().ints(1, 10).distinct().limit(4).mapToObj(String::valueOf).reduce("",
                    (s, n) -> s + n));
            game.setDateTime(null);
            game.setFinished(false);
            em.persist(game);
            transaction.commit();
            return game.getId();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startGame(Long gameId, String username) throws Exception {
        var transaction = em.getTransaction();
        try {
            transaction.begin();
            Game game = em.find(Game.class, gameId);
            Long existingAssociation = getExistingAssociation(gameId, username);

            if (game == null) {
                throw new GameNotFoundException(gameId);
            }

            if (game.getDateTime() != null) {
                throw new GameAlreadyStartedException(gameId);
            }
            if (existingAssociation == 0) {
                throw new OnlyGamerInGameCanStartGameException();
            }

            game.setDateTime(LocalDateTime.now());
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void joinGame(String username, Long gameId) {
        var transaction = em.getTransaction();
        try {
            transaction.begin();
            Game game = em.find(Game.class, gameId);
            Gamer gamer = em.find(Gamer.class, username);

            if (game == null) {
                throw new GameNotFoundException(gameId);
            }
            if (game.getDateTime() != null) {
                throw new GameAlreadyStartedException(gameId);
            }

            if (username == null) {
                throw new GamerNotFoundException(username);
            }

            Long existingAssociation = getExistingAssociation(gameId, username);

            if (existingAssociation > 0) {
                throw new GamerAlreadyInGameException(username, gameId);
            }

            GameGamer gameGamer = new GameGamer();
            gameGamer.setGame(game);
            gameGamer.setGamer(gamer);
            gameGamer.setWinner(false);

            em.persist(gameGamer);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }

    }

    private Long getExistingAssociation(Long gameId, String gamer) throws Exception {
        try {
            String jpqlCheck = "SELECT COUNT(gg) FROM GameGamer gg WHERE gg.game = :game AND gg.gamer = :gamer";
            Long existingAssociation = em.createQuery(jpqlCheck, Long.class)
                    .setParameter("game", gameId)
                    .setParameter("gamer", gamer)
                    .getSingleResult();
            return existingAssociation;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> getGamesToJoin(String username) throws Exception {
        String jpqlString = "SELECT g FROM Game g JOIN g.gamers gamer WHERE gamer.username = :username AND g.dateTime IS NOT NULL AND g.isFinished = false";
        TypedQuery<Long> query = em.createQuery(jpqlString, Long.class);
        return query.getResultList();
    }

    private Gamer getGamerByUsername(String username) {
        String jpqlString = "SELECT g FROM Gamer g WHERE g.username = :username";
        TypedQuery<Gamer> query = em.createQuery(jpqlString, Gamer.class);
        query.setParameter("username", username);
        return query.getResultList().stream().findFirst().orElse(null);
    }

    @Override
    public void performMove(Long gameId, String username, String moveStr) {
        var transaction = em.getTransaction();
        try {
            transaction.begin();
            Game game = em.find(Game.class, gameId);
            if (game == null || game.getDateTime() == null) {
                throw new GameNotStartedException(gameId);
            }
            if (game.isFinished()) {
                throw new GameFinishedException(gameId);
            }
            Gamer gamer = getGamerByUsername(username);
            if (gamer == null
                    || game.getGameGamers().stream().noneMatch(g -> g.getGamer().getUsername().equals(username))) {
                throw new UserNotInGameException(username, gameId);
            }
            Move gameMove = new Move();
            gameMove.setGame(game);
            gameMove.setGamer(gamer);
            gameMove.setMove(moveStr);
            gameMove.setDateTime(LocalDateTime.now());

            em.persist(gameMove);
            if (game.isFinished()) {
                em.merge(game);
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isUserExists(String username) {
        try {
            String jpqlString = "SELECT COUNT(g) FROM Gamer g WHERE g.username = :username";
            TypedQuery<Long> query = em.createQuery(jpqlString, Long.class);
            query.setParameter("username", username);
            Long gamerExist = query.getSingleResult();
            return gamerExist > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addUser(String username, LocalDate birthDate) throws Exception {
        try {
            Gamer newGamer = new Gamer();
            newGamer.setUsername(username);
            newGamer.setBirthDate(birthDate);
            em.getTransaction().begin();
            em.persist(newGamer);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> getUnstartedGamesWithoutUser(String username) {
        String jpqlString = "SELECT g.id FROM Game g JOIN g.gameGamers gg JOIN gg.gamer gamer WHERE gamer.username = :username AND g.dateTime IS NULL AND g.isFinished = false";
        TypedQuery<Long> query = em.createQuery(jpqlString, Long.class);
        query.setParameter("username", username);
        return query.getResultList();
    }

    @Override
    public String getWinnerOfGame(Long gameId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWinnerOfGame'");
    }

}
