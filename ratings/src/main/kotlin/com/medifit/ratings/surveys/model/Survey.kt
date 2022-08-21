package com.medifit.ratings.surveys.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "surveys")
class Survey(
    @Column(name = "doctor_id", nullable = false)
    val doctor: Long,

    @Column(name = "appointment_id", nullable = false, unique = true, updatable = false)
    val appointment: Long,

    @Column(name = "user_id", nullable = false)
    val patient: Long,

    @Column(nullable = false)
    val startTime: LocalDateTime,

    @Column(nullable = false)
    val endTime: LocalDateTime,

    @Column(nullable = false)
    var processed: Boolean = false,

    @Column(nullable = true)
    var rating: Int? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)
