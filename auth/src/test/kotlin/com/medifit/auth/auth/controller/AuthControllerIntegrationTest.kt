package com.medifit.auth.auth.controller

import com.medifit.auth.auth.dto.LoginDto
import com.medifit.auth.auth.dto.SignupDto
import com.medifit.auth.config.JwtTokenUtil
import com.medifit.auth.user.model.Role
import com.medifit.auth.user.model.User
import com.medifit.auth.user.model.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
@Testcontainers
internal class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate;

    @Autowired
    private lateinit var jwtTokenUtil: JwtTokenUtil

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Container
    private val postgres =
        PostgreSQLContainer("postgres:latest").withDatabaseName("auth-test")

    @BeforeEach
    fun setUp() {
        postgres.start()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("Should create new user")
    fun shouldCreateUser() {
        val signupDto = SignupDto(username = "test", password = "test")

        val request = HttpEntity(signupDto)

        val res = restTemplate.postForEntity("/auth/signup", request, Void::class.java)

        assertEquals(HttpStatus.CREATED, res.statusCode)

        assertNotNull(userRepository.findByUsername(signupDto.username))
    }

    @Test
    @DisplayName("Should return 400 - bad request")
    fun shouldReturn400_badRequest() {
        val signupDto = SignupDto(username = "", password = "")

        val request = HttpEntity(signupDto)

        val res = restTemplate.postForEntity("/auth/signup", request, Void::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    @DisplayName("Should return 400 - username taken")
    fun shouldReturn400_usernameAlreadyExists() {
        val signupDto = SignupDto(username = "test", password = "test")

        userRepository.save(User(username = signupDto.username, password = signupDto.password, role = Role.PATIENT))

        val request = HttpEntity(signupDto)

        val res = restTemplate.postForEntity("/auth/signup", request, Void::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    @DisplayName("Should log user in and issue JWT")
    fun shouldLoginAndIssueJwt() {
        val loginDto = LoginDto(username = "test", password = "test")

        userRepository.save(
            User(
                username = loginDto.username,
                password = passwordEncoder.encode(loginDto.password),
                role = Role.PATIENT
            )
        )

        val request = HttpEntity(loginDto)

        val res = restTemplate.postForEntity("/auth/login", request, String::class.java)

        assertEquals(HttpStatus.OK, res.statusCode)
        assertNotNull(res.body)
    }
}
