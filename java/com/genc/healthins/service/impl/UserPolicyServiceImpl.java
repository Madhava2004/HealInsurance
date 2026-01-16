package com.genc.healthins.service.impl;

import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.model.User;
import com.genc.healthins.model.Policy;
import com.genc.healthins.repository.UserPolicyRepository;
import com.genc.healthins.service.UserPolicyService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserPolicyServiceImpl implements UserPolicyService {

    private final UserPolicyRepository userPolicyRepository;

    public UserPolicyServiceImpl(UserPolicyRepository userPolicyRepository) {
        this.userPolicyRepository = userPolicyRepository;
    }

    @Override
    public List<UserPolicy> findByUser(User user) {
        return userPolicyRepository.findByUser(user);
    }

    @Override
    public List<UserPolicy> findByPolicy(Policy policy) {
        if (policy == null) return new ArrayList<>();
        // Use the ID of the template to find all purchased instances
        return userPolicyRepository.findByOriginalPlanId(policy.getId());
    }

    @Override
    public List<UserPolicy> findAll() {
        return userPolicyRepository.findAll();
    }

    @Override
    public Optional<UserPolicy> findById(Long id) {
        return userPolicyRepository.findById(id);
    }
    
    @Override
    public Page<UserPolicy> findByUserPaginated(User user, int page, int size, String sortField, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return userPolicyRepository.findByUser(user, pageable);
    }
}