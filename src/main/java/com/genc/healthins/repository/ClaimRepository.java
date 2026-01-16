package com.genc.healthins.repository;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import com.genc.healthins.model.UserPolicy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByPolicy(UserPolicy policy);
    List<Claim> findByPolicy(Policy policy);
  //  List<Claim> findTop1ByClaimStatusOrderByClaimDateDesc(String status);
 
    long countByClaimStatusNotIgnoreCase(String status);
 
    // 2. Specific status (APPROVED/REJECTED/PENDING)
    long countByClaimStatusIgnoreCase(String status);
 
    // 3. Total Revenue: Only 'APPROVED' claims 
    @Query("SELECT SUM(c.claimAmount) FROM Claim c WHERE UPPER(c.claimStatus) = 'APPROVED'")
    BigDecimal calculateTotalRevenue();
    
    long countByPolicy_User(User user);
    
    //Admin
    Page<Claim> findAll(Pageable pageable);
    
    //User 
    Page<Claim> findByPolicyUser(User user, Pageable pageable);
    
    //Admin
    Page<Claim> findByPolicy_User_AssignedAgentId(int agentId, Pageable pageable);
    
}