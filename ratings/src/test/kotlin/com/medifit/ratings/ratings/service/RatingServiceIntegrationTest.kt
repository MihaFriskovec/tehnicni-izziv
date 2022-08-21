package com.medifit.ratings.ratings.service

import com.medifit.ratings.ratings.model.RatingRepository
import com.medifit.ratings.surveys.model.Survey
import com.medifit.ratings.surveys.model.SurveyRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("integration")
@Testcontainers
internal class RatingServiceIntegrationTest {
    @Autowired
    private lateinit var surveyRepository: SurveyRepository

    @Autowired
    private lateinit var ratingRepository: RatingRepository

    @Autowired
    private lateinit var ratingService: RatingService

    @Container
    private val postgres = PostgreSQLContainer("postgres:latest").withDatabaseName("rating-test")

    private val surveysMock = listOf(
        Survey(
            doctor = 1,
            appointment = 1,
            patient = 2,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            rating = 4
        ),
        Survey(
            doctor = 1,
            appointment = 2,
            patient = 2,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            rating = 5
        ),
        Survey(
            doctor = 2,
            appointment = 3,
            patient = 3,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            rating = 3
        ),
        Survey(
            doctor = 3,
            appointment = 4,
            patient = 2,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            rating = 2
        ),
        Survey(
            doctor = 3,
            appointment = 5,
            patient = 2,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
        ),
        Survey(
            doctor = 3,
            appointment = 6,
            patient = 2,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            rating = 3,
            processed = true
        ),

        )

    @BeforeEach
    fun setup() {
        postgres.start()
    }

    @AfterEach
    fun tearDown() {
        surveyRepository.deleteAll()
        ratingRepository.deleteAll()
        postgres.stop()
    }

    @Test
    @DisplayName("Should process surveys")
    fun shouldProcess() {
        surveyRepository.saveAll(surveysMock).toList()
        ratingService.process()

        val ratings = ratingRepository.findAll().toList()
        assertEquals(3, ratings.size)

        val ratingOne = ratings[0]
        assertEquals(1, ratingOne.doctor)
        assertEquals(BigDecimal("4.50"), ratingOne.rating)
        assertEquals(2, ratingOne.totalNumberOfRatings)

        val ratingTwo = ratings[1]
        assertEquals(2, ratingTwo.doctor)
        assertEquals(BigDecimal("3.00"), ratingTwo.rating)
        assertEquals(1, ratingTwo.totalNumberOfRatings)

        val surveys = surveyRepository.findAll()
        surveys
            .filter { it.rating != null }
            .map {
                assertTrue { it.processed }
            }
    }

}
