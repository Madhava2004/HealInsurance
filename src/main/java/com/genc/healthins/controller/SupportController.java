package com.genc.healthins.controller;

import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;
import com.genc.healthins.service.SupportResponseService;
import com.genc.healthins.service.SupportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class SupportController {

    private final SupportService ticketService;
    private final SupportResponseService responseService;

    public SupportController(SupportService ticketService, SupportResponseService responseService) {
        this.ticketService = ticketService;
        this.responseService = responseService;
    }

    @GetMapping({"/user/support","/user/support.html"})
    public String userSupport(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("tickets", ticketService.findByUser(user));
        return "user/support";
    }

    @GetMapping({"/user/create-ticket", "/user/create-ticket.html"})
    public String createTicketForm(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "user/create-ticket";
    }

    @PostMapping("/user/support/create")
    public String createTicket(@RequestParam String subject, @RequestParam String category,
                               @RequestParam String priority, @RequestParam String issue_description,
                               HttpServletRequest request, RedirectAttributes ra) {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;
        if (user == null) return "redirect:/login";

        try {
            SupportTicket ticket = ticketService.createTicket(subject, category, priority, issue_description, user);
            ra.addFlashAttribute("success", "Ticket created successfully! Ticket ID: TKT-" + ticket.getId());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Database error: " + e.getMessage());
        }
        return "redirect:/user/support";
    }

    @GetMapping({"/admin/support","/admin/support.html"})
    public String adminSupport(Model model) {
        model.addAttribute("tickets", ticketService.findAll());
        model.addAttribute("openCount", ticketService.countByStatus("OPEN"));
        model.addAttribute("highPriorityCount", ticketService.countByPriority("High"));
        model.addAttribute("resolvedCount", ticketService.countByStatus("RESOLVED"));
        return "admin/support";
    }

    @GetMapping("/support/view/{id}")
    public String viewTicket(@PathVariable Long id, Model model) {
        var opt = ticketService.findById(id);
        if (opt.isEmpty()) return "not-found";
        model.addAttribute("ticket", opt.get());
        model.addAttribute("responses", responseService.findByTicket(opt.get()));
        return "user/view";
    }

    @GetMapping("/agent/support")
    public String agentSupport(Model model, HttpServletRequest request) {
        User agent = (User) request.getSession().getAttribute("loggedInUser");
        if (agent == null) return "redirect:/login";

        List<SupportTicket> tickets = ticketService.findByUserAssignedAgentId(Long.valueOf(agent.getId()));
        model.addAttribute("tickets", tickets);
        model.addAttribute("openCount", tickets.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getTicketStatus())).count());
        model.addAttribute("highPriorityCount", tickets.stream().filter(t -> "High".equalsIgnoreCase(t.getPriority())).count());
        model.addAttribute("resolvedCount", tickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getTicketStatus())).count());
        return "agent/support";
    }

    @PostMapping({"/admin/support/submit-response", "/agent/support/submit-response"})
    public String handleResponse(@RequestParam("ticketId") Long ticketId,
                                 @RequestParam("message") String message,
                                 HttpServletRequest request,
                                 RedirectAttributes ra) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        if (user != null) {
            ticketService.addResponse(ticketId, message, user);
            ra.addFlashAttribute("success", "Reply sent successfully");
            
            if ("ADMIN".equalsIgnoreCase(user.getRole())) return "redirect:/admin/support";
            if ("AGENT".equalsIgnoreCase(user.getRole())) return "redirect:/agent/support";
        }
        return "redirect:/login";
    }

    @PostMapping("/support/{id}/resolve")
    public String resolveTicket(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        if (user != null) {
            ticketService.resolveTicket(id);
            ra.addFlashAttribute("success", "Ticket resolved successfully.");
            
            if ("ADMIN".equalsIgnoreCase(user.getRole())) return "redirect:/admin/support";
            if ("AGENT".equalsIgnoreCase(user.getRole())) return "redirect:/agent/support";
        }
        return "redirect:/user/support";
    }
}