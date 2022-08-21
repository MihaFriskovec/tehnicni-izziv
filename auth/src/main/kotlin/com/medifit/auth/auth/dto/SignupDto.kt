package com.medifit.auth.auth.dto

import javax.validation.constraints.NotEmpty

data class SignupDto(
    @field:NotEmpty val username: String,
    @field:NotEmpty val password: String,
    val email: String? = null,
    val role: String? = null
)
