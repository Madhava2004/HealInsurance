package com.genc.healthins.service;

import com.genc.healthins.model.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    // Basic CRUD
    Optional<User> findByEmail(String email);
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
  
    User findByUsername(String username);
    

    
    // For User Controller
    Map<String, Object> getUserDashboardStats(User user);

    // For Agent Controller
    List<User> findCustomersByAgentId(Integer agentId);
    Map<String, Object> getAgentDashboardStats(User agent);
    List<Map<String, Object>> getAgentCustomersWithStats(User agent);

    // For Admin Controller
    void assignAgent(Long userId, Long agentId);
    void updateUser(Long id, String username, String phone, String role);
    List<Map<String, Object>> getRecentUsersWithStats();
}