package com.medifit.scheduling.timeslots.service

import com.medifit.scheduling.doctors.model.Doctor
import com.medifit.scheduling.doctors.model.DoctorRepository
import com.medifit.scheduling.timeslots.dto.CreateTimeslotDto
import com.medifit.scheduling.timeslots.model.Timeslot
import com.medifit.scheduling.timeslots.model.TimeslotRepository
import com.medifit.sharedlib.IdTransformer
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class TimeslotServiceImpl(
    private val timeslotRepository: TimeslotRepository,
    private val doctorRepository: DoctorRepository,
    meterRegistry: MeterRegistry
) : TimeslotService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun processTimeslots(userId: Long, timeslotsDto: List<CreateTimeslotDto>): List<Timeslot> {
        val doctor = doctorRepository.findByUser(userId)

        if (doctor == null) {
            logger.error("Doctor does not exist, userId=$userId")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor does not exist.")
        }

        validateTimeslots(timeslotsDto)
        val timeslotsToCreate = transformTimeslots(doctor, timeslotsDto)

        return try {
            timeslotRepository.saveAll(timeslotsToCreate).toList()
        } catch (e: Exception) {
            logger.error("Error saving timeslots, e=${e.message}")

            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred when saving Timeslots.")
        }
    }

    override fun listTimeslotsByDoctorId(doctorId: Long): List<Timeslot> {
        val doctor = doctorRepository.findByIdOrNull(doctorId)

        if (doctor == null) {
            logger.warn("Doctor not found, doctorId=$doctorId")
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found")
        }

        logger.info(doctor.id.toString())
        logger.info(doctor.timeslots.size.toString())

        return timeslotRepository.findByDoctor(doctor)
    }

    override fun listTimeslots(doctors: List<String>?, from: LocalDate?, to: LocalDate?): List<Timeslot> {
        val doctorIds = doctors?.map { IdTransformer.toInternal(it) }
        val fromDate = from ?: LocalDate.now()
        val toDate = to ?: LocalDate.now().plusWeeks(1)

        logger.info("Query Doctors: ${doctorIds.toString()}")
        logger.info("Query From: ${fromDate.atStartOfDay()}")
        logger.info("Query To: ${toDate.atTime(23, 59, 59)}")

        val timeslots = if (doctorIds.isNullOrEmpty()) {
            timeslotRepository.findFreeTimeslots(
                from = fromDate.atStartOfDay(),
                to = toDate.atTime(23, 59, 59)
            )
        } else {
            timeslotRepository.findFreeTimeslots(
                doctors = doctorIds,
                from = fromDate.atStartOfDay(),
                to = toDate.atTime(23, 59, 59)
            )
        }

        logger.info("Found ${timeslots.size} matching the query")

        return timeslots
    }

    private fun transformTimeslots(doctor: Doctor, timeslots: List<CreateTimeslotDto>): List<Timeslot> {
        return timeslots.map {
            Timeslot(
                startTime = it.startTime!!,
                endTime = it.endTime!!,
                doctor = doctor,
                free = true
            )
        }
    }

    private fun validateTimeslots(timeslots: List<CreateTimeslotDto>) {
        val validatedTimeslots = mutableListOf<CreateTimeslotDto>()

        for (ts in timeslots) {
            if (ts.startTime == null || ts.endTime == null) {
                logger.warn("Start or end time not set")
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and End time have to be set.")
            }
            // Start and/or end datetime not in the past
            if (ts.startTime.isBefore(LocalDateTime.now()) || ts.endTime.isBefore(LocalDateTime.now())) {
                logger.warn("Start or end time are in the past")
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and End date must be in future.")
            }
            if (ts.startTime.isAfter(ts.endTime)) {
                logger.warn("End time is before start time")
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "End time can't be before start date.")
            }

            // Check for overlapping
            if (validatedTimeslots.size > 0) {
                for (vts in validatedTimeslots) {
                    if (ts.startTime.isBefore(vts.endTime) && ts.endTime.isAfter(vts.startTime)) {
                        logger.warn("Overlapping")
                        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Overlapping timeslots.")
                    }
                }
            }

            validatedTimeslots.add(ts)
        }

    }
}
