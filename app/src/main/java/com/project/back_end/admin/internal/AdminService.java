package com.project.back_end.admin.internal;

import com.project.back_end.admin.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final AdminRepository adminRepository;
    private final ApplicationEventPublisher events;

    public AdminService(AdminRepository adminRepository, ApplicationEventPublisher events) {
        this.adminRepository = adminRepository;
        this.events = events;
    }

    public Admin findByUsername(String username){
        return adminRepository.findByUsername(username);
    }

}
