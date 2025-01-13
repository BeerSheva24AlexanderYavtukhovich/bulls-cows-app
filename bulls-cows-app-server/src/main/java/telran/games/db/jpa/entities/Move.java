package telran.games.db.jpa.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "move")
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @ManyToOne
    @JoinColumn(name = "game_gamer_id")
    GameGamer gameGamer;
    int bulls;
    int cows;
    String sequence;

    public void setBulls(int bulls) {
        this.bulls = bulls;
    }

    public void setCows(int cows) {
        this.cows = cows;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public void setGameGamer(GameGamer gameGamer) {
        this.gameGamer = gameGamer;
    }

    @Override
    public String toString() {
        return "{" +
                "\"bulls\": " + bulls + ", " +
                "\"cows\": " + cows + ", " +
                "\"sequence\": \"" + sequence + "\", " +
                "\"gameGamer\": \"" + (gameGamer != null ? gameGamer.getGamer().getUsername() : "Unknown") + "\"" +
                "}";
    }
}
