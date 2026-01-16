package com.genc.healthins.service;

import com.genc.healthins.model.Payment;
import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentService {

    Payment save(Payment payment);
    List<Payment> findAll();
    
   
    
    /**
     * Handles the purchase of a policy: creates the UserPolicy record and the Payment record.
     */
    void processPayment(User user, Long planId, BigDecimal amount);

    /**
     * Retrieves all payments associated with a specific user.
     */
    List<Payment> findPaymentsByUser(User user);
}