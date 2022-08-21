package com.medifit.scheduling.timeslots.service

import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.doctors.model.DoctorRepository
import com.medifit.scheduling.specialties.model.Speciality
import com.medifit.scheduling.specialties.model.SpecialtyRepository
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.scheduling.timeslots.model.TimeslotRepository
import com.medifit.sharedlib.IdTransformer
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DataJpaTest
internal class TimeslotServiceIntegrationTest {

    @Autowired
    private lateinit var timeslotRepository: TimeslotRepository

    @Autowired
    private lateinit var doctorRepository: DoctorRepository

    @Autowired
    private lateinit var specialtyRepository: SpecialtyRepository

    private lateinit var timeslotService: TimeslotService

    @BeforeEach
    fun setUp() {
        timeslotService = TimeslotServiceImpl(timeslotRepository, doctorRepository, SimpleMeterRegistry())
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    @DisplayName("Should list Timeslots default filters")
    fun listTimeslotsWitDefaultFilters() {
        val speciality = specialtyRepository.save(Speciality(name = "name"))
        val doctor = doctorRepository.save(Doctor(user = 1, specialties = setOf(speciality)))
        timeslotRepository.save(
            Timeslot(
                doctor = doctor,
                startTime = LocalDateTime.now().plusHours(1),
                endTime = LocalDateTime.now().plusHours(2)
            )
        )

        val timeslots = timeslotService.listTimeslots(emptyList(), null, null)
        assertNotNull(timeslots)
        assertEquals(1, timeslots.size)
    }

    @Test
    @DisplayName("Should empty list if there are no timeslots in database")
    fun shouldReturnEmptyList_NoTimeslotsInDB() {
        val timeslots = timeslotService.listTimeslots(emptyList(), null, null)
        assertNotNull(timeslots)
        assertEquals(0, timeslots.size)
    }

    @Test
    @DisplayName("Should empty list if there are no free timeslots")
    fun shouldReturnEmptyList_NoFreeTimeslots() {
        val speciality = specialtyRepository.save(Speciality(name = "name"))
        val doctor = doctorRepository.save(Doctor(user = 1, specialties = setOf(speciality)))
        timeslotRepository.save(
            Timeslot(
                doctor = doctor,
                startTime = LocalDateTime.now().plusHours(1),
                endTime = LocalDateTime.now().plusHours(2),
                free = false
            )
        )
        val timeslots = timeslotService.listTimeslots(emptyList(), null, null)
        assertNotNull(timeslots)
        assertEquals(0, timeslots.size)
    }

    @Test
    @DisplayName("Should empty list if there are no free timeslots in default date range")
    fun shouldReturnEmptyList_NoFreeTimeslotsInDefaultDateRange() {
        val speciality = specialtyRepository.save(Speciality(name = "name"))
        val doctor = doctorRepository.save(Doctor(user = 1, specialties = setOf(speciality)))
        timeslotRepository.save(
            Timeslot(
                doctor = doctor,
                startTime = LocalDateTime.now().plusYears(1),
                endTime = LocalDateTime.now().plusYears(1).plusDays(1)
            )
        )
        val timeslots = timeslotService.listTimeslots(emptyList(), null, null)
        assertNotNull(timeslots)
        assertEquals(0, timeslots.size)
    }

    @Test
    @DisplayName("Should list timeslots for given doctor")
    fun shouldListTimeslotsForGivenUser() {
        val speciality = specialtyRepository.save(Speciality(name = "name"))
        val doctor = doctorRepository.save(Doctor(user = 1, specialties = setOf(speciality)))
        timeslotRepository.save(
            Timeslot(
                doctor = doctor,
                startTime = LocalDateTime.now().plusHours(1),
                endTime = LocalDateTime.now().plusHours(2)
            )
        )

        val timeslots = timeslotService.listTimeslots(listOf(IdTransformer.toExternal(doctor.id)), null, null)

        assertNotNull(timeslots)
        assertEquals(1, timeslots.size)
    }
}
