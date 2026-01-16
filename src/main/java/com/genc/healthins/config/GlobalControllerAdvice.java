package com.genc.healthins.config;

import com.genc.healthins.model.User;
import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        if (session == null) return null;
        Object obj = session.getAttribute("loggedInUser");
        if (obj instanceof User) return (User) obj;
        return null;
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        // Pass the error message to the view (e.g., "Invalid Policy ID")
        model.addAttribute("error", e.getMessage());
        
        // Return the name of your error page (Make sure you have an 'error.html' file)
        return "error"; 
    }
    
}