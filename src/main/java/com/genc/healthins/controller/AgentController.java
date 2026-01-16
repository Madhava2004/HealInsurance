package com.genc.healthins.controller;

import com.genc.healthins.model.User;
import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.service.PolicyService;
import com.genc.healthins.service.SupportService;
import com.genc.healthins.service.UserPolicyService;
import com.genc.healthins.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class AgentController {

    private final PolicyService policyService;
    private final SupportService supportService;
    private final UserService userService;
    private final UserPolicyService userPolicyService;

    public AgentController(PolicyService policyService, SupportService supportService, UserService userService,UserPolicyService userPolicyService) {
        this.policyService = policyService;
        this.supportService = supportService;
        this.userService = userService;
        this.userPolicyService=userPolicyService;
    }

    // --- DASHBOARD ---
    @GetMapping("/agent/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        User agent = (User) request.getSession().getAttribute("loggedInUser");
        if (agent == null) return "redirect:/login";

        // 1. Fetch Policies List (Visuals)
        var policies = policyService.findAll();
        model.addAttribute("policies", policies);

        // 2. Fetch Aggregated Stats (Counts, Claims Status, etc.)
        Map<String, Object> stats = userService.getAgentDashboardStats(agent);
        model.addAllAttributes(stats);

        // 3. Support Tickets Count (kept simple call)
        model.addAttribute("openTicketsCount", supportService.countByStatus("OPEN"));

        return "agent/dashboard";
    }

    // --- CUSTOMERS LIST ---
    @GetMapping({"/agent/customers", "/agent/customers.html"})
    public String customers(Model model, HttpServletRequest request) {
        User agent = (User) request.getSession().getAttribute("loggedInUser");
        if (agent == null) return "redirect:/login";

        // Fetch customers with their stats pre-calculated
        List<Map<String, Object>> customerViews = userService.getAgentCustomersWithStats(agent);
        model.addAttribute("customerViews", customerViews);
        
        // Simple counts based on the returned list
        model.addAttribute("statActive", customerViews.stream().filter(v -> "ACTIVE".equals(v.get("status"))).count());
        model.addAttribute("statPending", customerViews.stream().filter(v -> "PENDING".equals(v.get("status"))).count());
        model.addAttribute("statInactive", 0);

        return "agent/customers";
    }

    @GetMapping({"/agent/customers/{id}", "/agent/customers/{id}.html"})
    public String customerDetails(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return userService.findById(id).map(user -> {
            model.addAttribute("customer", user);
            
            // 2. FETCH FROM USER_POLICY SERVICE
            List<UserPolicy> policies = userPolicyService.findByUser(user);
            model.addAttribute("policies", policies); 
            return "agent/customer-details";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Customer not found");
            return "redirect:/agent/customers";
        });
    }
}