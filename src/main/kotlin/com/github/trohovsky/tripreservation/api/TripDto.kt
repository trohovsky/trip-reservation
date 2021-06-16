package com.github.trohovsky.tripreservation.api

data class TripDto(
    val id: Int,
    override val fromCity: String,
    override val toCity: String,
    override val capacity: Int,
) : BaseTripDto()
