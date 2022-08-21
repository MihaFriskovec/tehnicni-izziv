package com.medifit.scheduling.timeslots.service

import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.doctors.model.DoctorRepository
import com.medifit.scheduling.timeslots.dto.CreateTimeslotDto
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.scheduling.timeslots.model.TimeslotRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import kotlin.test.assertEquals

internal class TimeslotServiceImplTest {
    private val timeslotRepository: TimeslotRepository = mockk()
    private val doctorRepository: DoctorRepository = mockk()
    private val timeslotService = TimeslotServiceImpl(timeslotRepository, doctorRepository, SimpleMeterRegistry())

    private val mockDoctor = Doctor(
        id = 1,
        user = 1
    )

    @BeforeEach
    fun setup() {
        every { doctorRepository.findByUser(1) } returns mockDoctor
    }

    @Test
    fun createTimeslots() {
        val testPayload = listOf(
            CreateTimeslotDto(
                startTime = LocalDateTime.now().plusMinutes(1),
                endTime = LocalDateTime.now().plusHours(1)
            )
        )

        every { timeslotRepository.saveAll<Timeslot>(any()) } returnsArgument 0

        timeslotService.processTimeslots(1, testPayload)
    }

    @Test
    @DisplayName("Should throw error - startTime not set")
    fun shouldThrow_startTimeNull() {
        val testPayload = listOf(
            CreateTimeslotDto(
                startTime = null,
                endTime = LocalDateTime.now().plusMinutes(1)
            )
        )

        val ex = assertThrows<ResponseStatusException> { timeslotService.processTimeslots(1, testPayload) }
        assertEquals("Start and End time have to be set.", ex.reason)
    }

    @Test
    @DisplayName("Should throw error - endTime not set")
    fun shouldThrow_endTimeNull() {
        val testPayload = listOf(
            CreateTimeslotDto(
                startTime = LocalDateTime.now().plusMinutes(1),
                endTime = null
            )
        )

        val ex = assertThrows<ResponseStatusException> { timeslotService.processTimeslots(1, testPayload) }
        assertEquals("Start and End time have to be set.", ex.reason)
    }

    @Test
    @DisplayName("Should throw error - startTime in the past")
    fun shouldThrow_startTimeInPast() {
        val testPayload = listOf(
            CreateTimeslotDto(
                startTime = LocalDateTime.now().minusDays(1),
                endTime = LocalDateTime.now().plusMinutes(1)
            )
        )

        val ex = assertThrows<ResponseStatusException> { timeslotService.processTimeslots(1, testPayload) }
        assertEquals("Start and End date must be in future.", ex.reason)
    }

    @Test
    @DisplayName("Should throw error - endTime in the past")
    fun shouldThrow_endTimeInPast() {
        val testPayload = listOf(
            CreateTimeslotDto(
                startTime = LocalDateTime.now().plusMinutes(1),
                endTime = LocalDateTime.now().minusDays(1)
            )
        )

        val ex = assertThrows<ResponseStatusException> { timeslotService.processTimeslots(1, testPayload) }
        assertEquals("Start and End date must be in future.", ex.reason)
    }

    @Test
    @DisplayName("Should throw error - endTime is before startTime")
    fun shouldThrow_endTimeIsBeforeStartTime() {
        val testPayload = listOf(
            CreateTimeslotDto(
                startTime = LocalDateTime.now().plusMinutes(10),
                endTime = LocalDateTime.now().plusMinutes(1)
            )
        )

        val ex = assertThrows<ResponseStatusException> { timeslotService.processTimeslots(1, testPayload) }
        assertEquals("End time can't be before start date.", ex.reason)
    }

    @Test
    fun shouldThrow_overlappingTimeslots() {
        val testPayload = listOf(
            CreateTimeslotDto(
                startTime = LocalDateTime.now().plusMinutes(1),
                endTime = LocalDateTime.now().plusHours(1)
            ),
            CreateTimeslotDto(
                startTime = LocalDateTime.now().plusMinutes(30),
                endTime = LocalDateTime.now().plusHours(2)
            )
        )

        val ex = assertThrows<ResponseStatusException> { timeslotService.processTimeslots(1, testPayload) }
        assertEquals("Overlapping timeslots.", ex.reason)
    }
}
