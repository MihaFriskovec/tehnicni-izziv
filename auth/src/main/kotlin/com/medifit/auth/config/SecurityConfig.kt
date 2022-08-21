package com.medifit.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig(private val jwtTokenFilter: JwtTokenFilter) {

    @Bean
    fun encoder(): PasswordEncoder {
        return BCryptPasswordEncoder(11)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.cors().and().csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();
        http.formLogin().disable().httpBasic().disable();

        http.authorizeHttpRequests()
            .antMatchers("/actuator/**").permitAll()
            .antMatchers(
                "/v3/api-docs/**",
                "/configuration/ui",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**"
            ).permitAll()
            .antMatchers("/auth/**").permitAll()
            .anyRequest().authenticated()

        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
