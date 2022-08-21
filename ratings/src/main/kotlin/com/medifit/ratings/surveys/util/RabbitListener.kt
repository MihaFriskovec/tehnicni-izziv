package com.medifit.ratings.surveys.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.medifit.ratings.surveys.service.SurveyService
import com.medifit.sharedlib.dto.Action
import com.medifit.sharedlib.dto.AppointmentRabbitMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class RabbitListener(private val surveyService: SurveyService) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule()).registerKotlinModule()

    @RabbitListener(queues = ["\${medifit-queue.appointments}"])
    fun listen(message: String) {
        val appointmentMessage = objectMapper.readValue(message, AppointmentRabbitMessage::class.java)

        logger.info("Received AppointmentMessage, message=$appointmentMessage")

        if (appointmentMessage.action == Action.CREATE) {
            surveyService.createSurvey(
                case = appointmentMessage.appointmentId!!,
                doctor = appointmentMessage.doctor!!,
                patient = appointmentMessage.patient!!,
                startTime = appointmentMessage.startTime!!,
                endTime = appointmentMessage.startTime!!
            )
        } else if (appointmentMessage.action == Action.DELETE) {
            surveyService.deleteSurvey(appointmentMessage.appointmentId!!)
        }
    }
}
