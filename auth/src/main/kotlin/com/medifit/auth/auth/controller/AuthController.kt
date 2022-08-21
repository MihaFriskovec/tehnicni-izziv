package com.medifit.auth.auth.controller

import com.medifit.auth.auth.dto.JwtDto
import com.medifit.auth.auth.dto.LoginDto
import com.medifit.auth.auth.dto.SignupDto
import com.medifit.auth.auth.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/signup")
    fun createUser(@Valid @RequestBody signupDto: SignupDto): ResponseEntity<Void> {
        authService.signup(signupDto)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginDto: LoginDto): ResponseEntity<JwtDto> {
        val user = authService.login(loginDto)
        return ResponseEntity.ok(JwtDto(authService.issueJwt(user)))
    }
}
