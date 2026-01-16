package com.genc.healthins.service;

import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

public interface PolicyService {
    // Basic CRUD
    List<Policy> findAll();
    Optional<Policy> findById(Long id);
    List<Policy> findByUser(User user);
    List<Policy> findPoliciesByAssignedAgent(int agentId);
    Policy save(Policy policy);


    
    List<Policy> findAllTemplates(); // Returns only Master Plans (user == null)
    
    Policy createPolicyTemplate(String name, BigDecimal premium, BigDecimal coverage, String startDate, String endDate);
    
    void updatePolicyTemplate(Long id, String name, BigDecimal premium, BigDecimal coverage, String startDate, String endDate);
    
    List<Policy> findMarketplacePlansForUser(User user); // Filters out plans user already owns
    
   
    
 // PolicyService.java
    Page<Policy> findAllTemplatesPaginated(int page, int size, String sortField, String sortDir);
    
}