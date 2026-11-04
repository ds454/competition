package ds454.locomotive.competition.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ds454.locomotive.competition.model.Competition;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, UUID> {
}
