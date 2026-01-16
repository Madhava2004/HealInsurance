package com.genc.healthins.controller;

import com.genc.healthins.model.Notification;
import com.genc.healthins.model.User;
import com.genc.healthins.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // --- USER: VIEW NOTIFICATIONS ---
    @GetMapping({"/user/notifications", "/user/notifications.html"})
    public String notifications(Model model, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        // Service returns them sorted by date desc
        List<Notification> notifications = notificationService.findByUser(user);

        model.addAttribute("notifications", notifications);
        return "user/notifications";
    }

    // --- USER: CLEAR NOTIFICATIONS ---
    @PostMapping("/user/notifications/clear")
    public String clearNotifications(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        if (user != null) {
            notificationService.markAllAsRead(user);
        }
        return "redirect:/user/notifications";
    }

    // --- AGENT: SEND NOTIFICATION TO CUSTOMER ---
    @PostMapping("/agent/customers/notify")
    @ResponseBody
    public ResponseEntity<?> notifyCustomer(@RequestParam Long userId, @RequestParam String message) {
        boolean success = notificationService.sendNotification(userId, message);
        
        if (success) {
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
    }
}