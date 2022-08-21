package com.medifit.scheduling.doctors.dto

import java.math.BigDecimal

data class DoctorDto(
    val id: String? = null,
    val user: String? = null,
    val rating: BigDecimal? = null,
    val specialties: List<SpecialtyDto>? = null,
)

data class SpecialtyDto(
    val id: String? = null,
    val name: String? = null
)
