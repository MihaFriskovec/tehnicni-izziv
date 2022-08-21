package com.medifit.scheduling.config

import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig(
    @Value("\${medifit-queue.appointments}") private val appointmentQueueName: String,
    @Value("\${medifit-queue.ratings}") private val ratingsQueueName: String
) {

    @Bean
    fun appointmentsQueue(): Queue {
        return Queue(appointmentQueueName)
    }

    @Bean
    fun ratingsQueue(): Queue {
        return Queue(ratingsQueueName)
    }
}
