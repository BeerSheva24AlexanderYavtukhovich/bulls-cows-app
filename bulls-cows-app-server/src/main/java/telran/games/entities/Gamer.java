package telran.games.entities;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "gamer")
public class Gamer {
    @Id
    String username;
    LocalDate birthDate;

    public List<GameGamer> getGameGamers() {
        return gameGamers;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    @OneToMany(mappedBy = "gamer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameGamer> gameGamers = new ArrayList<>();
}

