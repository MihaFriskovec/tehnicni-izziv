package com.medifit.auth.auth.service

import com.medifit.auth.auth.dto.SignupDto
import com.medifit.auth.auth.dto.LoginDto
import com.medifit.auth.config.JwtTokenUtil
import com.medifit.auth.user.model.Role
import com.medifit.auth.user.model.User
import com.medifit.auth.user.model.UserRepository
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenUtil: JwtTokenUtil,
    meterRegistry: MeterRegistry
) : AuthService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val usersCounter = Counter.builder("users.created").register(meterRegistry)

    override fun signup(signupDto: SignupDto): User {
        if (userRepository.findByUsername(signupDto.username) != null) {
            logger.warn("Username already in use. username=${signupDto.username}")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User with given username already exists.")
        }

        val userToCreate = User(
            username = signupDto.username,
            password = passwordEncoder.encode(signupDto.password),
            email = signupDto.email,
            active = true,
            role = Role.valueOf(signupDto.role ?: Role.PATIENT.name)
        )

        logger.debug(userToCreate.toString())

        return try {
            val user = userRepository.save(userToCreate)
            usersCounter.increment()
            user
        } catch (e: Exception) {
            logger.error("Error creating new user, e=${e.message}")
            logger.debug(e.toString())

            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error occurred while saving User.")
        }
    }

    override fun login(loginDto: LoginDto): User {
        val user = userRepository.findByUsername(loginDto.username)

        if (user == null) {
            logger.warn("User with given username does not exists. username=${loginDto.username}")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User with given username does not exists.")
        }

        if (!passwordEncoder.matches(loginDto.password, user.password)) {
            logger.warn("Passwords do not match for given username. username=${loginDto.username}")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or Email do not match.")
        }

        return user
    }

    override fun issueJwt(user: User): String {
        return jwtTokenUtil.createToken(user.id.toString(), user.role.name)
    }
}
