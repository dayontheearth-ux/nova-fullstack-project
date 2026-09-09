package com.nova.dto;

import java.time.LocalDate;
import java.util.List;

public class ProjectDtos {
    public record ProjectRequest(String name, String description, LocalDate startDate, LocalDate dueDate) {}
    public record ProjectResponse(Long id, String name, String description, LocalDate startDate,
                                  LocalDate dueDate, Long ownerId, String ownerName,
                                  List<MemberResponse> members, long totalTasks, long completedTasks) {}
    public record MemberResponse(Long id, String name, String email) {}
}
