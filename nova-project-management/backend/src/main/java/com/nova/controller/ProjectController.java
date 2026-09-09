package com.nova.controller;

import com.nova.dto.ProjectDtos.*;
import com.nova.entity.*;
import com.nova.repository.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectRepository projects;
    private final UserRepository users;
    private final TaskRepository tasks;

    public ProjectController(ProjectRepository projects, UserRepository users, TaskRepository tasks) {
        this.projects = projects; this.users = users; this.tasks = tasks;
    }

    private User current(Authentication a) {
        return users.findByEmail(a.getName()).orElseThrow();
    }

    private ProjectResponse dto(Project p) {
        long total = tasks.countByProjectId(p.getId());
        long done = tasks.countByProjectIdAndStatus(p.getId(), TaskStatus.COMPLETED);
        List<MemberResponse> members = p.getMembers().stream()
            .map(u -> new MemberResponse(u.getId(), u.getName(), u.getEmail())).toList();
        return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getStartDate(),
            p.getDueDate(), p.getOwner().getId(), p.getOwner().getName(), members, total, done);
    }

    @GetMapping
    public List<ProjectResponse> all(Authentication a) {
        User u = current(a);
        return projects.findByOwnerIdOrMembersId(u.getId(), u.getId()).stream().map(this::dto).toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse one(@PathVariable Long id) {
        return dto(projects.findById(id).orElseThrow());
    }

    @PostMapping
    public ProjectResponse create(@RequestBody ProjectRequest r, Authentication a) {
        User owner = current(a);
        Project p = Project.builder().name(r.name()).description(r.description())
            .startDate(r.startDate()).dueDate(r.dueDate()).owner(owner).build();
        p.getMembers().add(owner);
        return dto(projects.save(p));
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @RequestBody ProjectRequest r) {
        Project p = projects.findById(id).orElseThrow();
        p.setName(r.name()); p.setDescription(r.description());
        p.setStartDate(r.startDate()); p.setDueDate(r.dueDate());
        return dto(projects.save(p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        projects.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/members/{userId}")
    public ProjectResponse addMember(@PathVariable Long id, @PathVariable Long userId) {
        Project p = projects.findById(id).orElseThrow();
        User u = users.findById(userId).orElseThrow();
        p.getMembers().add(u);
        return dto(projects.save(p));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ProjectResponse removeMember(@PathVariable Long id, @PathVariable Long userId) {
        Project p = projects.findById(id).orElseThrow();
        p.getMembers().removeIf(u -> u.getId().equals(userId));
        return dto(projects.save(p));
    }

    @GetMapping("/users/search")
    public List<MemberResponse> searchUsers(@RequestParam String q) {
        return users.findTop20ByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q)
            .stream().map(u -> new MemberResponse(u.getId(), u.getName(), u.getEmail())).toList();
    }
}
