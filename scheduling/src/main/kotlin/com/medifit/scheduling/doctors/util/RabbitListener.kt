package com.medifit.scheduling.doctors.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.medifit.scheduling.doctors.service.DoctorService
import com.medifit.sharedlib.dto.RatingMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class RabbitListener(private val doctorService: DoctorService) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    @RabbitListener(queues = ["\${medifit-queue.ratings}"], returnExceptions = "false")

    fun listen(message: String) {
        val ratingMessage = objectMapper.readValue(message, RatingMessage::class.java)

        logger.info("Received RatingMessage, message=$ratingMessage")

        try {
            doctorService.updateRating(ratingMessage.doctor!!, ratingMessage.rating!!)
        } catch (e: Exception) {
            return
        }
    }
}
