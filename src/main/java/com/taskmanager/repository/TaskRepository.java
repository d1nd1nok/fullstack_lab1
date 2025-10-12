package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.PriorityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByPriorityLevel(PriorityLevel priorityLevel);
    List<Task> findByAssignedUser(User assignedUser);

    @Query("SELECT t FROM Task t WHERE t.priorityLevel = 'HIGH' OR t.priorityLevel = 'URGENT'")
    List<Task> findHighPriorityTasks();

    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId")
    List<Task> findByAssignedUserId(Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.done = false")
    long countActiveTasksByUserId(Long userId);

    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.done = false")
    List<Task> findActiveTasksByUserId(Long userId);
}