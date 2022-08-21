package com.medifit.scheduling.specialties.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SpecialtyRepository : CrudRepository<Speciality, Long> {
}
