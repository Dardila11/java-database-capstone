package com.project.back_end.shared;

public interface AdminUserLookup {
    String getPasswordByUsername(String username);
    void updatePassword(String username, String encodedPassword);
}
