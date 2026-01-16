package com.genc.healthins.repository;

import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByUser(User user);
    List<Policy> findByUser_AssignedAgentId(Integer agentId);
    Page<Policy> findByUser(User user, Pageable pageable);
    Page<Policy> findByUser_AssignedAgentId(Integer agentId, Pageable pageable);
    Page<Policy> findByUserIsNull(Pageable pageable);
	long countByPolicyStatusIgnoreCase(String policyStatus);
    
}