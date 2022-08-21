package com.medifit.scheduling.doctors.dto

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class CreateDoctorDto(
    @field:NotNull @field:NotEmpty val name: String,
    val specialties: List<Long>
)
