package com.genc.healthins.service;

import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;
import java.util.List;
import java.util.Optional;

public interface SupportService {
    // Basic CRUD / Search
    List<SupportTicket> findByUser(User user);
    List<SupportTicket> findAll();
    SupportTicket save(SupportTicket ticket);
    Optional<SupportTicket> findById(Long id);
    long countByStatus(String status);
    long countByPriority(String priority);
    List<SupportTicket> findTop1ByTicketStatusOrderByCreatedDateDesc(String status);
    List<SupportTicket> getLatestOpenTicket();
    List<SupportTicket> findByUserAssignedAgentId(Long agentId);

  
    SupportTicket createTicket(String subject, String category, String priority, String description, User user);
    
    void addResponse(Long ticketId, String message, User responder);
    
    void resolveTicket(Long ticketId);
}