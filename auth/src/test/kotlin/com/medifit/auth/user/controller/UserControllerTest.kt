package com.medifit.auth.user.controller

import com.medifit.auth.auth.service.AuthService
import com.medifit.auth.config.JwtTokenUtil
import com.medifit.auth.user.model.Role
import com.medifit.auth.user.model.User
import com.medifit.auth.user.service.UserService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
internal class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var jwtTokenUtil: JwtTokenUtil

    private val mockUser = User(
        id = 1,
        username = "test",
        password = "test",
        role = Role.PATIENT
    )

    @Test
    @DisplayName("Should return current user")
    @WithMockUser(username = "1")
    fun shouldReturnCurrentUser() {
        every { userService.getUserById(1) } returns mockUser

        mockMvc.perform(get("/users/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath(("$.id")).value("EXTERNAL_1"))
    }

    @Test
    @DisplayName("Should return 404 if user not found")
    @WithMockUser(username = "1")
    fun shouldReturn404() {
        every { userService.getUserById(1) } returns null

        mockMvc.perform(get("/users/me"))
            .andExpect(status().isNotFound)
    }
}
