package com.medifit.auth.user.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @NotEmpty
    @Email
    @Column(nullable = true, unique = true)
    var email: String? = null,

    @NotEmpty
    @Column(nullable = false, unique = true, updatable = false, length = 32)
    val username: String,

    @NotEmpty
    @Column(nullable = false, length = 128)
    var password: String,

    @Column(nullable = false)
    var active: Boolean = true,

    @NotEmpty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var role: Role,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdDate: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false, updatable = true)
    var modifiedDate: LocalDateTime = LocalDateTime.now()
)

