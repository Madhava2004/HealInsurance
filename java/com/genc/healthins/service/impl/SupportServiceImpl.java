package com.genc.healthins.service.impl;

import com.genc.healthins.model.Notification;
import com.genc.healthins.model.SupportResponse;
import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;
import com.genc.healthins.repository.SupportResponseRepository;
import com.genc.healthins.repository.SupportTicketRepository;
import com.genc.healthins.service.NotificationService;
import com.genc.healthins.service.SupportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupportServiceImpl implements SupportService {

    private final SupportTicketRepository repository;
    private final SupportResponseRepository responseRepository;
    private final NotificationService notificationService;      

    public SupportServiceImpl(SupportTicketRepository repository,
                              SupportResponseRepository responseRepository,
                              NotificationService notificationService) {
        this.repository = repository;
        this.responseRepository = responseRepository;
        this.notificationService = notificationService;
    }

    @Override 
    public List<SupportTicket> findByUser(User user) { 
    	return repository.findByUser(user); }
    
    @Override 
    public List<SupportTicket> findAll() { 
    	return repository.findAll(); }
    
    @Override 
    public SupportTicket save(SupportTicket ticket) { 
    	return repository.save(ticket); }
    
    @Override 
    public Optional<SupportTicket> findById(Long id) { 
    	return repository.findById(id); }
    @Override 
    public long countByStatus(String status) { 
    	return repository.countByTicketStatus(status); }
    @Override 
    public long countByPriority(String priority) { 
    	return repository.countByPriority(priority); }
    @Override 
    public List<SupportTicket> findTop1ByTicketStatusOrderByCreatedDateDesc(String status) { 
    	return repository.findTop1ByTicketStatusOrderByCreatedDateDesc(status); }
    @Override 
    public List<SupportTicket> getLatestOpenTicket() { 
    	return repository.findTop1ByTicketStatusOrderByCreatedDateDesc("OPEN"); }
    @Override 
    public List<SupportTicket> findByUserAssignedAgentId(Long agentId) { 
    	return repository.findByUser_AssignedAgentId(agentId); }

    // --- Business Logic ---

    @Override
    public SupportTicket createTicket(String subject, String category, String priority, String description, User user) {
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject(subject);
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setIssueDescription(description);
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setTicketStatus("OPEN");
        ticket.setUser(user);

        SupportTicket savedTicket = repository.save(ticket);

        // Notify User
        Notification n = new Notification();
        n.setUser(user);
        n.setType("TICKET_CREATED");
        n.setMessage("Your support ticket #" + savedTicket.getId() + " has been created successfully.");
        n.setTicketId(savedTicket.getId());
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationService.save(n);

        return savedTicket;
    }

    @Override
    public void addResponse(Long ticketId, String message, User responder) {
        repository.findById(ticketId).ifPresent(ticket -> {
            // Create Response
            SupportResponse response = new SupportResponse();
            response.setSupportTicket(ticket);
            response.setResponder(responder);
            response.setMessage(message);
            response.setCreatedAt(LocalDateTime.now());
            responseRepository.save(response);

            // Notify Ticket Owner
            Notification n = new Notification();
            n.setUser(ticket.getUser());
            n.setType("SUPPORT_REPLY");
            n.setTicketId(ticket.getId());
            
            String roleLabel = "ADMIN".equalsIgnoreCase(responder.getRole()) ? "Admin" : "Agent";
            n.setMessage(roleLabel + " replied to Ticket #" + ticket.getId());
            
            n.setRead(false);
            n.setCreatedAt(LocalDateTime.now());
            notificationService.save(n);
        });
    }

    @Override
    public void resolveTicket(Long ticketId) {
        repository.findById(ticketId).ifPresent(ticket -> {
            ticket.setTicketStatus("RESOLVED");
            ticket.setResolvedDate(LocalDateTime.now());
            repository.save(ticket);

            // Notify Ticket Owner
            Notification resolveNote = new Notification();
            resolveNote.setUser(ticket.getUser());
            resolveNote.setType("TICKET_RESOLVED");
            resolveNote.setMessage("Your ticket #" + ticket.getId() + " has been marked as RESOLVED.");
            resolveNote.setCreatedAt(LocalDateTime.now());
            resolveNote.setRead(false);
            notificationService.save(resolveNote);
        });
    }
}