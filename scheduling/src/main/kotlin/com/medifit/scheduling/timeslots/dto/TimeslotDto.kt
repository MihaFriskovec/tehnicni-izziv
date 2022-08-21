package com.medifit.scheduling.timeslots.dto

import java.time.LocalDateTime

data class TimeslotDto(
    val id: String,
    val doctor: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val free: Boolean
)
