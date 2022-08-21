package com.medifit.auth

import com.medifit.auth.user.model.Role
import com.medifit.auth.user.model.User
import com.medifit.auth.user.model.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@Component
class AuthDataLoader(
    private val entityManager: EntityManager,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun run(vararg args: String?) {
        val executed = entityManager.createNativeQuery("SELECT executed FROM demodatasettings").singleResult.toString().toBoolean()

        logger.info("Demo data loader status: enabled=${!executed}")

        if (!executed) {
            logger.warn("Deleting all old data")
            userRepository.deleteAll()
            logger.info("Starting to load demo data...")

            val users = listOf(
                User(
                    username = "admin",
                    password = passwordEncoder.encode("test"),
                    role = Role.ADMIN,
                    email = "admin@email.com"
                ),
                User(
                    username = "patient1",
                    password = passwordEncoder.encode("test"),
                    role = Role.PATIENT,
                    email = "patient1@email.com"
                ),
                User(
                    username = "patient2",
                    password = passwordEncoder.encode("test"),
                    role = Role.PATIENT,
                    email = "patient2@email.com"
                ),
                User(
                    username = "doctor1",
                    password = passwordEncoder.encode("test"),
                    role = Role.DOCTOR,
                    email = "doctor1@email.com"
                ),
                User(
                    username = "doctor2",
                    password = passwordEncoder.encode("test"),
                    role = Role.DOCTOR,
                    email = "doctor22@email.com"
                ),
            )

            logger.info("Saving users")
            userRepository.saveAll(users)
            logger.info("Demo data successfully imported.")
            entityManager.createNativeQuery("UPDATE demodatasettings SET executed = true WHERE 1 = 1").executeUpdate()
        }
    }
}
