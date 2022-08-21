package com.medifit.scheduling.timeslots.model

import com.medifit.scheduling.appointments.model.Appointment
import com.medifit.scheduling.doctors.model.Doctor
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.Future

@Entity
@Table(name = "timeslots")
class Timeslot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    val doctor: Doctor,

    @Future
    @Column(nullable = false)
    val startTime: LocalDateTime,

    @Future
    @Column(nullable = false)
    val endTime: LocalDateTime,

    @Column(nullable = false)
    var free: Boolean = true,

    @OneToOne(mappedBy = "timeslot")
    var appointment: Appointment? = null
)
