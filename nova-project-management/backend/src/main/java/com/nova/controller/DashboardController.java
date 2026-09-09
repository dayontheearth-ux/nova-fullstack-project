package com.nova.controller;

import com.nova.entity.TaskStatus;
import com.nova.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final ProjectRepository projects;
    private final TaskRepository tasks;
    private final UserRepository users;

    public DashboardController(ProjectRepository projects, TaskRepository tasks, UserRepository users) {
        this.projects = projects; this.tasks = tasks; this.users = users;
    }

    @GetMapping
    public Map<String,Object> dashboard(Authentication a) {
        Long uid = users.findByEmail(a.getName()).orElseThrow().getId();
        var ps = projects.findByOwnerIdOrMembersId(uid, uid);
        long total = ps.stream().mapToLong(p -> tasks.countByProjectId(p.getId())).sum();
        long completed = ps.stream().mapToLong(p -> tasks.countByProjectIdAndStatus(p.getId(), TaskStatus.COMPLETED)).sum();
        return Map.of(
            "projects", ps.size(),
            "tasks", total,
            "completedTasks", completed,
            "pendingTasks", total - completed
        );
    }
}
