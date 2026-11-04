package ds454.locomotive.competition.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ds454.locomotive.competition.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
