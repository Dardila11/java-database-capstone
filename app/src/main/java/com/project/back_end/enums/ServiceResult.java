package com.project.back_end.enums;

public enum ServiceResult {
    SUCCESS,      // was  1 — operation completed successfully
    CONFLICT,     // was  0 — slot taken / save failed / appointment not pending
    NOT_FOUND,    // was -1 — entity not found (or duplicate on create operations)
    UNAUTHORIZED  // was -2 — token owner does not match resource owner
}
