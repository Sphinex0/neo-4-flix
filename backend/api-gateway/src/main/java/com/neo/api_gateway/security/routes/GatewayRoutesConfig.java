package com.neo.api_gateway.security.routes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import com.neo.api_gateway.security.securityConfig.UserClaimsGatewayFilter;

import lombok.RequiredArgsConstructor;


@Configuration
@RequiredArgsConstructor
public class GatewayRoutesConfig {
    private final UserClaimsGatewayFilter userClaimsGatewayFilter;


    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {

        RouterFunction<ServerResponse> userServiceRoute = route("user-service-route")
                .route(
                        request -> request.path().startsWith("/api/auth") ||
                                request.path().startsWith("/api/users"),
                        http())
                .before(userClaimsGatewayFilter::apply)
                .filter(lb("user-service"))
                .build();

        RouterFunction<ServerResponse> movieServiceRoute = route("movie-service-route")
                .route(
                        request -> request.path().startsWith("/api/movies") ||
                                request.path().startsWith("/api/genre"),
                        http())
                .before(userClaimsGatewayFilter::apply)
                .filter(lb("movie-service"))
                .build();

        RouterFunction<ServerResponse> ratingServiceRoute = route("rating-service-route")
                .route(
                        request -> request.path().startsWith("/api/ratings"),
                        http())
                .before(userClaimsGatewayFilter::apply)
                .filter(lb("rating-service"))
                .build();

        RouterFunction<ServerResponse> recommendationServiceRoute = route("recommendation-service-route")
                .route(
                        request -> request.path().startsWith("/api/recommendations"),
                        http())
                .before(userClaimsGatewayFilter::apply)
                .filter(lb("recommendation-service"))
                .build();


        return userServiceRoute
                .and(movieServiceRoute)
                .and(ratingServiceRoute)
                .and(recommendationServiceRoute);
    }
}
