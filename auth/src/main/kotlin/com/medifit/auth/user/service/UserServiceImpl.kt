package com.medifit.auth.user.service

import com.medifit.auth.user.model.User
import com.medifit.auth.user.model.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override fun getUserById(id: Long): User? {
        return userRepository.findByIdOrNull(id)
    }
}
