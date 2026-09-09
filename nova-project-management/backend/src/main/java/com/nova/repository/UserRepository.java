package com.nova.repository;

import com.nova.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findTop20ByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
