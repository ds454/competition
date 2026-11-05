package ds454.locomotive.competition.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Document(collection = "competitions")
public class Competition {

    @Id
    private UUID uuid;

    @Setter
    private String name;

    private String gameId;
    private User host;
    private Long startTime;
    private Long endTime;

    private Set<User> competitors = new HashSet<>();

    protected Competition() {}

    public Competition(String name, String gameId, User host, Long startTime, Long endTime) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.gameId = gameId;
        this.host = host;
        this.startTime = startTime;
        this.endTime = endTime;

        // The host also participates in the competition
        competitors.add(host);
    }
}