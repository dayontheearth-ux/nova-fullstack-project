package com.nova.controller;

import com.nova.dto.TaskDtos.*;
import com.nova.entity.*;
import com.nova.repository.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskRepository tasks;
    private final ProjectRepository projects;
    private final UserRepository users;

    public TaskController(TaskRepository tasks, ProjectRepository projects, UserRepository users) {
        this.tasks = tasks; this.projects = projects; this.users = users;
    }

    private TaskResponse dto(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), t.getDueDate(),
            t.getStatus(), t.getPriority(), t.getProject().getId(),
            t.getAssignee() == null ? null : t.getAssignee().getId(),
            t.getAssignee() == null ? null : t.getAssignee().getName());
    }

    @GetMapping("/project/{projectId}")
    public List<TaskResponse> byProject(@PathVariable Long projectId) {
        return tasks.findByProjectId(projectId).stream().map(this::dto).toList();
    }

    @PostMapping("/project/{projectId}")
    public TaskResponse create(@PathVariable Long projectId, @RequestBody TaskRequest r) {
        Project p = projects.findById(projectId).orElseThrow();
        User assignee = r.assigneeId() == null ? null : users.findById(r.assigneeId()).orElseThrow();
        Task t = Task.builder().title(r.title()).description(r.description()).dueDate(r.dueDate())
            .status(r.status() == null ? TaskStatus.TODO : r.status())
            .priority(r.priority() == null ? TaskPriority.MEDIUM : r.priority())
            .project(p).assignee(assignee).build();
        return dto(tasks.save(t));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @RequestBody TaskRequest r) {
        Task t = tasks.findById(id).orElseThrow();
        t.setTitle(r.title()); t.setDescription(r.description()); t.setDueDate(r.dueDate());
        if (r.status() != null) t.setStatus(r.status());
        if (r.priority() != null) t.setPriority(r.priority());
        t.setAssignee(r.assigneeId() == null ? null : users.findById(r.assigneeId()).orElseThrow());
        return dto(tasks.save(t));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        tasks.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
