package com.medifit.scheduling.appointments.controller

import com.medifit.scheduling.appointments.dto.AppointmentDto
import com.medifit.scheduling.appointments.dto.CreateAppointmentDto
import com.medifit.scheduling.appointments.model.Appointment
import com.medifit.scheduling.appointments.service.AppointmentService
import com.medifit.sharedlib.IdTransformer
import com.medifit.sharedlib.dto.ListResponseDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@RestController
@RequestMapping("/appointments")
class AppointmentController(private val appointmentService: AppointmentService) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    fun createAppointment(
        principal: Principal,
        @Valid @RequestBody appointmentDto: CreateAppointmentDto
    ): ResponseEntity<AppointmentDto> {
        val appointment = appointmentService.createAppointment(
            patient = principal.name.toLong(),
            appointmentDto = appointmentDto
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(appointment.toDto())
    }

    @PostMapping("/{id}/cancel")
    fun cancelAppointment(
        principal: Principal,
        @NotNull @NotEmpty @PathVariable id: String
    ): ResponseEntity<Void> {
        appointmentService.cancelAppointment(patient = principal.name.toLong(), id = IdTransformer.toInternal(id))
        return ResponseEntity.noContent().build()
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    fun appointments(): ResponseEntity<ListResponseDto<AppointmentDto>> {
        return ResponseEntity.ok(ListResponseDto(appointmentService.getAppointments().map { it.toDto() }))
    }

    fun Appointment.toDto() = AppointmentDto(
        id = IdTransformer.toExternal(id),
        patient = IdTransformer.toExternal(patient),
        timeslot = IdTransformer.toExternal(timeslot.id)
    )
}
