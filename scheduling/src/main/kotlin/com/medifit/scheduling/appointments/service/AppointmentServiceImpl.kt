package com.medifit.scheduling.appointments.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.medifit.scheduling.appointments.dto.CreateAppointmentDto
import com.medifit.scheduling.appointments.model.Appointment
import com.medifit.scheduling.appointments.model.AppointmentRepository
import com.medifit.scheduling.timeslots.model.TimeslotRepository
import com.medifit.sharedlib.IdTransformer
import com.medifit.sharedlib.dto.Action
import com.medifit.sharedlib.dto.AppointmentRabbitMessage
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class AppointmentServiceImpl(
    private val appointmentRepository: AppointmentRepository,
    private val timeslotRepository: TimeslotRepository,
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${medifit-queue.appointments}") private val queueName: String,
    meterRegistry: MeterRegistry
) : AppointmentService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    private val appointmentsCreatedCounter = Counter.builder("appointments.created").register(meterRegistry)
    private val appointmentsCancelledCounter = Counter.builder("appointments.cancelled").register(meterRegistry)

    @Transactional
    override fun createAppointment(patient: Long, appointmentDto: CreateAppointmentDto): Appointment {
        if (appointmentDto.timeslot.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timeslot id.")
        }

        val appointmentId = IdTransformer.toInternal(appointmentDto.timeslot)

        val timeslot = timeslotRepository.findByIdOrNull(appointmentId)

        if (timeslot == null) {
            logger.warn("Timeslot not found, timeslot=$appointmentId")

            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Timeslot not found.")
        }

        if (!timeslot.free) {
            logger.warn("Timeslot already taken")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Timeslot is already taken.")
        }

        if (timeslot.doctor.user == patient) {
            logger.warn("Can not book you own appointments.")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You can not book your own timeslots.")
        }

        timeslot.free = false

        return try {
            timeslotRepository.save(timeslot)

            val appointmentToCreate = Appointment(
                patient = patient,
                timeslot = timeslot
            )
            val appointment = appointmentRepository.save(appointmentToCreate)

            val appointmentMessage = AppointmentRabbitMessage(
                appointmentId = appointment.id!!,
                doctor = timeslot.doctor.user,
                patient = appointment.patient,
                startTime = timeslot.startTime,
                endTime = timeslot.endTime,
                action = Action.CREATE
            )

            logger.info("Sending AppointmentMessage, queue=$queueName, message=$appointmentMessage")

            rabbitTemplate.convertAndSend(queueName, objectMapper.writeValueAsString(appointmentMessage))
            appointmentsCreatedCounter.increment()
            appointment
        } catch (e: Exception) {
            logger.error("Error saving new appointment, e=${e.message}")

            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unknown error occurred while saving Appointment."
            )
        }
    }

    @Transactional
    override fun cancelAppointment(patient: Long, id: Long) {
        val appointment = appointmentRepository.findByIdOrNull(id) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Appointment not found."
        )

        if (appointment.patient != patient) {
            logger.warn("Trying to cancel someone's else appointment.")
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment for given patient not found.")
        }

        // Do not allow to cancel appointments if startTime is within one day
        if (appointment.timeslot.startTime.isBefore(LocalDateTime.now().plusDays(1))) {
            logger.warn("Can not cancel appointment. Start day too soon.")

            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You can not cancel this appointment.")
        }

        val timeslot = appointment.timeslot
        timeslot.free = true

        return try {
            appointmentRepository.deleteById(id)
            timeslotRepository.save(timeslot)

            val appointmentMessage = AppointmentRabbitMessage(
                appointmentId = appointment.id!!,
                doctor = timeslot.doctor.user,
                patient = appointment.patient,
                startTime = timeslot.startTime,
                endTime = timeslot.endTime,
                action = Action.DELETE
            )

            logger.info("Sending AppointmentMessage, queue=$queueName, message=$appointmentMessage")

            rabbitTemplate.convertAndSend(queueName, objectMapper.writeValueAsString(appointmentMessage))

            appointmentsCancelledCounter.increment()
        } catch (e: Exception) {
            logger.error("Error deleting appointment, e=${e.message}")

            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error canceling Appointment.")
        }
    }

    override fun getAppointments(): List<Appointment> {
        return appointmentRepository.findAll().toList()
    }
}
