package com.genc.healthins.controller;

import com.genc.healthins.model.User;
import com.genc.healthins.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
  

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
       
    }

    @GetMapping({"/user/payments", "/user/payments.html"})
    public String payments(
                           Model model,
                           HttpServletRequest request) {

        User user = (User) request.getSession().getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

      

        // Payment History: Fetched cleanly via service
        model.addAttribute("payments", paymentService.findPaymentsByUser(user));
        
        return "user/payments";
    }

    @PostMapping("/user/payments/pay")
    public String processPayment(@RequestParam("planId") Long planId,
                                 @RequestParam("amount") BigDecimal amount,
                                 HttpServletRequest request,
                                 RedirectAttributes ra) {

        User user = (User) request.getSession().getAttribute("loggedInUser");
        
        if (user != null) {
            paymentService.processPayment(user, planId, amount);
            ra.addFlashAttribute("success", "Payment successful! Policy activated.");
            return "redirect:/user/policies";
        }
        
        return "redirect:/user/marketplace";
    }
}