package ds454.locomotive.competition.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ds454.locomotive.competition.model.Competition;

@Repository
public interface CompetitionRepository extends MongoRepository<Competition, UUID> {
    List<Competition> findByCompetitorsUuid(String userUuid);
}
