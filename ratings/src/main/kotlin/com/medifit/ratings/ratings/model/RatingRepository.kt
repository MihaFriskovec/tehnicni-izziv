package com.medifit.ratings.ratings.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : CrudRepository<Rating, Long>{
    fun getRatingByDoctor(doctor: Long): Rating?
}
