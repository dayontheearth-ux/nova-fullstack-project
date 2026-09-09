package com.nova.repository;

import com.nova.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, com.nova.entity.TaskStatus status);
}
