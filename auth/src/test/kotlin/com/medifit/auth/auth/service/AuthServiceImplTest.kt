package com.medifit.auth.auth.service

import com.medifit.auth.auth.dto.LoginDto
import com.medifit.auth.auth.dto.SignupDto
import com.medifit.auth.config.JwtTokenUtil
import com.medifit.auth.user.model.Role
import com.medifit.auth.user.model.User
import com.medifit.auth.user.model.UserRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class AuthServiceImplTest {

    private val userRepository: UserRepository = mockk()
    private val jwtTokenUtil: JwtTokenUtil = mockk()

    private val meterRegistry = SimpleMeterRegistry()

    private val passwordEncoder = NoOpPasswordEncoder.getInstance()

    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        authService = AuthServiceImpl(userRepository, passwordEncoder, jwtTokenUtil, meterRegistry)
    }

    @Test
    @DisplayName("Should create new user with default role")
    fun shouldCreateNewUser_defaultRole() {
        val signupDto = SignupDto(
            username = "test",
            password = "test"
        )

        val expectedUser = User(
            username = "test",
            password = passwordEncoder.encode("test"),
            active = true,
            role = Role.PATIENT
        )

        every { userRepository.findByUsername("test") } returns null
        every { userRepository.save(any()) } returnsArgument 0

        val user = authService.signup(signupDto)

        verify(exactly = 1) { userRepository.findByUsername("test") }

        assertNotNull(user)
        assertEquals(user.role, expectedUser.role)
        assertEquals(user.password, expectedUser.password)
    }

    @Test
    @DisplayName("Should not create user - username already in use")
    fun shouldThrow_usernameAlreadyInUse() {
        val signupDto = SignupDto(
            username = "test",
            password = "test"
        )

        every { userRepository.findByUsername("test") } returns User(
            username = "test",
            password = "test",
            role = Role.PATIENT
        )

        assertThrows<ResponseStatusException> { authService.signup(signupDto) }
        verify(exactly = 1) { userRepository.findByUsername("test") }
    }

    @Test
    @DisplayName("Should log user in")
    fun shouldLogUserIn() {
        val loginDto = LoginDto(
            username = "test",
            password = "test"
        )

        val existingUser = User(
            username = "test",
            password = passwordEncoder.encode("test"),
            role = Role.PATIENT
        )

        every { userRepository.findByUsername("test") } returns existingUser

        val user = authService.login(loginDto)

        assertNotNull(user)
    }

    @Test
    @DisplayName("Should throw error - user with username does not exist")
    fun showThrowError_usernameInvalid() {
        val loginDto = LoginDto(
            username = "test1",
            password = "test"
        )

        every { userRepository.findByUsername("test1") } returns null

        assertThrows<ResponseStatusException> { authService.login(loginDto) }
    }

    @Test
    @DisplayName("Should throw error - invalid password")
    fun shouldThrow_invalidPassword() {
        val loginDto = LoginDto(
            username = "test",
            password = "test1"
        )

        val existingUser = User(
            username = "test",
            password = passwordEncoder.encode("test"),
            role = Role.PATIENT
        )

        every { userRepository.findByUsername("test") } returns existingUser

        assertThrows<ResponseStatusException> { authService.login(loginDto) }
    }

    @Test
    @DisplayName("Should issue JWT")
    fun shouldIssueJwtToken() {
        val user = User(
            id = 1,
            username = "test",
            password = "test",
            role = Role.PATIENT
        )

        every { jwtTokenUtil.createToken("1", "PATIENT") } returns "TOKEN"

        authService.issueJwt(user)

        verify(exactly = 1) { jwtTokenUtil.createToken("1", "PATIENT") }
    }
}
