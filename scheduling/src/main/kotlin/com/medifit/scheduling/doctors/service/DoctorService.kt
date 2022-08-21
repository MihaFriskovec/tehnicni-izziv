package com.medifit.scheduling.doctors.service

import com.medifit.scheduling.doctors.dto.CreateDoctorDto
import com.medifit.scheduling.doctors.model.Doctor
import java.math.BigDecimal

interface DoctorService {
    fun createDoctor(userId: Long, doctorDto: CreateDoctorDto): Doctor
    fun getDoctorById(id: Long): Doctor
    fun getDoctorByUserId(userId: Long): Doctor
    fun getDoctorsBySpecialty(id: Long): List<Doctor>
    fun updateRating(id: Long, rating: BigDecimal): Doctor?
    fun getDoctors(): List<Doctor>
}
