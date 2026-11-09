package ds454.locomotive.competition.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import ds454.locomotive.competition.model.Competition;

@Repository
public interface CompetitionRepository extends MongoRepository<Competition, UUID> {
    List<Competition> findByCompetitorsUuid(String userUuid);

    @Query("{ 'invitations.userUuid': { $eq: ?0 } }")
    List<Competition> getInvitations(String userUuid);

    // Get documents with the matching uuid and that don't have any invitations that
    // match the second argument...
    @Query("{ 'uuid': ?0, 'invitations.userUuid': { $ne: ?1 } }")
    // ...then add the userUuid and invite time to the array.
    @Update("{ $push : { 'invitations': { 'userUuid': ?1, 'requestSentTimestamp': ?2 } }}")
    void inviteUserToCompetition(UUID competitionId, String userUuid, Long inviteTime);

    @Query("{ 'uuid': ?0 }")
    @Update("{ $pull: {'invitations': {'userUuid': ?1} } }")
    void declineCompetitionInvitation(UUID competitionId, String inviteeUuid);

    @Query("{ 'uuid': ?0, 'invitations.userUuid': { $eq: ?1 } }")
    @Update("{ $pull: { 'invitations': {'userUuid': ?1}}, $push: {'competitors': {'uuid': ?1, 'steamid': ?2 } }}")
    void acceptCompetitionInvitation(UUID competitionId, String inviteeUuid, String inviteeSteamid);
}