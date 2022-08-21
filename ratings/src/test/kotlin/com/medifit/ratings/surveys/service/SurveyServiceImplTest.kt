package com.medifit.ratings.surveys.service

import com.medifit.ratings.surveys.model.Survey
import com.medifit.ratings.surveys.model.SurveyRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

internal class SurveyServiceImplTest {
    private val surveyRepository: SurveyRepository = mockk()

    private val surveyService = SurveyServiceImpl(surveyRepository, SimpleMeterRegistry())

    @Test
    @DisplayName("Should create new survey")
    fun shouldCreateNewSurvey() {
        every { surveyRepository.findByAppointment(any()) } returns null
        every { surveyRepository.save(any()) } returnsArgument 0

        val survey = surveyService.createSurvey(
            case = 1,
            doctor = 1,
            patient = 2,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now()
        )

        verify(exactly = 1) { surveyRepository.findByAppointment(1) }
        verify(exactly = 1) { surveyRepository.save(any()) }

        assertNotNull(survey)
        assertEquals(1, survey.appointment)
        assertNull(survey.rating)
        assertFalse(survey.processed)
    }

    @Test
    @DisplayName("Should throw error when trying to create duplicate")
    fun shouldThrowError_surveyForCaseExists() {
        every { surveyRepository.findByAppointment(any()) } returns Survey(
            doctor = 1,
            appointment = 1,
            patient = 2,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now()
        )

        val ex = assertThrows<ResponseStatusException> {
            surveyService.createSurvey(
                case = 1,
                doctor = 1,
                patient = 2,
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now()
            )
        }

        verify(exactly = 1) { surveyRepository.findByAppointment(1) }
        verify(exactly = 0) { surveyRepository.save(any()) }

        assertEquals("Survey for given case already exists.", ex.reason)
    }

    @Test
    @DisplayName("Should update existing survey")
    fun shouldUpdateSurvey() {
        every { surveyRepository.findByAppointmentAndPatient(any(), any()) } returns Survey(
            doctor = 1,
            appointment = 1,
            patient = 2,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now()
        )

        every { surveyRepository.save(any()) } returnsArgument 0

        val survey = surveyService.submitSurvey(
            case = 1,
            doctor = 1,
            patient = 2,
            caseDateTime = LocalDateTime.now(),
            rating = 5
        )

        assertNotNull(survey)
        assertNotNull(survey.rating)
        assertEquals(1, survey.appointment)
        assertEquals(1, survey.doctor)
        assertEquals(2, survey.patient)
        assertEquals(5, survey.rating)
    }

    @Test
    @DisplayName("Should throw error why trying to update non existing survey")
    fun shouldThrowError_surveyNotExists() {
        every { surveyRepository.findByAppointmentAndPatient(any(), any()) } returns null

        val ex = assertThrows<ResponseStatusException> {
            surveyService.submitSurvey(
                case = 1,
                doctor = 1,
                patient = 2,
                caseDateTime = LocalDateTime.now(),
                5
            )
        }

        verify(exactly = 0) { surveyRepository.save(any()) }

        assertEquals("Survey not found.", ex.reason)
    }

    @Test
    @DisplayName("Should throw error why trying to update completed survey")
    fun shouldThrowError_surveyCompleted() {
        every { surveyRepository.findByAppointmentAndPatient(any(), any()) } returns Survey(
            doctor = 1,
            appointment = 1,
            patient = 2,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            rating = 5
        )

        val ex = assertThrows<ResponseStatusException> {
            surveyService.submitSurvey(
                case = 1,
                doctor = 1,
                patient = 2,
                caseDateTime = LocalDateTime.now(),
                rating = 5
            )
        }

        verify(exactly = 0) { surveyRepository.save(any()) }

        assertEquals("Review was already submitted for this survey.", ex.reason)
    }
}
