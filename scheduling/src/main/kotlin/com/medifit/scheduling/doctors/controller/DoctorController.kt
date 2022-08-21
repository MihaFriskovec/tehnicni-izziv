package com.medifit.scheduling.doctors.controller

import com.medifit.scheduling.doctors.dto.CreateDoctorDto
import com.medifit.scheduling.doctors.dto.DoctorDto
import com.medifit.scheduling.doctors.dto.SpecialtyDto
import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.doctors.service.DoctorService
import com.medifit.scheduling.specialties.model.Speciality
import com.medifit.sharedlib.IdTransformer
import com.medifit.sharedlib.dto.ListResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import javax.validation.Valid

@RestController
@RequestMapping("/doctors")
class DoctorController(private val doctorService: DoctorService) {

    @PreAuthorize("hasAuthority('DOCTOR')")
    @PostMapping
    fun createDoctor(
        principal: Principal,
        @Valid @RequestBody createDoctorDto: CreateDoctorDto
    ): ResponseEntity<DoctorDto> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(doctorService.createDoctor(userId = principal.name.toLong(), doctorDto = createDoctorDto).toDto())
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    fun doctors(): ResponseEntity<ListResponseDto<DoctorDto>> {
        return ResponseEntity.ok(ListResponseDto(doctorService.getDoctors().map { it.toDto() }))
    }

    fun Doctor.toDto() = DoctorDto(
        id = IdTransformer.toExternal(id),
        user = IdTransformer.toExternal(user),
        rating = rating,
        specialties = specialties.map { it.toDto() }
    )

    fun Speciality.toDto() = SpecialtyDto(
        id = IdTransformer.toExternal(id),
        name = name
    )
}
