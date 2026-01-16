package com.genc.healthins.service.impl;

import com.genc.healthins.model.Payment;
import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.repository.PaymentRepository;
import com.genc.healthins.repository.PolicyRepository;
import com.genc.healthins.repository.UserPolicyRepository;
import com.genc.healthins.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;       
    private final UserPolicyRepository userPolicyRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              PolicyRepository policyRepository,
                              UserPolicyRepository userPolicyRepository) {
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.userPolicyRepository = userPolicyRepository;
    }


    @Override
    public Payment save(Payment payment) { 
    	return paymentRepository.save(payment); 
    }

    @Override
    public List<Payment> findAll() { 
    	return paymentRepository.findAll(); 
    }

    @Override
    public List<Payment> findPaymentsByUser(User user) {
        // Moving the filter logic from Controller to Service
        return findAll().stream()
                .filter(p -> p.getPolicy() != null
                        && p.getPolicy().getUser() != null
                        && p.getPolicy().getUser().getId() == user.getId())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processPayment(User user, Long planId, BigDecimal amount) {
        Policy template = policyRepository.findById(planId).orElse(null);

        if (template != null && user != null) {
            // 1. Create UserPolicy
            UserPolicy myPolicy = new UserPolicy();


            String uniqueUserPolicyId = template.getPolicyNumber(); 

            myPolicy.setPolicyNumber(uniqueUserPolicyId);
            myPolicy.setCoverageType(template.getCoverageType());
            myPolicy.setCoverageAmount(template.getCoverageAmount());
            myPolicy.setPremiumAmount(amount);
            myPolicy.setOriginalPlanId(template.getId());
            myPolicy.setStartDate(LocalDateTime.now());
            myPolicy.setEndDate(LocalDateTime.now().plusYears(1));
            myPolicy.setStatus("ACTIVE");
            myPolicy.setPaymentStatus("PAID");
            myPolicy.setUser(user);

            // Save Policy
            myPolicy = userPolicyRepository.saveAndFlush(myPolicy);

            // 2. Create Payment
            Payment payment = new Payment();
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentAmount(amount);
            payment.setPaymentStatus("COMPLETED");
            payment.setPolicy(myPolicy);
            payment.setUser(user);
            paymentRepository.save(payment);
        }
    }
}