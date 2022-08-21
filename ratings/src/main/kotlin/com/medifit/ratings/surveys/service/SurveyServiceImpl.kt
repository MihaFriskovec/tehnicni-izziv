package com.medifit.ratings.surveys.service

import com.medifit.ratings.surveys.model.Survey
import com.medifit.ratings.surveys.model.SurveyRepository
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class SurveyServiceImpl(private val surveyRepository: SurveyRepository, meterRegistry: MeterRegistry) : SurveyService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val createdSurveys = Counter.builder("surveys.created").register(meterRegistry)
    private val submittedSurveys = Counter.builder("surveys.filled").register(meterRegistry)

    override fun createSurvey(
        case: Long,
        doctor: Long,
        patient: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Survey {
        val existingSurvey = surveyRepository.findByAppointment(case)

        if (existingSurvey != null) {
            logger.error("Survey for Appointment already exists, appointment=$case")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey for given case already exists.")
        }

        val survey = Survey(
            appointment = case,
            doctor = doctor,
            patient = patient,
            startTime = startTime,
            endTime = endTime
        )

        return try {
            val createdSurvey = surveyRepository.save(survey)
            createdSurveys.increment()

            createdSurvey
        } catch (e: Exception) {
            logger.error("Error saving survey, e=${e.message}")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while saving survey.")
        }
    }

    override fun getSurveyByCaseAndPatient(caseId: Long, patient: Long): Survey {
        val survey = surveyRepository.findByAppointmentAndPatient(caseId, patient)

        if (survey == null) {
            logger.warn("Survey not found, caseId=$caseId")
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found.")
        }

        return survey
    }

    override fun submitSurvey(
        case: Long,
        doctor: Long,
        patient: Long,
        caseDateTime: LocalDateTime,
        rating: Int
    ): Survey {
        val survey = getSurveyByCaseAndPatient(case, patient)

        if (survey.doctor != doctor) {
            logger.warn("Error submitting survey, doctorId miss-match, case=$case")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Error submitting survey. Please check you data.")
        }

        if (survey.rating != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Review was already submitted for this survey.")
        }

        survey.rating = rating

        return try {
            val updatedSurvey = surveyRepository.save(survey)
            submittedSurveys

            updatedSurvey
        } catch (e: Exception) {
            logger.error("Error saving survey, e=${e.message}")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while saving survey.")
        }
    }

    override fun deleteSurvey(case: Long): Survey {
        TODO("Not yet implemented")
    }

    override fun getSurveys(): List<Survey> {
        return surveyRepository.findAll().toList()
    }
}
