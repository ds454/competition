package ds454.locomotive.competition.model;

import java.util.Objects;

import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Getter;

@Getter
public class User {
    @Indexed
    private String uuid;

    private String steamid;

    protected User() {
    }

    public User(String uuid, String steamid) {
        this.uuid = uuid;
        this.steamid = steamid;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User other && Objects.equals(uuid, other.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}