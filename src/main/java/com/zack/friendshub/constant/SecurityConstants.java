package com.zack.friendshub.constant;

public final class SecurityConstants {
    public static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml"
    };

    private SecurityConstants() {
    }
}
