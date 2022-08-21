package com.medifit.scheduling.appointments.service

import com.medifit.scheduling.appointments.dto.CreateAppointmentDto
import com.medifit.scheduling.appointments.model.Appointment
import com.medifit.scheduling.appointments.model.AppointmentRepository
import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.doctors.model.DoctorRepository
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.scheduling.timeslots.model.TimeslotRepository
import com.medifit.sharedlib.IdTransformer
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.TestPropertySources
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@DataJpaTest
internal class AppointmentServiceIntegrationTest {

    @Autowired
    private lateinit var appointmentRepository: AppointmentRepository

    @Autowired
    private lateinit var timeslotRepository: TimeslotRepository

    @Autowired
    private lateinit var doctorRepository: DoctorRepository

    private val rabbitTemplate: RabbitTemplate = mockk()

    private lateinit var appointmentService: AppointmentService

    @BeforeEach
    fun setup() {
        every { rabbitTemplate.convertAndSend(any(), any<Any>()) } returnsArgument 0
        appointmentService =
            AppointmentServiceImpl(appointmentRepository, timeslotRepository, rabbitTemplate, "test", SimpleMeterRegistry())
    }

    @AfterEach
    fun teardown() {
        timeslotRepository.deleteAll()
        appointmentRepository.deleteAll()
    }

    @Test
    @DisplayName("Should create appointment")
    fun shouldCreateAppointment() {
        val doctor = doctorRepository.save(Doctor(user = 2))
        val timeslot = Timeslot(
            doctor = doctor,
            startTime = LocalDateTime.now().plusHours(1),
            endTime = LocalDateTime.now().plusHours(2),
            free = true
        )

        timeslotRepository.save(timeslot)

        val createAppointmentDto = CreateAppointmentDto(timeslot = IdTransformer.toExternal(timeslot.id))

        val appointment = appointmentService.createAppointment(1, createAppointmentDto)

        assertEquals(1, appointment.id)
        assertEquals(1, appointment.patient)
        assertEquals(1, appointment.timeslot.id)
        assertFalse(appointment.timeslot.free)

        assertFalse(timeslotRepository.findByIdOrNull(1)!!.free)
    }

    @Test
    @DisplayName("Should throw error - timeslot not exist")
    fun shouldThrow_timeslotNotExist() {
        val createAppointmentDto = CreateAppointmentDto(timeslot = "EXTERNAL_1")
        val ex = assertThrows<ResponseStatusException> { appointmentService.createAppointment(1, createAppointmentDto) }
        assertEquals("Timeslot not found.", ex.reason)
    }

    @Test
    @DisplayName("Should cancel existing appointment")
    fun cancelAppointment() {
        val doctor = doctorRepository.save(Doctor(user = 2))
        val timeslot = Timeslot(
            doctor = doctor,
            startTime = LocalDateTime.now().plusDays(1).plusMinutes(1),
            endTime = LocalDateTime.now().plusDays(2),
            free = false
        )

        val appointment = Appointment(
            patient = 1,
            timeslot = timeslot
        )

        appointmentRepository.save(appointment)

        appointmentService.cancelAppointment(1, appointment.id!!)

        assertNull(appointmentRepository.findByIdOrNull(appointment.id!!))
        assertTrue(timeslotRepository.findByIdOrNull(timeslot.id!!)!!.free)
    }
}
