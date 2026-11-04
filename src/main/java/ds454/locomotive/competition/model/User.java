package ds454.locomotive.competition.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "users")
@Getter
public class User {
    @Id
    private String uuid;

    @Column(nullable = false)
    private String steamid;

    @ManyToMany(mappedBy = "competitors")
    private Set<Competition> competitions = new HashSet<>();

    protected User() {}

    public User(String uuid, String steamid) {
        this.uuid = uuid;
        this.steamid = steamid;
    }

    public Set<Competition> getCompetitions() { return Set.copyOf(competitions); }

    public boolean joinCompetition(Competition c) {
        c.addCompetitor(this);          // owning side: this is what gets saved
        return competitions.add(c);
    }

    public boolean leaveCompetition(Competition c) {
        c.removeCompetitor(this);
        return competitions.remove(c);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof User other && uuid != null && uuid.equals(other.getUuid()));
    }

    @Override
    public int hashCode() { return Objects.hashCode(uuid); }
}