package com.medifit.auth.auth.dto

import javax.validation.constraints.NotEmpty

data class LoginDto(
    @field:NotEmpty val username: String,
    @field:NotEmpty val password: String
)
