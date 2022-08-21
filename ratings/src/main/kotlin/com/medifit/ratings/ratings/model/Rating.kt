package com.medifit.ratings.ratings.model

import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "ratings")
class Rating(
    @Column(name = "doctor_id", nullable = false, unique = true, updatable = false)
    val doctor: Long,

    @Column(nullable = true)
    var rating: BigDecimal? = null,

    @Column(nullable = true)
    var totalNumberOfRatings: Int? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)
