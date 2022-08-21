package com.medifit.scheduling.doctors.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DoctorRepository : CrudRepository<Doctor, Long> {
    fun findByUser(userId: Long): Doctor?
}
