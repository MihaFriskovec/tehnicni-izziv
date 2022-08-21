package com.medifit.scheduling.doctors.model

import com.medifit.scheduling.specialties.model.Speciality
import com.medifit.scheduling.timeslots.model.Timeslot
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "doctors")
class Doctor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, name = "user_id")
    val user: Long,

    @Column(nullable = true)
    var rating: BigDecimal? = null,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "doctor")
    val timeslots: Set<Timeslot> = hashSetOf(),

    @ManyToMany
    @JoinTable(
        name = "doctor_specialty",
        joinColumns = [JoinColumn(name = "doctor_id")],
        inverseJoinColumns = [JoinColumn(name = "specialty_id")]
    )
    val specialties: Set<Speciality> = hashSetOf(),

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdDate: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false, updatable = true)
    var modifiedDate: LocalDateTime = LocalDateTime.now()
)
