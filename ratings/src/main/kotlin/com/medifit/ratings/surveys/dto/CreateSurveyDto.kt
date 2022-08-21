package com.medifit.ratings.surveys.dto

import java.time.LocalDateTime

data class CreateSurveyDto(
    val case: String,
    val doctor: String,
    val patient: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
