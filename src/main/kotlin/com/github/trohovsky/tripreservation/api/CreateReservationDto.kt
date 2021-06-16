package com.github.trohovsky.tripreservation.api

data class CreateReservationDto(
    override val username: String,
    override val spots: Int,
) : BaseReservationDto()
