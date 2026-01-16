package com.genc.healthins.controller;

import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.service.PolicyService;
import com.genc.healthins.service.UserPolicyService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class PolicyController {

    private final PolicyService policyService;
    private final UserPolicyService userPolicyService;

    public PolicyController(PolicyService policyService, UserPolicyService userPolicyService) {
        this.policyService = policyService;
        this.userPolicyService = userPolicyService;
    }

    // ==========================================
    // ADMIN SECTION
    // ==========================================

    @GetMapping({"/admin/policies", "/admin/policies.html"})
    public String adminPolicies(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "coverageType") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<Policy> policyPage = policyService.findAllTemplatesPaginated(page, size, sortField, sortDir);

        model.addAttribute("policies", policyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", policyPage.getTotalPages());
        model.addAttribute("totalItems", policyPage.getTotalElements());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "admin/policies";
    }

    @PostMapping("/admin/policies")
    public String createPolicy(@RequestParam String policyName, 
                               @RequestParam java.math.BigDecimal premium,
                               @RequestParam java.math.BigDecimal coverage, 
                               @RequestParam String startDate,
                               @RequestParam String endDate, 
                               RedirectAttributes ra) {

     
        // Check if premium or coverage is negative
        if (premium.compareTo(java.math.BigDecimal.ZERO) < 0 || coverage.compareTo(java.math.BigDecimal.ZERO) < 0) {
            ra.addFlashAttribute("error", "Failed: Premium and Coverage amounts cannot be negative.");
            return "redirect:/admin/policies";
        }
        

        Policy p = policyService.createPolicyTemplate(policyName, premium, coverage, startDate, endDate);
        ra.addFlashAttribute("success", "New Plan published with ID: " + p.getPolicyNumber());
        return "redirect:/admin/policies";
    }

    @PostMapping("/admin/policies/edit")
    public String editPolicy(@RequestParam Long id,
                             @RequestParam(required = false) String policyName,
                             @RequestParam(required = false) java.math.BigDecimal premium,
                             @RequestParam(required = false) java.math.BigDecimal coverage,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             RedirectAttributes ra) {

    
        if ((premium != null && premium.compareTo(java.math.BigDecimal.ZERO) < 0) || 
            (coverage != null && coverage.compareTo(java.math.BigDecimal.ZERO) < 0)) {
            ra.addFlashAttribute("error", "Update Failed: Premium and Coverage amounts cannot be negative.");
            return "redirect:/admin/policies";
        }
       

        policyService.updatePolicyTemplate(id, policyName, premium, coverage, startDate, endDate);
        ra.addFlashAttribute("success", "Plan updated successfully!");
        return "redirect:/admin/policies";
    }

    @GetMapping({"/admin/policies/{id}", "/admin/policies/{id}.html"})
    public String viewPolicy(@PathVariable Long id, Model model) {
        policyService.findById(id).ifPresent(policy -> {
            model.addAttribute("policy", policy);
            model.addAttribute("enrollments", userPolicyService.findByPolicy(policy));
        });
        return "admin/policy-details";
    }

    // ==========================================
    // AGENT SECTION
    // ==========================================

    @GetMapping({"/agent/policies", "/agent/policies.html"})
    public String agentPolicies(
            Model model, 
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size, 
            @RequestParam(defaultValue = "coverageType") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        // Security Check
        if (request.getSession().getAttribute("loggedInUser") == null) return "redirect:/login";

        // Call Service (Logic is hidden inside)
        Page<Policy> policyPage = policyService.findAllTemplatesPaginated(page, size, sortField, sortDir);

        // Pass results to Model
        model.addAttribute("policies", policyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", policyPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "agent/policies";
    }

    @GetMapping("/agent/customers/{id}/policies")
    public String agentCustomerPolicies(@PathVariable Long id, Model model) {
        model.addAttribute("policies", policyService.findById(id)); 
        return "agent/customer-policies";
    }



    @PostMapping("/agent/policies/send-reminder")
    @ResponseBody
    public ResponseEntity<?> sendReminder(@RequestParam Long policyId, @RequestParam String type) {
        // Simple enough to keep logic inline, or move to service if complex reminder logic is added later
        if (policyService.findById(policyId).isEmpty()) return ResponseEntity.status(404).build();
        return ResponseEntity.ok(Map.of("message", type + " reminder sent successfully!"));
    }
    
    // ==========================================
    // USER SECTION
    // ==========================================

    @GetMapping({"/user/policies", "/user/policies.html"})
    public String userPolicies(
            Model model, 
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size, // 3 cards per page looks good for grid
            @RequestParam(defaultValue = "endDate") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        User user = (User) request.getSession().getAttribute("loggedInUser");
        if(user == null) return "redirect:/login";

        Page<UserPolicy> policyPage = userPolicyService.findByUserPaginated(user, page, size, sortField, sortDir);

        model.addAttribute("policies", policyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", policyPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "user/policies";
    }

    @GetMapping({"/user/policy-details", "/user/policy-details.html"})
    public String viewUserPolicyDetails(@RequestParam("id") Long policyId, Model model, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        UserPolicy policy = userPolicyService.findById(policyId).orElse(null);
        if (policy == null || policy.getUser().getId() != user.getId()) {
            return "redirect:/user/policies";
        }

        model.addAttribute("policy", policy);
        return "user/policy-details";
    }

    @GetMapping({"/user/marketplace", "/user/marketplace.html"})
    public String marketplace(Model model, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        model.addAttribute("plans", policyService.findMarketplacePlansForUser(user));
        return "user/marketplace";
    }

    @PostMapping("/user/policies/enroll")
    public String enrollInPolicy(@RequestParam("planId") Long planId) {
        return "redirect:/user/checkout?planId=" + planId;
    }

    @GetMapping("/user/marketplace/details")
    public String viewMarketplacePlanDetails(@RequestParam("id") Long id, Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("loggedInUser") == null) return "redirect:/login";
        policyService.findById(id).ifPresentOrElse(p -> model.addAttribute("plan", p), () -> {});
        return "user/marketplace-details";
    }

    @GetMapping("/user/checkout")
    public String checkoutPage(@RequestParam("planId") Long planId, Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("loggedInUser") == null) return "redirect:/login";
        policyService.findById(planId).ifPresent(p -> model.addAttribute("plan", p));
        return "user/checkout";
    }
}