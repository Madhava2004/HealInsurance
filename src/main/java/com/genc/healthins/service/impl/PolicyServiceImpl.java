package com.genc.healthins.service.impl;


import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.repository.PolicyRepository;
import com.genc.healthins.repository.UserPolicyRepository;

import com.genc.healthins.service.PolicyService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final UserPolicyRepository userPolicyRepository;


    public PolicyServiceImpl(PolicyRepository policyRepository,
                             UserPolicyRepository userPolicyRepository
                            ) {
        this.policyRepository = policyRepository;
        this.userPolicyRepository = userPolicyRepository;

    }

    @Override
    public List<Policy> findAll() { 
    	
    	return policyRepository.findAll(); 
    }

    @Override
    public Optional<Policy> findById(Long id) { 
    	return policyRepository.findById(id); 
    }

    @Override
    public List<Policy> findByUser(User user) { 
    	
    	return policyRepository.findByUser(user); 
    }

    @Override
    public List<Policy> findPoliciesByAssignedAgent(int agentId) {
        return policyRepository.findByUser_AssignedAgentId(agentId);
    }

    @Override
    public Policy save(Policy policy) { 
    	
    	return policyRepository.save(policy); 
    }



    @Override
    public List<Policy> findAllTemplates() {
        return findAll().stream()
                .filter(p -> p.getUser() == null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Policy createPolicyTemplate(String planname, BigDecimal premium, BigDecimal coverage, String startDateStr, String endDateStr) {
        Policy p = new Policy();

        // Custom ID Logic
        int currentYear = Year.now().getValue();
        long countThisYear = findAllTemplates().stream()
                .filter(policy -> policy.getStartDate() != null && policy.getStartDate().getYear() == currentYear)
                .count();

        String customId = String.format("POL-%d-%03d", currentYear, countThisYear + 1);
        p.setPolicyNumber(customId);
        p.setCoverageType(planname);
        p.setCoverageAmount(coverage);
        p.setPremiumAmount(premium);
        p.setPolicyStatus("ACTIVE");
        p.setStartDate(LocalDate.parse(startDateStr).atStartOfDay());
        p.setEndDate(LocalDate.parse(endDateStr).atStartOfDay());

        return policyRepository.save(p);
    }

    @Override
    @Transactional
    public void updatePolicyTemplate(Long id, String name, BigDecimal premium, BigDecimal coverage, String startDateStr, String endDateStr) {
        policyRepository.findById(id).ifPresent(p -> {
            if (name != null && !name.trim().isEmpty()) p.setCoverageType(name);
            if (premium != null) p.setPremiumAmount(premium);
            if (coverage != null) p.setCoverageAmount(coverage);
            try {
                if (startDateStr != null && !startDateStr.isEmpty()) p.setStartDate(LocalDate.parse(startDateStr).atStartOfDay());
                if (endDateStr != null && !endDateStr.isEmpty()) p.setEndDate(LocalDate.parse(endDateStr).atStartOfDay());
            } catch (Exception e) {
                // Handle parsing error if necessary
            }
            policyRepository.save(p);
        });
    }

    @Override
    public List<Policy> findMarketplacePlansForUser(User user) {
        List<Policy> allTemplates = findAllTemplates();
        
        if (user != null) {
            List<UserPolicy> myPolicies = userPolicyRepository.findByUser(user);
            Set<String> ownedTypes = myPolicies.stream()
                    .map(p -> p.getCoverageType().toLowerCase())
                    .collect(Collectors.toSet());

            return allTemplates.stream()
                    .filter(p -> !ownedTypes.contains(p.getCoverageType().toLowerCase()))
                    .collect(Collectors.toList());
        }
        return allTemplates;
    }


    
    @Override
    public Page<Policy> findAllTemplatesPaginated(int page, int size, String sortField, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                    ? Sort.by(sortField).ascending() 
                    : Sort.by(sortField).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Request from Repository
        return policyRepository.findAll(pageable);
    }
    
    
}