package com.medifit.ratings.surveys.service

import com.medifit.ratings.surveys.dto.CreateSurveyDto
import com.medifit.ratings.surveys.model.Survey
import java.time.LocalDateTime

interface SurveyService {
    fun createSurvey(case: Long, doctor: Long, patient: Long, startTime: LocalDateTime, endTime: LocalDateTime): Survey
    fun getSurveyByCaseAndPatient(caseId: Long, patient: Long): Survey
    fun submitSurvey(case: Long, doctor: Long, patient: Long, caseDateTime: LocalDateTime, rating: Int): Survey
    fun deleteSurvey(case: Long): Survey
    fun getSurveys(): List<Survey>
}
