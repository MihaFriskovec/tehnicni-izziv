package com.medifit.auth.user.service

import com.medifit.auth.user.model.User

interface UserService {
    fun getUserById(id: Long): User?
}
