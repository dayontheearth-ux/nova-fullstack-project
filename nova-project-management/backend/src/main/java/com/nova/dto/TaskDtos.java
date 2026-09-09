package com.nova.dto;

import com.nova.entity.TaskPriority;
import com.nova.entity.TaskStatus;
import java.time.LocalDate;

public class TaskDtos {
    public record TaskRequest(String title, String description, LocalDate dueDate,
                              TaskStatus status, TaskPriority priority, Long assigneeId) {}
    public record TaskResponse(Long id, String title, String description, LocalDate dueDate,
                               TaskStatus status, TaskPriority priority, Long projectId,
                               Long assigneeId, String assigneeName) {}
}
