package com.aspero.collaborativeboard.domain.auth.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
