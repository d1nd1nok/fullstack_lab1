package com.taskmanager.repository;

import com.taskmanager.entity.User;
import com.taskmanager.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.availabilityStatus = 'AVAILABLE'")
    List<User> findAvailableUsers();

    List<User> findByAvailabilityStatus(AvailabilityStatus availabilityStatus);
}
