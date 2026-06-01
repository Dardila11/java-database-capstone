package com.project.back_end.admin.internal;

import com.project.back_end.admin.Admin;
import com.project.back_end.shared.AdminUserLookup;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class AdminService implements AdminUserLookup {

    private final AdminRepository adminRepository;
    private final ApplicationEventPublisher events;

    public AdminService(AdminRepository adminRepository, ApplicationEventPublisher events) {
        this.adminRepository = adminRepository;
        this.events = events;
    }

    @Override
    public String getPasswordByUsername(String username) {
        Admin admin = adminRepository.findByUsername(username);
        return admin != null ? admin.getPassword() : null;
    }

    @Override
    public void updatePassword(String username, String encodedPassword) {
        Admin admin = adminRepository.findByUsername(username);
        if (admin != null) {
            admin.setPassword(encodedPassword);
            adminRepository.save(admin);
        }
    }
}
