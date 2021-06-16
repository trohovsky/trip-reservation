package com.github.trohovsky.tripreservation.api

data class CreateTripDto(
    override val fromCity: String,
    override val toCity: String,
    override val capacity: Int,
) : BaseTripDto()
