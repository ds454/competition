package ds454.locomotive.competition.model;

import java.time.Instant;

import org.springframework.data.mongodb.core.index.Indexed;

import com.google.common.base.Objects;

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

    @Override
    public boolean equals(Object o) {
        return o instanceof CompetitionInvitation other && userUuid.equals(other.userUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userUuid);
    }
}
