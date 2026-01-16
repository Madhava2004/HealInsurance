package com.genc.healthins.service;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import com.genc.healthins.model.UserPolicy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;

public interface ClaimService {
   
  
    Optional<Claim> findById(Long id);
    Claim save(Claim claim);

    List<Claim> findAll();

    
    
//    // Admin

    void processAdminClaim(Long claimId, String status, BigDecimal approvedAmount);

    // Agent
    List<Claim> findClaimsByAgent(User agent);


    // User
    List<Claim> findClaimsByUser(User user);
    Claim submitClaim(Long policyId, BigDecimal amount, String description, LocalDate incidentDate);

    // Shared Helpers
    Map<String, Integer> calculateClaimStats(List<Claim> claims);
    
    Page<Claim> findClaimsByUserPaginated(User user, int page, int size, String sortField, String sortDir);
    
    Page<Claim> findClaimsByAgentPaginated(User agent, int page, int size, String sortField, String sortDir);
    
    Page<Claim> findAllPaginated(int page, int size, String sortField, String sortDir);
    
    
}