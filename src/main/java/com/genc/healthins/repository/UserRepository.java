package com.genc.healthins.repository;

import com.genc.healthins.model.User;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);


    Optional<User> findByUsername(String username);
    List<User> findByAssignedAgentId(Integer assignedAgentId);
}