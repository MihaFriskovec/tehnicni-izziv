package com.medifit.auth.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.medifit.auth.auth.dto.LoginDto
import com.medifit.auth.auth.dto.SignupDto
import com.medifit.auth.auth.service.AuthService
import com.medifit.auth.config.JwtTokenUtil
import com.medifit.auth.config.SecurityConfig
import com.medifit.auth.user.model.Role
import com.medifit.auth.user.model.User
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Import(SecurityConfig::class)
@WebMvcTest(AuthController::class)
internal class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var authService: AuthService

    @MockkBean
    private lateinit var jwtTokenUtil: JwtTokenUtil

    private val mockUser = User(
        id = 1,
        username = "test",
        password = "test",
        role = Role.PATIENT
    )

    @Test
    @DisplayName("Should create user")
    fun shouldCreateUser() {
        val signupDto = SignupDto(username = "test", password = "test")

        every { authService.signup(signupDto) } returns mockUser

        mockMvc.perform(
            post("/auth/signup").content(ObjectMapper().writeValueAsString(signupDto))
                .contentType(MediaType.APPLICATION_JSON).with(csrf())
        ).andExpect(status().isCreated)
    }

    @Test
    @DisplayName("Should log user in and issue JWT")
    fun shouldLogUserIn() {
        val loginDto = LoginDto(username = "test", password = "test")

        every { authService.login(loginDto) } returns mockUser
        every { authService.issueJwt(mockUser) } returns "TOKEN"

        mockMvc.perform(
            post("/auth/login").content(ObjectMapper().writeValueAsString(loginDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.jwt").value("TOKEN"))
    }
}
