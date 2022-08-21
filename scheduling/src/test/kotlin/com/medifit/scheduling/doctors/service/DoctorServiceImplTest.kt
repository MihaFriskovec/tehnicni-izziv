package com.medifit.scheduling.doctors.service

import com.medifit.scheduling.doctors.dto.CreateDoctorDto
import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.doctors.model.DoctorRepository
import com.medifit.scheduling.specialties.model.Speciality
import com.medifit.scheduling.specialties.model.SpecialtyRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class DoctorServiceImplTest {
    private val doctorRepository: DoctorRepository = mockk()
    private val specialtyRepository: SpecialtyRepository = mockk()

    private lateinit var doctorService: DoctorService

    @BeforeEach
    fun setup() {
        doctorService = DoctorServiceImpl(doctorRepository, specialtyRepository)
    }

    private val specialtiesMock = listOf(Speciality(id = 1, name = "name"), Speciality(id = 2, name = "name"))
    private val doctorMock = Doctor(id = 1, user = 1)

    @Test
    @DisplayName("Should create new doctor without specialties")
    fun shouldCreateDoctor() {
        val createDoctorDto = CreateDoctorDto(name = "name", specialties = emptyList())

        every { specialtyRepository.findAllById(any()) } returns emptyList()
        every { doctorRepository.save(any()) } returnsArgument 0

        doctorService.createDoctor(1, createDoctorDto)

        verify(exactly = 1) { specialtyRepository.findAllById(any()) }
        verify(exactly = 1) { doctorRepository.save(any()) }
    }

    @Test
    @DisplayName("Should create new doctor with specialties")
    fun shouldCreateDoctorWithSpecialties() {
        val createDoctorDto = CreateDoctorDto(name = "name", specialties = listOf(1, 2))

        every { specialtyRepository.findAllById(any()) } returns specialtiesMock
        every { doctorRepository.save(any()) } returnsArgument 0

        val doctor = doctorService.createDoctor(1, createDoctorDto)

        assertNotNull(doctor)

        verify(exactly = 1) { specialtyRepository.findAllById(any()) }
        verify(exactly = 1) { doctorRepository.save(any()) }
    }

    @Test
    @DisplayName("Should get Doctor by id")
    fun shouldGetDoctorById() {
        every { doctorRepository.findByIdOrNull(any()) } returns doctorMock

        val doctor = doctorService.getDoctorById(1)

        assertNotNull(doctor)
    }

    @Test
    @DisplayName("Should throw error if Doctor not found by id")
    fun shouldThrow_doctorNotFoundById() {
        every { doctorRepository.findByIdOrNull(any()) } returns null

        val ex = assertThrows<ResponseStatusException> { doctorService.getDoctorById(1) }

        assertEquals(HttpStatus.NOT_FOUND, ex.status)
        assertEquals("Doctor not found.", ex.reason)
    }


    @Test
    @DisplayName("Should get Doctor by userId")
    fun shouldGetDoctorByUserId() {
        every { doctorRepository.findByUser(any()) } returns doctorMock

        val doctor = doctorService.getDoctorByUserId(1)

        assertNotNull(doctor)
    }

    @Test
    @DisplayName("Should throw error if Doctor not found by userId")
    fun shouldThrow_doctorNotFoundByUserId() {
        every { doctorRepository.findByUser(any()) } returns null

        val ex = assertThrows<ResponseStatusException> { doctorService.getDoctorByUserId(1) }

        assertEquals(HttpStatus.NOT_FOUND, ex.status)
        assertEquals("Doctor not found.", ex.reason)
    }
}
