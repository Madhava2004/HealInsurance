package com.genc.healthins.service.impl;

import com.genc.healthins.model.Notification;
import com.genc.healthins.model.User;
import com.genc.healthins.repository.NotificationRepository;
import com.genc.healthins.repository.UserRepository;
import com.genc.healthins.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository; // Injected for sendNotification

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> findByUser(User user) {
        // Already sorted by Descending Date via Repository naming convention
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Notification> findUnreadByUser(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> list = notificationRepository.findByUserAndIsReadFalse(user);
        if (!list.isEmpty()) {
            list.forEach(n -> n.setRead(true));
            notificationRepository.saveAll(list);
        }
    }

    @Override
    public long countUnread(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public boolean sendNotification(Long userId, String message) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            Notification n = new Notification();
            n.setUser(userOpt.get());
            n.setMessage(message);
            n.setType("REMINDER"); // Default type for agent manual notifications
            n.setCreatedAt(LocalDateTime.now());
            n.setRead(false);
            notificationRepository.save(n);
            return true;
        }
        return false;
    }
}