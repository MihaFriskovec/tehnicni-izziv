package com.medifit.sharedlib.dto

import java.math.BigDecimal

data class RatingMessage(
    val doctor: Long? = null,
    val rating: BigDecimal? = null
)
