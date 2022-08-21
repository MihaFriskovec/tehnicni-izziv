package com.medifit.scheduling.appointments.service

import com.medifit.scheduling.appointments.dto.CreateAppointmentDto
import com.medifit.scheduling.appointments.model.Appointment
import com.medifit.scheduling.appointments.model.AppointmentRepository
import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.scheduling.timeslots.model.TimeslotRepository
import com.medifit.sharedlib.IdTransformer
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import kotlin.test.assertEquals

internal class AppointmentServiceImplTest {
    private val appointmentRepository: AppointmentRepository = mockk()
    private val timeslotRepository: TimeslotRepository = mockk()
    private val rabbitTemplate: RabbitTemplate = mockk()

    private val meterRegistry = SimpleMeterRegistry()

    private lateinit var appointmentService: AppointmentService

    @BeforeEach
    fun setup() {
        every { rabbitTemplate.convertAndSend(any(), any<Any>()) } returnsArgument 0
        appointmentService =
            AppointmentServiceImpl(appointmentRepository, timeslotRepository, rabbitTemplate, "test", meterRegistry)
    }

    @AfterEach
    fun tearDown() {

    }

    @Test
    @DisplayName("Should create new appointment for given timeslot")
    fun shouldCreateAppointment() {
        val createAppointmentDto = CreateAppointmentDto(timeslot = IdTransformer.toExternal(1))

        val timeslot = Timeslot(
            id = 1,
            doctor = Doctor(user = 1),
            startTime = LocalDateTime.now().plusHours(1),
            endTime = LocalDateTime.now().plusHours(2),
            free = true
        )

        val expectedAppointment = Appointment(
            patient = 2,
            timeslot = timeslot
        )

        every { timeslotRepository.findByIdOrNull(1) } returns timeslot
        every { timeslotRepository.save(any()) } returns timeslot
        every { appointmentRepository.save(any()) } returns expectedAppointment

        appointmentService.createAppointment(2, createAppointmentDto)

        verify(exactly = 1) { timeslotRepository.findById(1) }
        verify(exactly = 1) { appointmentRepository.save(any()) }
    }

    @Test
    @DisplayName("Should throw error - timeslot does not exists")
    fun shouldThrow_invalidTimeslot() {
        val createAppointmentDto = CreateAppointmentDto(timeslot = IdTransformer.toExternal(1))

        every { timeslotRepository.findByIdOrNull(any()) } returns null

        val ex = assertThrows<ResponseStatusException> { appointmentService.createAppointment(2, createAppointmentDto) }
        assertEquals("Timeslot not found.", ex.reason)
    }

    @Test
    @DisplayName("Should throw error - timeslot not free")
    fun shouldThrow_timeslotNotFree() {
        val createAppointmentDto = CreateAppointmentDto(timeslot = IdTransformer.toExternal(1))

        val timeslot = Timeslot(
            id = 1,
            doctor = Doctor(user = 1),
            startTime = LocalDateTime.now().plusHours(1),
            endTime = LocalDateTime.now().plusHours(2),
            free = false
        )

        every { timeslotRepository.findByIdOrNull(any()) } returns timeslot

        val ex = assertThrows<ResponseStatusException> { appointmentService.createAppointment(2, createAppointmentDto) }
        assertEquals("Timeslot is already taken.", ex.reason)
    }

    @Test
    @DisplayName("Should throw error - can not book you own timeslots")
    fun shouldThrow_canNotBookYourOwnTimeslot() {
        val createAppointmentDto = CreateAppointmentDto(timeslot = IdTransformer.toExternal(1))

        val timeslot = Timeslot(
            id = 1,
            doctor = Doctor(user = 1),
            startTime = LocalDateTime.now().plusHours(1),
            endTime = LocalDateTime.now().plusHours(2),
            free = true
        )

        every { timeslotRepository.findByIdOrNull(any()) } returns timeslot

        val ex = assertThrows<ResponseStatusException> { appointmentService.createAppointment(1, createAppointmentDto) }
        assertEquals("You can not book your own timeslots.", ex.reason)
    }

    @Test
    @DisplayName("Should cancel appointment")
    fun shouldCancelAppointment() {
        val appointment = Appointment(
            id = 1,
            patient = 1,
            timeslot = Timeslot(
                id = 1,
                doctor = Doctor(user = 2),
                startTime = LocalDateTime.now().plusDays(1).plusMinutes(1),
                endTime = LocalDateTime.now().plusDays(2)
            )
        )

        every { appointmentRepository.findByIdOrNull(any()) } returns appointment
        every { appointmentRepository.deleteById(1) } returnsArgument 0
        every { timeslotRepository.save(any()) } returnsArgument 0

        appointmentService.cancelAppointment(1, 1)

        verify(exactly = 1) { appointmentRepository.deleteById(1) }
        verify(exactly = 1) { timeslotRepository.save(any()) }
    }

    @Test
    @DisplayName("Should throw - invalid appointment")
    fun shouldThrow_invalidAppointment() {
        every { appointmentRepository.findByIdOrNull(any()) } returns null

        val ex = assertThrows<ResponseStatusException> { appointmentService.cancelAppointment(1, 1) }
        assertEquals("Appointment not found.", ex.reason)
    }

    @Test
    @DisplayName("Should throw - startTime too soon")
    fun shouldThrow_startTimeWithinOneDay() {
        val appointment = Appointment(
            id = 1,
            patient = 1,
            timeslot = Timeslot(
                id = 1,
                doctor = Doctor(user = 2),
                startTime = LocalDateTime.now().plusDays(1).minusMinutes(1),
                endTime = LocalDateTime.now().plusDays(2)
            )
        )

        every { appointmentRepository.findByIdOrNull(any()) } returns appointment

        val ex = assertThrows<ResponseStatusException> { appointmentService.cancelAppointment(1, 1) }
        assertEquals("You can not cancel this appointment.", ex.reason)
    }
}
