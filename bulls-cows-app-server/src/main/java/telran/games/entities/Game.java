package telran.games.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "is_finished")
    private boolean isFinished;

    public Long getId() {
        return id;
    }

    private String sequence;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<GameGamer> gameGamers = new ArrayList<>();

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public List<GameGamer> getGameGamers() {
        return gameGamers;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void addGamer(Gamer gamer) {
        GameGamer gameGamer = new GameGamer();
        gameGamer.setGame(this);
        gameGamer.setGamer(gamer);
        gameGamers.add(gameGamer);
    }
}
