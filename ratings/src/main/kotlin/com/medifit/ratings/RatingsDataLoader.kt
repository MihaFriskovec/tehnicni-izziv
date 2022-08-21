package com.medifit.ratings

import com.medifit.ratings.ratings.model.RatingRepository
import com.medifit.ratings.surveys.model.Survey
import com.medifit.ratings.surveys.model.SurveyRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.EntityManager

@Profile("!integration")
@Component
class RatingsDataLoader(
    private val entityManager: EntityManager,
    private val surveyRepository: SurveyRepository,
    private val ratingRepository: RatingRepository
) : CommandLineRunner {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun run(vararg args: String?) {
        val executed =
            entityManager.createNativeQuery("SELECT executed FROM demodatasettings").singleResult.toString().toBoolean()

        logger.info("Demo data loader status: enabled=${!executed}")

        if (!executed) {
            logger.warn("Deleting all old data")
            surveyRepository.deleteAll()
            ratingRepository.deleteAll()

            logger.info("Starting to load demo data...")

            val surveys = listOf(
                Survey(
                    appointment = 1,
                    doctor = 1,
                    patient = 1,
                    startTime = LocalDateTime.now().plusHours(1),
                    endTime = LocalDateTime.now().plusHours(2),
                    rating = 3
                ),
                Survey(
                    appointment = 2,
                    doctor = 2,
                    patient = 2,
                    startTime = LocalDateTime.now().plusHours(1),
                    endTime = LocalDateTime.now().plusHours(2),
                    rating = 4
                ),
                Survey(
                    appointment = 3,
                    doctor = 1,
                    patient = 2,
                    startTime = LocalDateTime.now().plusHours(1),
                    endTime = LocalDateTime.now().plusHours(2),
                    rating = 5
                ),
                Survey(
                    appointment = 4,
                    doctor = 1,
                    patient = 2,
                    startTime = LocalDateTime.now().plusHours(1),
                    endTime = LocalDateTime.now().plusHours(2)
                ),
                Survey(
                    appointment = 5,
                    doctor = 1,
                    patient = 2,
                    startTime = LocalDateTime.now().plusHours(1),
                    endTime = LocalDateTime.now().plusHours(2)
                )
            )

            entityManager.createNativeQuery("UPDATE demodatasettings SET executed = true WHERE 1 = 1").executeUpdate()
            surveyRepository.saveAll(surveys)
        }
    }
}
