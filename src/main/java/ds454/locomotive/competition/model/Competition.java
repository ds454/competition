package ds454.locomotive.competition.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "competition")
@Getter
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "comp_uuid")
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "game")
    private String gameId;

    @Column(name = "host")
    private String host;

    @ManyToMany
    @JoinTable(
        name = "comp_parts",
        joinColumns = @JoinColumn(name = "comp_uuid"),
        inverseJoinColumns = @JoinColumn(name = "user_uuid")
    )
    private Set<User> competitors = new HashSet<>();

    protected Competition() {}

    public Competition(String name, String gameId, String host) {
        this.name = name;
        this.gameId = gameId;
        this.host = host;
    }

    public Set<User> getCompetitors() { return Set.copyOf(competitors); }

    // package-private so only User.join/leaveCompetition can call them
    boolean addCompetitor(User u) { return competitors.add(u); }
    boolean removeCompetitor(User u) { return competitors.remove(u); }
}