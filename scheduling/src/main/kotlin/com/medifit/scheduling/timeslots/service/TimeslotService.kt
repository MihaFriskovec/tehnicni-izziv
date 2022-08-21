package com.medifit.scheduling.timeslots.service

import com.medifit.scheduling.timeslots.dto.CreateTimeslotDto
import com.medifit.scheduling.timeslots.model.Timeslot
import java.time.LocalDate

interface TimeslotService {
    fun processTimeslots(userId: Long, timeslots: List<CreateTimeslotDto>): List<Timeslot>
    fun listTimeslotsByDoctorId(doctorId: Long): List<Timeslot>
    fun listTimeslots(doctors: List<String>?, from: LocalDate?, to: LocalDate?): List<Timeslot>
}
