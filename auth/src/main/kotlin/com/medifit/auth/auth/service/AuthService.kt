package com.medifit.auth.auth.service

import com.medifit.auth.auth.dto.SignupDto
import com.medifit.auth.auth.dto.LoginDto
import com.medifit.auth.user.model.User

interface AuthService {
    fun signup(signupDto: SignupDto): User
    fun login(loginDto: LoginDto): User
    fun issueJwt(user: User): String
}
