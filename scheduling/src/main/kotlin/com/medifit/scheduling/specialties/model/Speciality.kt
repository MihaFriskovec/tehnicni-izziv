package com.medifit.scheduling.specialties.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.medifit.scheduling.doctors.model.Doctor
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "specialties")
class Speciality(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @NotEmpty
    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = true)
    var description: String? = null,

    @ManyToMany(mappedBy = "specialties")
    val doctors: Set<Doctor> = hashSetOf(),

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdDate: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false, updatable = true)
    var modifiedDate: LocalDateTime = LocalDateTime.now()
)
