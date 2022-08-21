package com.medifit.scheduling.timeslots.controller

import com.medifit.scheduling.config.JwtTokenUtil
import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.doctors.model.DoctorRepository
import com.medifit.scheduling.timeslots.dto.CreateTimeslotDto
import com.medifit.scheduling.timeslots.dto.TimeslotBulkUploadDto
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.scheduling.timeslots.model.TimeslotRepository
import com.medifit.sharedlib.IdTransformer
import com.medifit.sharedlib.dto.ListResponseDto
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
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
internal class TimeslotControllerIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate;

    @Autowired
    private lateinit var jwtTokenUtil: JwtTokenUtil

    @Autowired
    private lateinit var doctorRepository: DoctorRepository

    @Autowired
    private lateinit var timeslotRepository: TimeslotRepository

    @Container
    private val postgres =
        PostgreSQLContainer("postgres:latest").withDatabaseName("scheduling-test")

    private fun buildHeadersForUserAndRole(user: String, role: String): HttpHeaders {
        val token = jwtTokenUtil.createToken(user, role)

        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer $token")

        return headers
    }

    private val timeslots = listOf(
        CreateTimeslotDto(
            startTime = LocalDateTime.now().plusHours(1),
            endTime = LocalDateTime.now().plusHours(2)
        )
    )

    @BeforeEach
    fun setUp() {
        postgres.start()
    }

    @AfterEach
    fun tearDown() {
        doctorRepository.deleteAll()
        postgres.close()
    }

    @Test
    @DisplayName("Should create timeslots")
    fun shouldCreateTimeslots() {
        val headers = buildHeadersForUserAndRole("1", "DOCTOR")

        val request = HttpEntity(timeslots, headers)

        doctorRepository.save(Doctor(user = 1))

        val res = restTemplate.postForEntity("/timeslots", request, TimeslotBulkUploadDto::class.java)

        assertEquals(HttpStatus.CREATED, res.statusCode)
        assertNotNull(res.body)
        assertEquals(1, res.body!!.numberOfCreatedSlots)
    }

    @Test
    @DisplayName("Should return 403 when user is not DOCTOR")
    fun shouldReturn403() {
        val headers = buildHeadersForUserAndRole("1", "PATIENT")

        val request = HttpEntity(timeslots, headers)
        val res = restTemplate.postForEntity("/timeslots", request, TimeslotBulkUploadDto::class.java)

        assertEquals(HttpStatus.FORBIDDEN, res.statusCode)
    }

    @Test
    @DisplayName("Should list all timeslots for doctor")
    fun shouldListTimeslotsForDoctor() {
        val headers = buildHeadersForUserAndRole("1", "DOCTOR")

        val request = HttpEntity(timeslots, headers)

        val doctor = doctorRepository.save(Doctor(user = 1))

        restTemplate.postForEntity("/timeslots", request, TimeslotBulkUploadDto::class.java)

        val res = restTemplate.exchange(
            "/timeslots/doctor/${IdTransformer.toExternal(doctor.id)}",
            HttpMethod.GET,
            request,
            ListResponseDto::class.java
        )

        assertEquals(HttpStatus.OK, res.statusCode)
        assertNotNull(res.body)
        assertNotNull(res.body!!.data)
        assertEquals(1, res.body!!.data.size)
    }

    @Test
    @DisplayName("Should return empty array when no timeslots for doctor")
    fun shouldReturnEmptyListWhenNoTimeslots() {
        val headers = buildHeadersForUserAndRole("1", "DOCTOR")

        val request = HttpEntity(timeslots, headers)

        val doctors = doctorRepository.saveAll(listOf(Doctor(user = 1), Doctor(user = 2))).toList()

        restTemplate.postForEntity("/timeslots", request, TimeslotBulkUploadDto::class.java)

        val res = restTemplate.exchange(
            "/timeslots/doctor/${IdTransformer.toExternal(doctors[1].id)}",
            HttpMethod.GET,
            request,
            ListResponseDto::class.java
        )

        assertEquals(HttpStatus.OK, res.statusCode)
        assertNotNull(res.body)
        assertNotNull(res.body!!.data)
        assertEquals(0, res.body!!.data.size)
    }

    @Test
    @DisplayName("Should list free Timeslots for given filters")
    fun shouldReturnFreeTimeslots() {
        val doctors = doctorRepository.saveAll(listOf(Doctor(user = 1), Doctor(user = 2))).toList()
        val timeslots = timeslotRepository.saveAll(
            listOf(
                Timeslot(
                    doctor = doctors[0],
                    startTime = LocalDateTime.now().plusHours(1),
                    endTime = LocalDateTime.now().plusHours(2)
                ), Timeslot(
                    doctor = doctors[1],
                    startTime = LocalDateTime.now().plusHours(1),
                    endTime = LocalDateTime.now().plusHours(2)
                )
            )
        )

        val headers = buildHeadersForUserAndRole("1", "DOCTOR")
        val request = HttpEntity(null, headers)

        val res = restTemplate.exchange(
            "/timeslots?doctor=${IdTransformer.toExternal(doctors[0].id)}",
            HttpMethod.GET,
            request,
            ListResponseDto::class.java
        )

        assertNotNull(res)
        assertEquals(HttpStatus.OK, res.statusCode)
        assertNotNull(res.body)

        val body = res.body!!

        assertNotNull(body.data)
        assertEquals(1, body.data.size)
    }
}
