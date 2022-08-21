package com.medifit.ratings.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class JwtTokenFilter(private val jwtTokenUtil: JwtTokenUtil) : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (header == null || header.isBlank() || !header.startsWith("Bearer ")) {
            return filterChain.doFilter(request, response)
        }

        val token = header.split(" ")[1].trim()

        if (!jwtTokenUtil.validateToken(token)) {
            return filterChain.doFilter(request, response)
        }

        val userId = jwtTokenUtil.getUserId(token)
        val role = SimpleGrantedAuthority(jwtTokenUtil.getRole(token))

        val authentication = UsernamePasswordAuthenticationToken(
            userId,
            token,
            mutableListOf(role)
        )

        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }
}
