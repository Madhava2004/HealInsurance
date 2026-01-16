package com.genc.healthins.controller;

import com.genc.healthins.model.User;
import com.genc.healthins.repository.UserPolicyRepository;
import com.genc.healthins.service.NotificationService;
import com.genc.healthins.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class UserController {

    private final UserPolicyRepository userPolicyRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    public UserController(UserPolicyRepository userPolicyRepository,
                          NotificationService notificationService,
                          UserService userService) {
        this.userPolicyRepository = userPolicyRepository;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // --- DASHBOARD ---
    @GetMapping({"/user/dashboard", "/user/dashboard.html"})
    public String dashboard(Model model, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        if(user == null) return "redirect:/login";

        // Fetch aggregated stats from Service
        Map<String, Object> stats = userService.getUserDashboardStats(user);
        
        // Fetch specific lists for display
        var policies = userPolicyRepository.findByUser(user);
        var allNotifications = notificationService.findByUser(user);
        if (allNotifications != null) {
            allNotifications.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));
        }

        model.addAttribute("userName", user.getUsername());
        model.addAttribute("policies", policies);
        model.addAttribute("notifications", allNotifications.stream().limit(5).toList());
        model.addAttribute("unreadCount", allNotifications.stream().filter(n -> !n.isRead()).count());
        
        // Apply stats to model
        model.addAllAttributes(stats);

        return "user/dashboard";
    }
}