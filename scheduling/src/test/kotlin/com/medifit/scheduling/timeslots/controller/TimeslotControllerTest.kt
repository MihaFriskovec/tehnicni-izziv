package com.medifit.scheduling.timeslots.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.medifit.scheduling.config.JwtTokenUtil
import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.timeslots.dto.CreateTimeslotDto
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.scheduling.timeslots.service.TimeslotService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime


@ContextConfiguration
@WebMvcTest(TimeslotController::class)
internal class TimeslotControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var timeslotService: TimeslotService

    @MockkBean
    private lateinit var jwtTokenUtil: JwtTokenUtil

    private val timeslots = listOf(
        CreateTimeslotDto(
            startTime = LocalDateTime.now().plusHours(1),
            endTime = LocalDateTime.now().plusHours(2)
        ),
        CreateTimeslotDto(
            startTime = LocalDateTime.now().plusHours(2),
            endTime = LocalDateTime.now().plusHours(3)
        )
    )

    private val processedTimeslots = listOf(
        Timeslot(
            id = 1,
            startTime = timeslots[0].startTime!!,
            endTime = timeslots[0].endTime!!,
            doctor = Doctor(user = 1),
            free = true
        ), Timeslot(
            id = 2,
            startTime = timeslots[1].startTime!!,
            endTime = timeslots[1].endTime!!,
            doctor = Doctor(user = 1),
            free = true
        )
    )

    @Test
    @DisplayName("Should upload timeslots for doctor")
    @WithMockUser(username = "1", authorities = ["DOCTOR"])
    fun shouldUploadTimeslots() {

        every { timeslotService.processTimeslots(1, any()) } returns processedTimeslots

        mockMvc.perform(
            post("/timeslots").content(ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(timeslots))
                .contentType(MediaType.APPLICATION_JSON).with(csrf())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.numberOfCreatedSlots").value(2))
    }

    @Test
    @DisplayName("Should list timeslots by doctor")
    @WithMockUser
    fun shouldListTimeslotsByDoctor() {
        every { timeslotService.listTimeslotsByDoctorId(1) } returns processedTimeslots

        mockMvc.perform(get("/timeslots/doctor/EXTERNAL_1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].id").value("EXTERNAL_1"))
            .andExpect(jsonPath("$.data[1].id").value("EXTERNAL_2"))

    }

}
