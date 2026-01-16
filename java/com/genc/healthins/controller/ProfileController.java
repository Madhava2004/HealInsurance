package com.genc.healthins.controller;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.repository.ClaimRepository;
import com.genc.healthins.repository.UserPolicyRepository;
import com.genc.healthins.service.ClaimService;
import com.genc.healthins.service.PolicyService;
import com.genc.healthins.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

	private final UserService userService;
    private final PolicyService policyService;
    private final ClaimService claimService;
    private final PasswordEncoder passwordEncoder;
    private final UserPolicyRepository userPolicyRepository;
    private final ClaimRepository claimRepository;

    public ProfileController(UserService userService, PolicyService policyService, 
                             ClaimService claimService, PasswordEncoder passwordEncoder,
                             UserPolicyRepository userPolicyRepository,
                             ClaimRepository claimRepository) {
        this.userService = userService;
        this.policyService = policyService;
        this.claimService = claimService;
        this.passwordEncoder = passwordEncoder;
        this.userPolicyRepository = userPolicyRepository;
        this.claimRepository = claimRepository;
    }

    // ==========================================
    // ADMIN PROFILE
    // ==========================================

    @GetMapping({"/admin/profile", "/admin/profile.html"})
    public String adminProfile(Model model, HttpServletRequest request) {
        User adminUser = (User) request.getSession().getAttribute("loggedInUser");
        if (adminUser == null) return "redirect:/login";
        
        userService.findById((long) adminUser.getId()).ifPresent(u -> model.addAttribute("user", u));
        return "admin/profile";
    }

    @PostMapping("/admin/profile/update")
    public String updateAdminProfile(@ModelAttribute User updatedUser, HttpServletRequest request, RedirectAttributes ra) {
        User currentUser = (User) request.getSession().getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/login";

        currentUser.setUsername(updatedUser.getUsername());
        currentUser.setEmail(updatedUser.getEmail());
        currentUser.setPhone(updatedUser.getPhone());
        userService.save(currentUser);
        request.getSession().setAttribute("loggedInUser", currentUser);

        ra.addFlashAttribute("success", "Admin profile updated successfully!");
        return "redirect:/admin/profile";
    }

    @PostMapping("/admin/profile/change-password")
    public String changeAdminPassword(@RequestParam String currentPassword,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword,
                                      HttpServletRequest request,
                                      RedirectAttributes ra) {
        User admin = (User) request.getSession().getAttribute("loggedInUser");
        if (admin == null) return "redirect:/login";

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("passwordError", "New passwords do not match");
            return "redirect:/admin/profile";
        }
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            ra.addFlashAttribute("passwordError", "Current password is incorrect");
            return "redirect:/admin/profile";
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        userService.save(admin);
        ra.addFlashAttribute("passwordSuccess", "Password updated successfully");
        return "redirect:/admin/profile";
    }

    // ==========================================
    // AGENT PROFILE
    // ==========================================

    @GetMapping({"/agent/profile", "/agent/profile.html"})
    public String agentProfile(HttpServletRequest request, Model model) {
        User agent = (User) request.getSession().getAttribute("loggedInUser");
        if (agent == null) return "redirect:/login";

        // Logic for statistics REMOVED as requested. 
        // Just passing the agent object.
        model.addAttribute("agent", agent);
        
        return "agent/profile";
    }

    @PostMapping("/agent/profile/update")
    public String updateAgentProfile(@ModelAttribute User updatedAgent, HttpServletRequest request, RedirectAttributes ra) {
        User currentAgent = (User) request.getSession().getAttribute("loggedInUser");
        if (currentAgent == null) return "redirect:/login";

        currentAgent.setUsername(updatedAgent.getUsername());
        currentAgent.setEmail(updatedAgent.getEmail());
        currentAgent.setPhone(updatedAgent.getPhone());
        userService.save(currentAgent);
        request.getSession().setAttribute("loggedInUser", currentAgent);

        ra.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/agent/profile";
    }

    @PostMapping("/agent/profile/change-password")
    public String changeAgentPassword(@RequestParam String currentPassword,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword,
                                      HttpServletRequest request,
                                      RedirectAttributes ra) {
        User agent = (User) request.getSession().getAttribute("loggedInUser");
        if (agent == null) return "redirect:/login";

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("passwordError", "New passwords do not match");
            return "redirect:/agent/profile";
        }
        if (!passwordEncoder.matches(currentPassword, agent.getPassword())) {
            ra.addFlashAttribute("passwordError", "Current password is incorrect");
            return "redirect:/agent/profile";
        }
        agent.setPassword(passwordEncoder.encode(newPassword));
        userService.save(agent);

        ra.addFlashAttribute("passwordSuccess", "Password updated successfully");
        return "redirect:/agent/profile";
    }

    // ==========================================
    // USER PROFILE
    // ==========================================

    @GetMapping({"/user/profile", "/user/profile.html"})
    public String userProfile(Model model, HttpServletRequest request) {
        User sessionUser = (User) request.getSession().getAttribute("loggedInUser");
        if (sessionUser == null) return "redirect:/login";
        
        User user = userService.findByUsername(sessionUser.getUsername());
        if (user == null) user = sessionUser;
        
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/user/profile/update")
    public String updateUserProfile(@ModelAttribute User formData, HttpServletRequest request, RedirectAttributes ra) {
        User sessionUser = (User) request.getSession().getAttribute("loggedInUser");
        if (sessionUser == null) return "redirect:/login";
        
        User existingUser = userService.findByUsername(sessionUser.getUsername());
        if (existingUser != null) {
            existingUser.setUsername(formData.getUsername());
            existingUser.setEmail(formData.getEmail());
            existingUser.setPhone(formData.getPhone());
            User updatedUser = userService.save(existingUser);
            request.getSession().setAttribute("loggedInUser", updatedUser);
            ra.addFlashAttribute("success", "Profile updated successfully!");
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/user/profile/change-password")
    public String changeUserPassword(@RequestParam String currentPassword, @RequestParam String newPassword, @RequestParam String confirmPassword, HttpServletRequest request, RedirectAttributes ra) {
        User sessionUser = (User) request.getSession().getAttribute("loggedInUser");
        if (sessionUser == null) return "redirect:/login";
        
        User user = userService.findByUsername(sessionUser.getUsername());
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("passwordError", "New passwords do not match");
            return "redirect:/user/profile";
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            ra.addFlashAttribute("passwordError", "Current password is incorrect");
            return "redirect:/user/profile";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
        ra.addFlashAttribute("passwordSuccess", "Password updated successfully");
        return "redirect:/user/profile";
    }
}