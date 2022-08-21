package com.medifit.scheduling.timeslots.dto

import java.time.LocalDateTime
import javax.validation.constraints.FutureOrPresent
import javax.validation.constraints.NotEmpty

data class CreateTimeslotDto(
    @field:NotEmpty @field:FutureOrPresent val startTime: LocalDateTime? = null,
    @field:NotEmpty @field:FutureOrPresent val endTime: LocalDateTime? = null
)
