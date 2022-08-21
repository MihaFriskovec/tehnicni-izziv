package com.medifit.ratings.surveys.dto

import java.time.LocalDateTime
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

data class SubmitSurveyDto(
    @field:NotBlank val doctorId: String? = null,
    @field:NotBlank val patientId: String? = null,
    @field:NotBlank val caseId: String? = null,
    val caseDateTime: LocalDateTime? = null,
    @field:Min(1) @field:Max(5) val rating: Int? = null
)
