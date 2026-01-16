package com.genc.healthins.service.impl;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.Notification;
//import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.repository.ClaimRepository;
import com.genc.healthins.repository.UserPolicyRepository;
import com.genc.healthins.repository.UserRepository;
import com.genc.healthins.service.ClaimService;
import com.genc.healthins.service.NotificationService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final UserPolicyRepository userPolicyRepository;

    public ClaimServiceImpl(ClaimRepository claimRepository,
                            NotificationService notificationService,
                            UserRepository userRepository,
                            UserPolicyRepository userPolicyRepository) {
        this.claimRepository = claimRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.userPolicyRepository = userPolicyRepository;
    }

   

    @Override 
    
    public Optional<Claim> findById(Long id) { 
    	return claimRepository.findById(id); }
    
    @Override 
    public Claim save(Claim claim) { 
    	return claimRepository.save(claim); }

   
    @Override public List<Claim> findAll() { 
    	return claimRepository.findAll(); }
    


    @Override
    @Transactional
    public void processAdminClaim(Long claimId, String status, BigDecimal approvedAmount) {
        findById(claimId).ifPresent(c -> {
            c.setClaimStatus(status);

            if ("APPROVED".equalsIgnoreCase(status) && approvedAmount != null) {
                // Validation: Cap approved amount at requested amount
                if (c.getRequestedAmount() != null && approvedAmount.compareTo(c.getRequestedAmount()) > 0) {
                    c.setClaimAmount(c.getRequestedAmount());
                } else {
                    c.setClaimAmount(approvedAmount);
                }
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                c.setClaimAmount(BigDecimal.ZERO);
            }
            save(c);

            // Notification
            if (c.getPolicy() != null && c.getPolicy().getUser() != null) {
                User policyHolder = c.getPolicy().getUser();
                Notification n = new Notification();
                n.setUser(policyHolder);
                n.setType("CLAIM_UPDATE");
                n.setMessage("Your claim has been " + status.toLowerCase());
                n.setRead(false);
                n.setCreatedAt(LocalDateTime.now());
                notificationService.save(n);
            }
        });
    }

    @Override
    public List<Claim> findClaimsByAgent(User agent) {
        List<User> myCustomers = userRepository.findByAssignedAgentId(agent.getId());
        List<Claim> allClaims = new ArrayList<>();

        for (User customer : myCustomers) {
            List<UserPolicy> customerPolicies = userPolicyRepository.findByUser(customer);
            for (UserPolicy userPolicy : customerPolicies) {
                allClaims.addAll(claimRepository.findByPolicy(userPolicy)); 
            }
        }
        return allClaims;
    }



    @Override
    public List<Claim> findClaimsByUser(User user) {
        List<UserPolicy> policies = userPolicyRepository.findByUser(user);
        List<Claim> allClaims = new ArrayList<>();
        for (UserPolicy p : policies) {
            allClaims.addAll(claimRepository.findByPolicy(p));
        }
        return allClaims;
    }

    @Override
    @Transactional
    public Claim submitClaim(Long policyId, BigDecimal amount, String description, LocalDate incidentDate) {
        UserPolicy policy = userPolicyRepository.findById(policyId).orElse(null);

        if (policy != null) {
            Claim c = new Claim();
            c.setPolicy(policy);
            c.setRequestedAmount(amount);
            c.setClaimAmount(amount);
            c.setDescription(description);
            c.setIncidentDate(incidentDate);
            c.setClaimDate(LocalDateTime.now());
            c.setClaimStatus("PENDING");
            return claimRepository.save(c);
        }
        return null;
    }

    @Override
    public Map<String, Integer> calculateClaimStats(List<Claim> claims) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("pending", (int) claims.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getClaimStatus())).count());
        stats.put("approved", (int) claims.stream().filter(c -> "APPROVED".equalsIgnoreCase(c.getClaimStatus())).count());
        stats.put("rejected", (int) claims.stream().filter(c -> "REJECTED".equalsIgnoreCase(c.getClaimStatus())).count());
        return stats;
    }
    
    @Override
    public Page<Claim> findClaimsByUserPaginated(User user, int page, int size, String sortField, String sortDir) {
        // 1. SERVICE LOGIC: Determine Sort Direction
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        
        // 2. SERVICE LOGIC: Create Pageable object
        PageRequest pageable = PageRequest.of(page, size, sort);
        
        
        return claimRepository.findByPolicyUser(user, pageable);
    }
    
    @Override
    public Page<Claim> findClaimsByAgentPaginated(User agent, int page, int size, String sortField, String sortDir) {
        // 1. Determine Sort Direction
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        
        // 2. Create Pageable object (Use the Interface type here)
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // 3. Call the repository using the agent's ID
        return claimRepository.findByPolicy_User_AssignedAgentId(agent.getId(), pageable);
    }
    
    @Override
    public Page<Claim> findAllPaginated(int page, int size, String sortField, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return claimRepository.findAll(pageable);
    }



}