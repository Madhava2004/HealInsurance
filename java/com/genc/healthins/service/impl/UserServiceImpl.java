package com.genc.healthins.service.impl;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;
import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.repository.*;
import com.genc.healthins.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PaymentRepository paymentRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final SupportResponseRepository supportResponseRepository;
 
    private final UserPolicyRepository userPolicyRepository; 

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           PolicyRepository policyRepository,
                           ClaimRepository claimRepository,
                           PaymentRepository paymentRepository,
                           SupportTicketRepository supportTicketRepository,
                           SupportResponseRepository supportResponseRepository,
                           UserPolicyRepository userPolicyRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.paymentRepository = paymentRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.supportResponseRepository = supportResponseRepository;
        this.userPolicyRepository = userPolicyRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getJoinDate() == null) {
            user.setJoinDate(java.time.LocalDateTime.now());
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }



    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findCustomersByAgentId(Integer agentId) {
        return userRepository.findAll().stream()
                .filter(u -> u.getAssignedAgentId() != null && u.getAssignedAgentId().equals(agentId))
                .collect(Collectors.toList());
    }


    @Override
    public Map<String, Object> getUserDashboardStats(User user) {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. Get Ticket Stats
        List<SupportTicket> tickets = supportTicketRepository.findByUser(user);
        int ticketsOpen = (int) tickets.stream()
                .filter(t -> !"CLOSED".equalsIgnoreCase(t.getTicketStatus()) && !"RESOLVED".equalsIgnoreCase(t.getTicketStatus()))
                .count();

        // 2. Get Claim Stats (Still need loop if Claim doesn't have direct User link)
        List<UserPolicy> policies = userPolicyRepository.findByUser(user);
        int claimsCount = 0;
        int pendingClaimsCount = 0;

        for (UserPolicy p : policies) {
            List<Claim> policyClaims = claimRepository.findByPolicy(p);
            claimsCount += policyClaims.size();
            for (Claim c : policyClaims) {
                if ("PENDING".equalsIgnoreCase(c.getClaimStatus())) {
                    pendingClaimsCount++;
                }
            }
        }

        // 3. Get Payment Stats (OPTIMIZED)
        // Instead of looping, we run the single SQL count query
        long totalPayments = paymentRepository.countByUser(user);

        stats.put("ticketsOpen", ticketsOpen);
        stats.put("claimsCount", claimsCount);
        stats.put("pendingClaimsCount", pendingClaimsCount);
        stats.put("paymentsDue", totalPayments); // "paymentsDue" maps to the HTML
        
        return stats;
    }
    @Override
    public Map<String, Object> getAgentDashboardStats(User agent) {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. Policies Count
        long activePoliciesCount = policyRepository.count(); // Approximation based on original logic fetching all
        stats.put("activePoliciesCount", (int) activePoliciesCount);

        // 2. Customer Count
        List<User> customers = findCustomersByAgentId(agent.getId());
        stats.put("customersCount", customers.size());

        // 3. Claim Stats
        int pendingClaimsCount = 0;
        int approved = 0;
        int rejected = 0;
        LocalDate today = LocalDate.now();

        // Optimized: Fetch all claims once, but strictly logic should be filtered closer to DB in production.
        // Adhering to original logic's behavior:
        List<Claim> allClaims = claimRepository.findAll();

        for (Claim c : allClaims) {
            if (c.getPolicy() != null && c.getPolicy().getUser() != null) {
                int claimUserId = c.getPolicy().getUser().getId();
                // Check if this user is in the agent's customer list
                boolean isMyCustomer = customers.stream().anyMatch(cust -> cust.getId() == claimUserId);

                if (isMyCustomer) {
                    if ("PENDING".equalsIgnoreCase(c.getClaimStatus())) {
                        pendingClaimsCount++;
                    }
                    if (c.getClaimDate() != null && c.getClaimDate().toLocalDate().equals(today)) {
                        if ("APPROVED".equalsIgnoreCase(c.getClaimStatus())) approved++;
                        if ("REJECTED".equalsIgnoreCase(c.getClaimStatus())) rejected++;
                    }
                }
            }
        }

        stats.put("pendingClaimsCount", pendingClaimsCount);
        stats.put("approved", approved);
        stats.put("rejected", rejected);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getAgentCustomersWithStats(User agent) {
        List<User> assignedUsers = findCustomersByAgentId(agent.getId());
        
        return assignedUsers.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            long pCount = userPolicyRepository.countByUser(u);
            long cCount = claimRepository.countByPolicy_User(u);

            map.put("user", u);
            map.put("policyCount", pCount);
            map.put("claimCount", cCount);
            map.put("status", pCount > 0 ? "ACTIVE" : "PENDING");
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignAgent(Long userId, Long agentId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (agentId == null || agentId == 0) {
                user.setAssignedAgentId(null);
            } else {
                user.setAssignedAgentId(agentId.intValue());
            }
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void updateUser(Long id, String username, String phone, String role) {
        userRepository.findById(id).ifPresent(u -> {
            u.setUsername(username);
            u.setPhone(phone);
            u.setRole(role);
            userRepository.save(u);
        });
    }

    @Override
    public List<Map<String, Object>> getRecentUsersWithStats() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getJoinDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("username", u.getUsername());
                    m.put("email", u.getEmail());
                    m.put("policyCount", userPolicyRepository.findByUser(u).size());
                    m.put("role", u.getRole());
                    return m;
                }).collect(Collectors.toList());
    }
}