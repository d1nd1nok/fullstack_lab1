package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByPriorityLevel(Task.PriorityLevel priorityLevel);
    List<Task> findByAssignedUser(User assignedUser);

    @Query("SELECT t FROM Task t WHERE t.priorityLevel = 'HIGH' OR t.priorityLevel = 'URGENT'")
    List<Task> findHighPriorityTasks();

    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId")
    List<Task> findByAssignedUserId(Long userId);
}