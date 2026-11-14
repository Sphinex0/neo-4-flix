package com.neo.api_gateway.security.securityConfig;


import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import com.neo.api_gateway.security.jwt.JwtAuthenticationFilter;

@Component
public class UserClaimsGatewayFilter {
    public ServerRequest apply(ServerRequest request) {

        String userId = (String) request.servletRequest()
                .getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);

        String role = (String) request.servletRequest()
                .getAttribute(JwtAuthenticationFilter.USER_ROLE_ATTRIBUTE);

        if (userId == null || role == null) {
            return request;
        }

        return ServerRequest.from(request)
                .headers(headers -> {
                    headers.remove("Neo-User-Id");
                    headers.remove("Neo-User-Role");

                    headers.add("Neo-User-Id", userId);
                    headers.add("Neo-User-Role", role);
                })
                .build();
    }
}
