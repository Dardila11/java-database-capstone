package com.project.back_end.admin.internal;

import com.project.back_end.shared.RoleValidator;
import org.springframework.stereotype.Component;

@Component
class AdminRoleValidator implements RoleValidator {

    private final AdminRepository adminRepository;

    AdminRoleValidator(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public String role() {
        return "admin";
    }

    @Override
    public boolean isValidSubject(String username) {
        return adminRepository.findByUsername(username) != null;
    }
}
