package com.medifit.sharedlib.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class AppointmentRabbitMessage(
    val appointmentId: Long? = null,
    val patient: Long? = null,
    val doctor: Long? = null,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
    val action: Action? = null
)

enum class Action {
    CREATE, UPDATE, DELETE
}
