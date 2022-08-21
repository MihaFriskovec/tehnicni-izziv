package com.medifit.scheduling.appointments.model

import com.medifit.scheduling.timeslots.model.Timeslot
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "appointments")
class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val patient: Long,

    @OneToOne
    val timeslot: Timeslot,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdDate: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false, updatable = true)
    var modifiedDate: LocalDateTime = LocalDateTime.now()
)
