package telran.games.db.jpa.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

    public boolean isFinished() {
        return isFinished;
    }

    public String getSequence() {
        return sequence;
    }

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final Set<GameGamer> gameGamers = new HashSet<>();

}
