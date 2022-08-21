package com.medifit.scheduling.timeslots.controller

import com.medifit.scheduling.timeslots.dto.CreateTimeslotDto
import com.medifit.scheduling.timeslots.dto.TimeslotBulkUploadDto
import com.medifit.scheduling.timeslots.dto.TimeslotDto
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.scheduling.timeslots.service.TimeslotService
import com.medifit.sharedlib.IdTransformer
import com.medifit.sharedlib.dto.ListResponseDto
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDate
import javax.validation.Valid

@RestController
@RequestMapping("/timeslots")
class TimeslotController(private val timeslotService: TimeslotService) {

    @PreAuthorize("hasAuthority('DOCTOR')")
    @PostMapping
    fun uploadTimeslots(
        principal: Principal,
        @Valid @RequestBody timeslots: List<CreateTimeslotDto>
    ): ResponseEntity<TimeslotBulkUploadDto> {
        val processed = timeslotService.processTimeslots(userId = principal.name.toLong(), timeslots = timeslots)

        return ResponseEntity.status(HttpStatus.CREATED).body(TimeslotBulkUploadDto(processed.size))
    }

    @GetMapping("/doctor/{id}")
    fun timeslotsByDoctor(@PathVariable id: String): ResponseEntity<ListResponseDto<TimeslotDto>> {
        val timeslots = timeslotService.listTimeslotsByDoctorId(IdTransformer.toInternal(id))

        return ResponseEntity.ok(ListResponseDto(timeslots.map { it.toDto() }))
    }

    @GetMapping
    fun timeslotsByDoctor(
        @RequestParam doctor: List<String>? = null,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") startDate: LocalDate? = null,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") endDate: LocalDate? = null
    ): ResponseEntity<ListResponseDto<TimeslotDto>> {
        val timeslots = timeslotService.listTimeslots(
            doctors = doctor,
            from = startDate,
            to = endDate
        )

        return ResponseEntity.ok(ListResponseDto(timeslots.map { it.toDto() }))
    }


    fun Timeslot.toDto() = TimeslotDto(
        id = IdTransformer.toExternal(id),
        doctor = IdTransformer.toExternal(doctor.id),
        startTime = startTime,
        endTime = endTime,
        free = free
    )
}
