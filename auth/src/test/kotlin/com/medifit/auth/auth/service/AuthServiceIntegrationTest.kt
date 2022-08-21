package com.medifit.auth.auth.service

import com.medifit.auth.auth.dto.LoginDto
import com.medifit.auth.auth.dto.SignupDto
import com.medifit.auth.config.JwtTokenUtil
import com.medifit.auth.user.model.Role
import com.medifit.auth.user.model.User
import com.medifit.auth.user.model.UserRepository
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.web.server.ResponseStatusException

@DataJpaTest
internal class AuthServiceIntegrationTest(@Value("\${jwt.secret}") private val secret: String) {
    @Autowired
    private lateinit var userRepository: UserRepository

    private val jwtTokenUtil: JwtTokenUtil = JwtTokenUtil(secret)

    private val meterRegistry: MeterRegistry = SimpleMeterRegistry()

    private val passwordEncoder = NoOpPasswordEncoder.getInstance()

    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        authService = AuthServiceImpl(userRepository, passwordEncoder, jwtTokenUtil, meterRegistry)
    }

    @Test
    @DisplayName("Should create user with base params")
    fun shouldCreateNewUser() {
        val signupDto = SignupDto(username = "test", password = "test")

        val createdUser = authService.signup(signupDto)

        assertNotNull(createdUser)
        assertNotNull(createdUser.id)
        assertEquals(signupDto.username, createdUser.username)
        assertEquals(Role.PATIENT, createdUser.role)
        assertTrue(createdUser.active)
    }

    @Test
    @DisplayName("Should create user with email and custom role")
    fun shouldCreateNewUserWithEmailAndRole() {
        val signupDto = SignupDto(username = "test", password = "test", email = "test@test.si", role = "DOCTOR")

        val createdUser = authService.signup(signupDto)

        assertNotNull(createdUser)
        assertNotNull(createdUser.id)
        assertEquals(signupDto.username, createdUser.username)
        assertEquals(signupDto.email, createdUser.email)
        assertEquals(Role.DOCTOR, createdUser.role)
        assertTrue(createdUser.active)
    }

    @Test
    @DisplayName("Should log user in")
    fun shouldLogUserIn() {
        userRepository.save(User(username = "test", password = passwordEncoder.encode("test"), role = Role.PATIENT))

        val loginDto = LoginDto(username = "test", password = "test")

        val user = authService.login(loginDto)

        assertNotNull(user)
        assertEquals(loginDto.username, user.username)
    }

    @Test
    @DisplayName("Should throw error - user does not exist")
    fun shouldThrow_noUser() {
        val loginDto = LoginDto(username = "test", password = "test")

        val ex = assertThrows<ResponseStatusException> { authService.login(loginDto) }

        assertEquals("User with given username does not exists.", ex.reason)
    }

    @Test
    @DisplayName("Should throw error - invalid password")
    fun shouldThrow_invalidPassword() {
        userRepository.save(User(username = "test", password = passwordEncoder.encode("test"), role = Role.PATIENT))

        val loginDto = LoginDto(username = "test", password = "test1")

        val ex = assertThrows<ResponseStatusException> { authService.login(loginDto) }

        assertEquals("Username or Email do not match.", ex.reason)
    }

    @Test
    @DisplayName("Should issue JWT token with user details")
    fun issueJwt() {
        val user = userRepository.save(User(username = "test", password = passwordEncoder.encode("test"), role = Role.PATIENT))
        val jwt = authService.issueJwt(user)

        assertNotNull(jwt)
        assertEquals(user.id.toString(), jwtTokenUtil.getUserId(jwt))
        assertEquals(user.role.name, jwtTokenUtil.getRole(jwt))
    }
}
