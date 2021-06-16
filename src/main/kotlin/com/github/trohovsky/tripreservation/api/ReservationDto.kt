package com.github.trohovsky.tripreservation.api


data class ReservationDto(
    val id: Int,
    val tripId: Int,
    val username: String,
    val spots: Int,
)
