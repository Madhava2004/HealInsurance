package com.genc.healthins.repository;


import com.genc.healthins.model.Payment;
import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPolicy(Policy policy);
    long countByUser(User user);
}