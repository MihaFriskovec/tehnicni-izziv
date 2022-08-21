package com.medifit.scheduling.appointments.dto

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class CreateAppointmentDto(
    @field:NotEmpty val timeslot: String? = null
)
