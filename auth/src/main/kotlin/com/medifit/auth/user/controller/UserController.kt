package com.medifit.auth.user.controller

import com.medifit.auth.auth.service.AuthService
import com.medifit.auth.user.dto.UserDto
import com.medifit.auth.user.model.User
import com.medifit.auth.user.service.UserService
import com.medifit.sharedlib.IdTransformer
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "jwt")
class UserController(private val userService: UserService) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/me")
    fun me(principal: Principal): ResponseEntity<UserDto> {
        val user = userService.getUserById(principal.name.toLong())
        return if (user != null) {
            ResponseEntity.ok(user.toDto())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    fun User.toDto() = UserDto(
        id = IdTransformer.toExternal(id),
        username = username,
        email = email,
        role = role.name
    )
}
