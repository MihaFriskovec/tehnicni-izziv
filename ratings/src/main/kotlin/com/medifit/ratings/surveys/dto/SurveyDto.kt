package com.medifit.ratings.surveys.dto

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class SurveyDto(
    val id: String? = null,
    val case: String? = null,
    val doctor: String? = null,
    val patient: String? = null,
    val caseDateTime: LocalDateTime? = null,
    val rating: String? = null,
)
