package com.genc.healthins.repository;
//package com.genc.healthins.repository;

import com.genc.healthins.model.UserPolicy;
//import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserPolicyRepository extends JpaRepository<UserPolicy, Long> {
    List<UserPolicy> findByUser(User user);
   // List<UserPolicy> findByUser_AssignedAgentId(Long agentId);
    //List<UserPolicy> findByUser_AssignedAgentId1(Long agentId);
    List<UserPolicy> findByOriginalPlanId(Long originalPlanId);
  //  List<UserPolicy> findByPolicy(Policy policy);
    
    long countByUser(User user);
    //List<UserPolicy> findByUser(User user);
    
    Page<UserPolicy> findByUser(User user, Pageable pageable);
    
}