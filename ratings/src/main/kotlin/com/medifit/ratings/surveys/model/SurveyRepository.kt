package com.medifit.ratings.surveys.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SurveyRepository : CrudRepository<Survey, Long> {
    fun findByAppointment(case: Long): Survey?
    fun findByAppointmentAndPatient(case: Long, patient: Long): Survey?
    fun findByProcessedFalseAndRatingNotNull(): List<Survey>
}
