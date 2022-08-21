package com.medifit.apigateway

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.bind.annotation.RestController

@RestController
class RouteConfiguration(private val jwtFilter: JwtTokenAuthenticationFilter) {

    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("auth") { r -> r.path("/api/auth/**").uri("lb://auth-service") }
            .route("user") { r ->
                r.path("/api/users/**").filters { f -> f.filter(jwtFilter) }.uri("lb://auth-service")
            }
            .route("doctors") { r ->
                r.path("/api/doctors/**").filters { f -> f.filter(jwtFilter) }.uri("lb://scheduling-service")
            }
            .route("timeslots") { r ->
                r.path("/api/timeslots/**").filters { f -> f.filter(jwtFilter) }.uri("lb://scheduling-service")
            }
            .route("appointments") { r ->
                r.path("/api/appointments/**").filters { f -> f.filter(jwtFilter) }.uri("lb://scheduling-service")
            }
            .route("surveys") { r ->
                r.path("/api/surveys/**").filters { f -> f.filter(jwtFilter) }.uri("lb://ratings-service")
            }
            .build()
    }

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.cors().disable().csrf().disable().httpBasic().disable().authorizeExchange()
            .anyExchange().permitAll()
            .and().build()
    }


}
