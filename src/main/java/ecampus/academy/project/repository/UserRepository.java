package ecampus.academy.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ecampus.academy.project.model.User;

public interface UserRepository extends JpaRepository<User,Long>{
Optional<User> findByUsername(String username);
}
