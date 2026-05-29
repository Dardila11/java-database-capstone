package com.project.back_end.admin.internal;

import com.project.back_end.admin.Admin;
import com.project.back_end.exceptions.InvalidCredentialsException;
import com.project.back_end.shared.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AdminService adminService, AdminRepository adminRepository,
                       TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.adminRepository = adminRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public String validateAdminLogin(String username, String password) {
        Admin admin = adminService.findByUsername(username);
        if (admin == null) throw new InvalidCredentialsException("Invalid username or password");

        String stored = admin.getPassword();
        if (passwordEncoder.matches(password, stored)) {
            // already BCrypt-hashed
        } else if (password.equals(stored)) {
            admin.setPassword(passwordEncoder.encode(password));
            adminRepository.save(admin);
        } else {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return tokenService.generateToken(admin.getUsername());
    }
}
