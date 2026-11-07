package ds454.locomotive.competition.model;

import java.time.Instant;

import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Getter;

@Getter
public class CompetitionInvitation {

    @Indexed
    private String userUuid;

    private Long requestSentTimestamp;

    protected CompetitionInvitation() {
    }

    public CompetitionInvitation(String userUuid) {
        this.userUuid = userUuid;
        this.requestSentTimestamp = Instant.now().getEpochSecond();
    }
}
