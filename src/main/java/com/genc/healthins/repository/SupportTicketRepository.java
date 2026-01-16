package com.genc.healthins.repository;

import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUser(User user);
    long countByTicketStatus(String ticketStatus);
    
    
    long countByPriority(String priority);
    
    long countByUserAndTicketStatus(User user, String ticketStatus);
    List<SupportTicket> findTop1ByTicketStatusOrderByCreatedDateDesc(String ticketStatus);
    // List<SupportTicket> findByAgentId(Long agentId);
    List<SupportTicket> findByUser_AssignedAgentId(Long agentId); // Query method to find tickets by agent ID

}