package com.taskmanager;

import com.taskmanager.entity.User;
import com.taskmanager.entity.Task;
import com.taskmanager.enums.AvailabilityStatus;
import com.taskmanager.enums.PriorityLevel;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
//    private final PasswordEncoder passwordEncoder;
  
    public DataInitializer(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
//        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        User user1 = new User("asem", "asem@example.com", "password123", AvailabilityStatus.AVAILABLE);
        User user2 = new User("adil", "adil@example.com", "password123", AvailabilityStatus.BUSY);
        User user3 = new User("asyl", "asyl@example.com", "password123", AvailabilityStatus.AVAILABLE);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);


        Task task1 = new Task("task1", "null", PriorityLevel.HIGH, user1);
        Task task2 = new Task("task2", "null", PriorityLevel.MEDIUM, user2);
        Task task3 = new Task("task3", "null", PriorityLevel.HIGH, user1);
        Task task4 = new Task("task4", "null", PriorityLevel.LOW, user3);
        Task task5 = new Task("task5", "null", PriorityLevel.URGENT, user2);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);

    }
}
