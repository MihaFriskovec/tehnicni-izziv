package com.medifit.scheduling.appointments.service

import com.medifit.scheduling.appointments.dto.CreateAppointmentDto
import com.medifit.scheduling.appointments.model.Appointment

interface AppointmentService {
    fun createAppointment(patient: Long, appointmentDto: CreateAppointmentDto): Appointment
    fun cancelAppointment(patient: Long, id: Long)
    fun getAppointments(): List<Appointment>
}
