package com.medifit.scheduling.appointments.controller

import com.medifit.scheduling.appointments.dto.AppointmentDto
import com.medifit.scheduling.appointments.dto.CreateAppointmentDto
import com.medifit.scheduling.appointments.model.AppointmentRepository
import com.medifit.scheduling.config.JwtTokenUtil
import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.doctors.model.DoctorRepository
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.scheduling.timeslots.model.TimeslotRepository
import com.medifit.sharedlib.IdTransformer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
@Testcontainers
internal class AppointmentControllerIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate;

    @Autowired
    private lateinit var jwtTokenUtil: JwtTokenUtil

    @Autowired
    private lateinit var timeslotRepository: TimeslotRepository

    @Autowired
    private lateinit var appointmentRepository: AppointmentRepository

    @Autowired
    private lateinit var doctorRepository: DoctorRepository

    @Container
    private val postgres =
        PostgreSQLContainer("postgres:latest").withDatabaseName("scheduling-test")

    @Container
    private val rabbitMq = RabbitMQContainer("rabbitmq:latest")

    private fun buildHeadersForUserAndRole(user: String, role: String): HttpHeaders {
        val token = jwtTokenUtil.createToken(user, role)

        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer $token")

        return headers
    }

    @BeforeEach
    fun setUp() {
        postgres.start()
        rabbitMq.start()
    }

    @AfterEach
    fun tearDown() {
        appointmentRepository.deleteAll()
        timeslotRepository.deleteAll()
        doctorRepository.deleteAll()
        postgres.close()
        rabbitMq.stop()
    }

    @Test
    fun createAppointment() {
        val doctor = doctorRepository.save(Doctor(user = 2))
        val timeslot = timeslotRepository.save(
            Timeslot(
                doctor = doctor,
                startTime = LocalDateTime.now().plusHours(1),
                endTime = LocalDateTime.now().plusHours(2)
            ),
        )

        val body = CreateAppointmentDto(timeslot = IdTransformer.toExternal(timeslot.id))
        val headers = buildHeadersForUserAndRole("1", "PATIENT")

        val request = HttpEntity(body, headers)

        val res = restTemplate.postForEntity("/appointments", request, AppointmentDto::class.java)

        assertEquals(HttpStatus.CREATED, res.statusCode)
        assertNotNull(res.body)
        assertEquals("EXTERNAL_1", res.body!!.patient)
        assertEquals(IdTransformer.toExternal(timeslot.id), res.body!!.timeslot)
    }

    @Test
    fun cancelAppointment() {
        val doctor = doctorRepository.save(Doctor(user = 2))
        val timeslot = timeslotRepository.save(
            Timeslot(
                doctor = doctor,
                startTime = LocalDateTime.now().plusDays(2),
                endTime = LocalDateTime.now().plusDays(3)
            )
        )

        val body = CreateAppointmentDto(timeslot = IdTransformer.toExternal(timeslot.id))
        val headers = buildHeadersForUserAndRole("1", "PATIENT")

        val createRequest = HttpEntity(body, headers)
        val createdAppointment = restTemplate.postForEntity("/appointments", createRequest, AppointmentDto::class.java)

        val appointmentId = createdAppointment.body!!.id

        val deleteRequest = HttpEntity(null, headers)
        val res = restTemplate.postForEntity("/appointments/$appointmentId/cancel", deleteRequest, Void::class.java)

        assertEquals(HttpStatus.NO_CONTENT, res.statusCode)
    }

}

