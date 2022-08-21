package com.medifit.scheduling.appointments.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.medifit.scheduling.appointments.dto.CreateAppointmentDto
import com.medifit.scheduling.appointments.model.Appointment
import com.medifit.scheduling.appointments.service.AppointmentService
import com.medifit.scheduling.config.JwtTokenUtil
import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.sharedlib.IdTransformer
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime


@WebMvcTest(AppointmentController::class)
internal class AppointmentControllerUnitTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var appointmentService: AppointmentService

    @MockkBean
    private lateinit var jwtTokenUtil: JwtTokenUtil

    @Test
    @DisplayName("Should create new appointment")
    @WithMockUser(username = "1")
    fun createAppointment() {
        val body = CreateAppointmentDto(timeslot = "1")

        val appointment = Appointment(
            id = 1,
            patient = 1,
            timeslot = Timeslot(
                id = 1,
                doctor = Doctor(user = 2),
                startTime = LocalDateTime.now().plusMinutes(1),
                endTime = LocalDateTime.now().plusHours(1)
            )
        )

        every { appointmentService.createAppointment(any(), any()) } returns appointment

        mockMvc.perform(
            post("/appointments")
                .content(ObjectMapper().writeValueAsString(body))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(IdTransformer.toExternal(1)))
            .andExpect(jsonPath("$.patient").value(IdTransformer.toExternal(1)))

        verify(exactly = 1) { appointmentService.createAppointment(1, any()) }
    }

    @Test
    @DisplayName("Should return 400 when body is invalid")
    @WithMockUser(username = "1")
    fun throwError_invalidPayload() {
        val body = CreateAppointmentDto()

        mockMvc.perform(
            post("/appointments")
                .content(ObjectMapper().writeValueAsString(body))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Should return 401 anonymous user")
    @WithAnonymousUser
    fun throwError_anonymousUser() {
        val body = CreateAppointmentDto(timeslot = "1")

        mockMvc.perform(
            post("/appointments")
                .content(ObjectMapper().writeValueAsString(body))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("Should be able to cancel appointment")
    @WithMockUser(username = "1")
    fun shouldCancelAppointment() {
        every { appointmentService.cancelAppointment(1, 1) } returns Unit

        mockMvc.perform(post("/appointments/EXTERNAL_1/cancel").with(csrf()))
            .andExpect(status().isNoContent)
    }
}
