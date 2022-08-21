package com.medifit.ratings.ratings.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.medifit.ratings.ratings.model.Rating
import com.medifit.ratings.ratings.model.RatingRepository
import com.medifit.ratings.surveys.model.SurveyRepository
import com.medifit.sharedlib.dto.RatingMessage
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class RatingServiceImpl(
    private val surveyRepository: SurveyRepository,
    private val ratingRepository: RatingRepository,
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${medifit-queue.ratings}") private val queueName: String,
    meterRegistry: MeterRegistry
) : RatingService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val objectMapper = ObjectMapper()

    private val processTimer = Timer.builder("surveys.processTime").register(meterRegistry)
    private val processedCounter = Counter.builder("surveys.processed").register(meterRegistry)

    init {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            process()
        }, 1, 60, TimeUnit.MINUTES)
    }

    @Transactional
    override fun process() {
        processTimer.record {
            // Get all unprocessed surveys with rating
            val surveys = surveyRepository.findByProcessedFalseAndRatingNotNull()

            logger.info("Found ${surveys.size} surveys to process")

            // Group surveys by doctor
            val surveysByDoctor = surveys.groupBy { it.doctor }

            surveysByDoctor.map {
                val sum = it.value.sumOf { survey -> survey.rating!! }.toDouble()
                val newRatingsCount = it.value.size
                val newRating = (sum / newRatingsCount).toBigDecimal().setScale(2)

                val rating = ratingRepository.getRatingByDoctor(doctor = it.key)

                if (rating == null) {
                    logger.info("Rating for Doctor not found, doctor=${it.key}")
                    val ratingToSave = Rating(
                        doctor = it.key,
                        rating = newRating,
                        totalNumberOfRatings = newRatingsCount
                    )

                    logger.info("New rating calculated, doctor=${it.key}, rating=$newRating, numberOfRatings=$newRatingsCount")

                    val savedRating = ratingRepository.save(ratingToSave)

                    val ratingMessage = RatingMessage(
                        doctor = savedRating.doctor,
                        rating = savedRating.rating!!
                    )

                    logger.info("Sending new RatingMessage, message=$ratingMessage")

                    rabbitTemplate.convertAndSend(queueName, objectMapper.writeValueAsString(ratingMessage))
                } else {
                    logger.info("Rating for Doctor found, doctor=${it.key}, rating=$rating")

                    val oldRating = rating.rating!!
                    val numberOfRatings = rating.totalNumberOfRatings!!

                    val oldRatingNormalized = oldRating.multiply(numberOfRatings.toBigDecimal()).setScale(2)
                    val newRatingNormalized = newRating.multiply(newRatingsCount.toBigDecimal()).setScale(2)

                    val updatedRating =
                        (oldRatingNormalized + newRatingNormalized) / (numberOfRatings + newRatingsCount).toBigDecimal()
                            .setScale(2)
                    val updatedNumberOfRatings = numberOfRatings + newRatingsCount

                    logger.info("New rating calculated, doctor=${it.key}, rating=$updatedRating, numberOfRatings=$updatedNumberOfRatings")

                    rating.rating = updatedRating
                    rating.totalNumberOfRatings = updatedNumberOfRatings

                    val savedRating = ratingRepository.save(rating)

                    val ratingMessage = RatingMessage(
                        doctor = savedRating.doctor,
                        rating = savedRating.rating!!
                    )

                    logger.info("Sending new RatingMessage, message=$ratingMessage")

                    rabbitTemplate.convertAndSend(queueName, objectMapper.writeValueAsString(ratingMessage))
                }

                processedCounter.increment()
            }

            surveys.map { it.processed = true }

            surveyRepository.saveAll(surveys)
        }
    }
}
