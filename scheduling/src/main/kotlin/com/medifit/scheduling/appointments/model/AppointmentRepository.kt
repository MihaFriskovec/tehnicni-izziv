package com.medifit.scheduling.appointments.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AppointmentRepository : CrudRepository<Appointment, Long> {
}
