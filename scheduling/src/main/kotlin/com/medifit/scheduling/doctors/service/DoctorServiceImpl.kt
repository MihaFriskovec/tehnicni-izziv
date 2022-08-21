package com.medifit.scheduling.doctors.service

import com.medifit.scheduling.doctors.dto.CreateDoctorDto
import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.doctors.model.DoctorRepository
import com.medifit.scheduling.specialties.model.SpecialtyRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@Service
class DoctorServiceImpl(
    private val doctorRepository: DoctorRepository,
    private val specialityRepository: SpecialtyRepository
) : DoctorService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun createDoctor(userId: Long, doctorDto: CreateDoctorDto): Doctor {
        val specialties = specialityRepository.findAllById(doctorDto.specialties).toSet()

        val doctor = Doctor(
            user = userId,
            specialties = specialties
        )

        return try {
            doctorRepository.save(doctor)
        } catch (e: Exception) {
            logger.error("Error creating Doctor, e=${e.message}")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving Doctor.")
        }
    }

    override fun getDoctorById(id: Long): Doctor {
        val doctor = doctorRepository.findByIdOrNull(id)

        if (doctor == null) {
            logger.warn("Doctor not found, id=$id")
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found.")
        }

        return doctor
    }

    override fun getDoctorByUserId(userId: Long): Doctor {
        val doctor = doctorRepository.findByUser(userId)

        if (doctor == null) {
            logger.warn("Doctor not found, userId=$userId")
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found.")
        }

        return doctor
    }

    override fun getDoctorsBySpecialty(id: Long): List<Doctor> {
        TODO("Not yet implemented")
    }

    override fun updateRating(id: Long, rating: BigDecimal): Doctor? {
        val doctor = getDoctorById(id)
        doctor.rating = rating

        return doctorRepository.save(doctor)
    }

    override fun getDoctors(): List<Doctor> {
        return doctorRepository.findAll().toList()
    }
}
