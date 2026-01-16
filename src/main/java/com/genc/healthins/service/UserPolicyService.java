package com.genc.healthins.service;

import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.model.User;
import com.genc.healthins.model.Policy;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

public interface UserPolicyService {
   List<UserPolicy> findByUser(User user);
    List<UserPolicy> findByPolicy(Policy policy); // Required for Admin Dashboard
    List<UserPolicy> findAll();
    Optional<UserPolicy> findById(Long id); // Added to support controller logic
    Page<UserPolicy> findByUserPaginated(User user, int page, int size, String sortField, String sortDir);
}