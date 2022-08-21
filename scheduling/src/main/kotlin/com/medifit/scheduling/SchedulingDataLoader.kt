package com.medifit.scheduling

import com.medifit.scheduling.appointments.model.Appointment
import com.medifit.scheduling.appointments.model.AppointmentRepository
import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.doctors.model.DoctorRepository
import com.medifit.scheduling.specialties.model.Speciality
import com.medifit.scheduling.specialties.model.SpecialtyRepository
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.scheduling.timeslots.model.TimeslotRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.EntityManager

@Component
class SchedulingDataLoader(
    private val entityManager: EntityManager,
    private val specialtyRepository: SpecialtyRepository,
    private val doctorRepository: DoctorRepository,
    private val timeslotRepository: TimeslotRepository,
    private val appointmentRepository: AppointmentRepository
) : CommandLineRunner {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun run(vararg args: String?) {
        val executed = entityManager.createNativeQuery("SELECT executed FROM demodatasettings").singleResult.toString().toBoolean()

        logger.info("Demo data loader status: enabled=${!executed}")

        if (!executed) {
            logger.warn("Deleting all old data")
            appointmentRepository.deleteAll()
            timeslotRepository.deleteAll()
            doctorRepository.deleteAll()
            specialtyRepository.deleteAll()

            logger.info("Starting to load demo data...")
            val specialties = listOf(
                Speciality(name = "Allergy and immunolog"),
                Speciality(name = "Adolescent medicine"),
                Speciality(name = "Cardiology"),
                Speciality(name = "Dermatology"),
                Speciality(name = "Family Medicine"),
                Speciality(name = "Internal medicine"),
                Speciality(name = "Pediatrics"),
            )

            logger.info("Saving specialties")
            specialtyRepository.saveAll(specialties).toList()

            val doctors = listOf(
                Doctor(
                    user = 3,
                    specialties = setOf(specialties[0], specialties[2], specialties[4], specialties[6])
                ),
                Doctor(
                    user = 4,
                    specialties = setOf(specialties[1], specialties[3], specialties[5], specialties[6])
                ),
            )

            logger.info("Saving doctors")
            doctorRepository.saveAll(doctors)

            val timeslots = listOf(
                Timeslot(
                    doctor = doctors[0],
                    startTime = LocalDateTime.now().plusMinutes(30),
                    endTime = LocalDateTime.now().plusMinutes(60),
                    free = false
                ),
                Timeslot(
                    doctor = doctors[0],
                    startTime = LocalDateTime.now().plusMinutes(90),
                    endTime = LocalDateTime.now().plusMinutes(120)
                ),
                Timeslot(
                    doctor = doctors[0],
                    startTime = LocalDateTime.now().plusDays(2),
                    endTime = LocalDateTime.now().plusDays(2).plusHours(1),
                    free = false
                ),
                Timeslot(
                    doctor = doctors[1],
                    startTime = LocalDateTime.now().plusHours(1),
                    endTime = LocalDateTime.now().plusHours(2)
                ),
                Timeslot(
                    doctor = doctors[1],
                    startTime = LocalDateTime.now().plusHours(2),
                    endTime = LocalDateTime.now().plusHours(3),
                    free = false
                )
            )

            logger.info("Saving timeslots")
            timeslotRepository.saveAll(timeslots)

            val appointments = listOf(
                Appointment(
                    patient = 2,
                    timeslot = timeslots[0]
                ),
                Appointment(
                    patient = 1,
                    timeslot = timeslots[2]
                ),
                Appointment(
                    patient = 2,
                    timeslot = timeslots[4]
                ),
            )

            logger.info("Saving appointments")
            appointmentRepository.saveAll(appointments)
            logger.info("Demo data successfully imported.")
            entityManager.createNativeQuery("UPDATE demodatasettings SET executed = true WHERE 1 = 1").executeUpdate()
        }
    }
}
