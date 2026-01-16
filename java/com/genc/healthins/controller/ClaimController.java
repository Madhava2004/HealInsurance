package com.genc.healthins.controller;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.User;
import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.repository.UserPolicyRepository;
import com.genc.healthins.service.ClaimService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal; // Import added

@Controller
public class ClaimController {

    private final ClaimService claimService;
    private final UserPolicyRepository userPolicyRepository; 

    public ClaimController(ClaimService claimService, UserPolicyRepository userPolicyRepository) {
        this.claimService = claimService;
        this.userPolicyRepository = userPolicyRepository;
    }

    // ==========================================
    // ADMIN SECTION
    // ==========================================

    @GetMapping({"/admin/claims", "/admin/claims.html"})
    public String adminClaims(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "claimDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<Claim> claimPage = claimService.findAllPaginated(page, size, sortField, sortDir);
        
        List<Claim> allClaims = claimService.findAll();
        Map<String, Integer> stats = claimService.calculateClaimStats(allClaims);

        model.addAttribute("claims", claimPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", claimPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        model.addAttribute("pendingCount", stats.get("pending"));
        model.addAttribute("approvedCount", stats.get("approved"));
        model.addAttribute("rejectedCount", stats.get("rejected"));

        return "admin/claims";
    }

    @PostMapping("/admin/claims/process")
    public String processClaimAdmin(@RequestParam Long claimId, @RequestParam String status,
                                    @RequestParam(required = false) BigDecimal approvedAmount,
                                    RedirectAttributes ra) {
        
        
        if (approvedAmount != null && approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("error", "Process Failed: Approved amount cannot be Zero or Negative.");
            return "redirect:/admin/claims";
        }
     

        claimService.processAdminClaim(claimId, status, approvedAmount);
        ra.addFlashAttribute("success", "Claim " + status);
        return "redirect:/admin/claims";
    }

    @GetMapping("/admin/analytics/export")
    public void exportClaims(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=claims_report.csv");

        List<Claim> claims = claimService.findAll();
        PrintWriter writer = response.getWriter();
        writer.println("Claim ID,Policy Number,Coverage Type,Amount,Status,Date");

        for (Claim c : claims) {
            String policyNum = (c.getPolicy() != null) ? c.getPolicy().getPolicyNumber() : "N/A";
            String coverage = (c.getPolicy() != null) ? c.getPolicy().getCoverageType() : "N/A";
            String amount = (c.getClaimAmount() != null) ? c.getClaimAmount().toString() : "0";
            String date = (c.getClaimDate() != null) ? c.getClaimDate().toString() : "N/A";

            writer.println(String.format("%d,%s,%s,%s,%s,%s",
                    c.getId(), policyNum, coverage, amount, c.getClaimStatus(), date));
        }
        writer.flush();
        writer.close();
    }

    // ==========================================
    // AGENT SECTION
    // ==========================================

    @GetMapping({"/agent/claims", "/agent/claims.html"})
    public String agentClaims(
            Model model, 
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "claimDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir) {

        User agent = (User) request.getSession().getAttribute("loggedInUser");
        if (agent == null) return "redirect:/login";

        Page<Claim> claimPage = claimService.findClaimsByAgentPaginated(agent, page, size, sortField, sortDir);
        List<Claim> allClaims = claimService.findClaimsByAgent(agent);
        Map<String, Integer> stats = claimService.calculateClaimStats(allClaims);

        model.addAttribute("claims", claimPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", claimPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        model.addAttribute("statPending", stats.get("pending"));
        model.addAttribute("statApproved", stats.get("approved"));
        model.addAttribute("statRejected", stats.get("rejected"));
        
        return "agent/claims";
    }



    // ==========================================
    // USER SECTION
    // ==========================================

    @GetMapping({"/user/claims", "/user/claims.html"})
    public String userClaims(
            Model model, 
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "claimDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir) {

        User user = (User) request.getSession().getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Page<Claim> claimPage = claimService.findClaimsByUserPaginated(user, page, size, sortField, sortDir);
        
        List<Claim> allClaims = claimService.findClaimsByUser(user);
        Map<String, Integer> stats = claimService.calculateClaimStats(allClaims);

        model.addAttribute("claims", claimPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", claimPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        model.addAttribute("statPending", stats.get("pending"));
        model.addAttribute("statApproved", stats.get("approved"));
        model.addAttribute("statRejected", stats.get("rejected"));

        return "user/claims";
    }

    @GetMapping({"/user/submit-claim", "/user/submit-claim.html"})
    public String showSubmitClaimForm(Model model, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        List<UserPolicy> activePolicies = userPolicyRepository.findByUser(user).stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .toList();

        model.addAttribute("policies", activePolicies);
        return "user/submit-claim";
    }

    @PostMapping("/user/claims/submit")
    public String submitClaim(@RequestParam Long policyId,
                              @RequestParam BigDecimal amount,
                              @RequestParam String description,
                              @RequestParam LocalDate incidentDate, // Ensure this is LocalDate
                              HttpServletRequest request,
                              RedirectAttributes ra) {
        
        // Check for Negative Amount
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            ra.addFlashAttribute("error", "Error: Claim amount cannot be negative.");
            return "redirect:/user/claims";
        }

       
        if (incidentDate.isAfter(LocalDate.now())) {
            ra.addFlashAttribute("error", "Error: Incident date cannot be in the future.");
            return "redirect:/user/claims"; // Or redirect back to submit-claim
        }
        
  
        Claim created = claimService.submitClaim(policyId, amount, description, incidentDate);

        if (created != null) {
            ra.addFlashAttribute("success", "Claim submitted successfully! ID: CLM-" + created.getId());
        } else {
            ra.addFlashAttribute("error", "Policy not found.");
        }
        return "redirect:/user/claims";
    }
}