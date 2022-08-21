package com.medifit.ratings.surveys.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

data class SubmitSurveyDto(
    @field:NotBlank val doctorId: String? = null,
    @field:NotBlank val patientId: String? = null,
    @field:NotBlank val caseId: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @field:NotBlank val caseDateTime: LocalDateTime? = null,
    @field:NotBlank @field:Min(1) @field:Max(5) val rating: Int? = null
)
