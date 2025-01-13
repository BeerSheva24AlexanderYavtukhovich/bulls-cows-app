package telran.games.db.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import telran.games.db.jpa.entities.Game;
import telran.games.db.jpa.entities.GameGamer;
import telran.games.db.jpa.entities.Gamer;
import telran.games.db.jpa.entities.Move;
import telran.games.db.jpa.exceptions.GameAlreadyStartedException;
import telran.games.db.jpa.exceptions.GameFinishedException;
import telran.games.db.jpa.exceptions.GameNotFoundException;
import telran.games.db.jpa.exceptions.GameNotStartedException;
import telran.games.db.jpa.exceptions.GamerAlreadyInGameException;
import telran.games.db.jpa.exceptions.GamerNotFoundException;
import telran.games.db.jpa.exceptions.OnlyGamerInGameCanStartGameException;
import telran.games.db.jpa.exceptions.UserAlreadyExist;
import telran.games.db.jpa.exceptions.UserNotInGameException;
import telran.games.service.BullsCowsServiceImpl.GameResult;

public class BullsCowsRepositoryImpl implements BullsCowsRepository {

    private final EntityManager em;

    public BullsCowsRepositoryImpl(EntityManager em) {
        this.em = em;

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

            if (game == null) {
                throw new GameNotFoundException(gameId);
            }

            if (game.getDateTime() != null) {
                throw new GameAlreadyStartedException(gameId);
            }

            Long existingAssociation = getExistingAssociation(gameId, username);
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

    private Long getExistingAssociation(Long gameId, String username) throws Exception {
        try {
            String query = "SELECT COUNT(gg) FROM GameGamer gg WHERE gg.game.id = :gameId AND gg.gamer.username = :username";
            return em.createQuery(query, Long.class)
                    .setParameter("gameId", gameId)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Gamer getGamerByUsername(String username) {
        String jpqlString = "SELECT g FROM Gamer g WHERE g.username = :username";
        TypedQuery<Gamer> query = em.createQuery(jpqlString, Gamer.class);
        query.setParameter("username", username);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private GameGamer getGameGamerByUsernameAndGameID(String username, Long gameId) {
        String jpqlString = "SELECT g FROM GameGamer g WHERE g.gamer.username = :username AND g.game.id = :gameId";
        TypedQuery<GameGamer> query = em.createQuery(jpqlString, GameGamer.class);
        query.setParameter("username", username);
        query.setParameter("gameId", gameId);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public GameResult performMove(Long gameId, String username, String sequence) throws Exception {
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
            if (gamer == null) {
                throw new GamerNotFoundException(username);
            }
            Long existingAssociation = getExistingAssociation(gameId, username);

            if (existingAssociation == 0) {
                throw new UserNotInGameException(username, gameId);
            }
            String secretSequence = game.getSequence();

            // int[] bullsCowsAr = service.calculateBullsCows(secretSequence, sequence);
            // //FIXME
            int[] bullsCowsAr = { 0, 0 };
            boolean isWin = bullsCowsAr[0] == secretSequence.length();
            if (isWin) {
                game.setFinished(true);
                em.merge(game);
            }

            GameGamer gameGamer = getGameGamerByUsernameAndGameID(username, gameId);
            gameGamer.setWinner(isWin);

            Move gameMove = new Move();
            gameMove.setBulls(bullsCowsAr[0]);
            gameMove.setCows(bullsCowsAr[1]);
            gameMove.setSequence(sequence);
            gameMove.setGameGamer(gameGamer);
            em.persist(gameMove);

            transaction.commit();
            List<Move> allMoves = em.createQuery(
                    "SELECT m FROM Move m WHERE m.gameGamer.game.id = :gameId AND m.gameGamer.gamer.username = :username",
                    Move.class)
                    .setParameter("gameId", gameId)
                    .setParameter("username", username)
                    .getResultList();

            return new GameResult(allMoves, isWin);
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
            if (isUserExists(username)) {
                throw new UserAlreadyExist(username);
            }
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
    public List<Long> getUnstartedGames(String username) throws Exception {
        try {
            String jpqlString = "SELECT DISTINCT g.id FROM Game g WHERE g.dateTime IS NULL AND g.isFinished = false";
            TypedQuery<Long> query = em.createQuery(jpqlString, Long.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<Long> getGamesToJoin(String username) throws Exception {
        try {
            String jpqlString = "SELECT g.id FROM Game g " +
                                "LEFT JOIN g.gameGamers gg " +
                                "WITH gg.gamer.username = :username " +
                                "WHERE g.isFinished = false " +
                                "AND gg IS NULL";
            TypedQuery<Long> query = em.createQuery(jpqlString, Long.class);
            query.setParameter("username", username);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public List<Long> getGamesToPlay(String username) throws Exception {
        try {
            String jpqlString = "SELECT g.id FROM Game g " + "JOIN g.gameGamers gg " + "WHERE g.dateTime IS NOT NULL " + "AND gg.gamer.username = :username";
            TypedQuery<Long> query = em.createQuery(jpqlString, Long.class);
            query.setParameter("username", username);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
