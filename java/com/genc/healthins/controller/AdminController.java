package com.genc.healthins.controller;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.User;
import com.genc.healthins.repository.ClaimRepository;
import com.genc.healthins.repository.PolicyRepository;
import com.genc.healthins.repository.UserRepository;
import com.genc.healthins.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final UserService userService;
    private final PolicyService policyService;
    private final ClaimService claimService;
    private final SupportService supportService;
    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;

    public AdminController(UserService userService, PolicyService policyService,
                           ClaimService claimService, SupportService supportService,
                           ClaimRepository claimRepository, PolicyRepository policyRepository, 
                           UserRepository userRepository) {
        this.userService = userService;
        this.policyService = policyService;
        this.claimService = claimService;
        this.supportService = supportService;
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
    }

    // --- DASHBOARD ---
    @GetMapping({"/admin/dashboard", "/admin/dashboard.html"})
    public String dashboard(Model model) {
        var users = userService.findAll();
        var policies = policyService.findAll();
        List<Claim> allClaims = claimService.findAll();

        model.addAttribute("usersCount", users.size());
        model.addAttribute("activePolicies", policies.stream().filter(p -> "ACTIVE".equalsIgnoreCase(p.getPolicyStatus())).count());
        model.addAttribute("openClaims", allClaims.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getClaimStatus())).count());
        model.addAttribute("ticketsCount", supportService.findAll().size());

        var recentClaims = allClaims.stream()
                .sorted(Comparator.comparing(Claim::getId).reversed())
                .limit(3)
                .collect(Collectors.toList());
        model.addAttribute("recentClaims", recentClaims);

        // Logic Moved to Service
        model.addAttribute("recentUsers", userService.getRecentUsersWithStats());

        return "admin/dashboard";
    }

    // --- USER MANAGEMENT ---
    @GetMapping({"/admin/users", "/admin/users.html"})
    public String listUsers(Model model) {
        List<User> allUsers = userService.findAll();
        List<User> agents = allUsers.stream()
                .filter(u -> "AGENT".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());

        model.addAttribute("users", allUsers);
        model.addAttribute("agents", agents);
        return "admin/users";
    }

    @PostMapping("/admin/users/assign-agent")
    public String assignAgentToUser(@RequestParam Long userId, @RequestParam(required = false) Long agentId, RedirectAttributes ra) {
        userService.assignAgent(userId, agentId);
        ra.addFlashAttribute("success", "Agent assigned successfully!");
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/add")
    public String addUser(@ModelAttribute User user, RedirectAttributes ra) {
        userService.save(user); // Service handles password encoding & date
        ra.addFlashAttribute("success", "New account created!");
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/edit")
    public String editUser(@RequestParam Long id, @RequestParam String username,
                           @RequestParam String phone, @RequestParam String role, RedirectAttributes ra) {
        userService.updateUser(id, username, phone, role);
        ra.addFlashAttribute("success", "User updated!");
        return "redirect:/admin/users";
    }

    @GetMapping({"/admin/users/{id}", "/admin/users/{id}.html"})
    public String userDetails(@PathVariable Long id, Model model) {
        userService.findById(id).ifPresent(u -> {
            model.addAttribute("user", u);
            model.addAttribute("policies", policyService.findByUser(u));
        });
        return "admin/user-details";
    }

    // --- ANALYTICS ---
    @GetMapping({"/admin/analytics", "/admin/analytics.html"})
    public String analytics(Model model) {
        // Analytics logic remains here as it's specific report generation 
        // often tied to direct repo queries for performance or CSV exports.
        long totalUsers = userRepository.count();
        long activePolicies = policyRepository.countByPolicyStatusIgnoreCase("ACTIVE");
        long claimsProcessed = claimRepository.countByClaimStatusNotIgnoreCase("PENDING");
        java.math.BigDecimal revenue = claimRepository.calculateTotalRevenue();
        if (revenue == null) revenue = java.math.BigDecimal.ZERO;

        long pending = claimRepository.countByClaimStatusIgnoreCase("PENDING");
        long approved = claimRepository.countByClaimStatusIgnoreCase("APPROVED");
        long rejected = claimRepository.countByClaimStatusIgnoreCase("REJECTED");

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activePolicies", activePolicies);
        model.addAttribute("claimsProcessed", claimsProcessed);
        model.addAttribute("revenue", revenue);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("approvedCount", approved);
        model.addAttribute("rejectedCount", rejected);
        model.addAttribute("revenueData", java.util.Arrays.asList(12000.0, 15000.0, 18000.0, 14500.0, 21000.0, 19500.0));

        return "admin/analytics";
    }
}