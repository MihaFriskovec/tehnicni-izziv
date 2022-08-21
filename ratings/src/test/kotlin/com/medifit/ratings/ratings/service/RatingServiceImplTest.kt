package com.medifit.ratings.ratings.service

import com.medifit.ratings.ratings.model.Rating
import com.medifit.ratings.ratings.model.RatingRepository
import com.medifit.ratings.surveys.model.Survey
import com.medifit.ratings.surveys.model.SurveyRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.math.BigDecimal
import java.time.LocalDateTime

internal class RatingServiceImplTest {
    private val surveyRepository: SurveyRepository = mockk()
    private val ratingRepository: RatingRepository = mockk()
    private val rabbitTemplate: RabbitTemplate = mockk()

    private val surveysMock = listOf(
        Survey(
            doctor = 1,
            appointment = 1,
            patient = 2,
            processed = false,
            rating = 2,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now()
        )
    )

    private val ratingService = RatingServiceImpl(surveyRepository, ratingRepository, rabbitTemplate, "queue", SimpleMeterRegistry())

    @BeforeEach
    fun setup() {
        every { ratingRepository.save(any()) } returnsArgument 0
        every { rabbitTemplate.convertAndSend(any(), any<Any>()) } returnsArgument 0
        every { surveyRepository.saveAll<Survey>(any()) } returnsArgument 0
    }

    @Test
    @DisplayName("Should process ratings for Doctor without previous ratings")
    fun shouldProcessRatingsForNonExistingRating() {
        every { ratingRepository.getRatingByDoctor(any()) } returns null
        every { surveyRepository.findByProcessedFalseAndRatingNotNull() } returns surveysMock

        ratingService.process()
    }

    @Test
    @DisplayName("Should process ratings for Doctor with previous ratings")
    fun shouldProcessRatingsForExistingRating() {
        val rating = Rating(
            doctor = 1,
            rating = BigDecimal("3"),
            totalNumberOfRatings = 1
        )

        every { ratingRepository.getRatingByDoctor(any()) } returns rating
        every { surveyRepository.findByProcessedFalseAndRatingNotNull() } returns surveysMock

        ratingService.process()
    }
}
