package com.github.trohovsky.tripreservation.service

import com.github.trohovsky.tripreservation.entity.Reservation
import com.github.trohovsky.tripreservation.entity.Trip

const val FROM_CITY = "fromCity"
const val TO_CITY = "toCity"
const val CAPACITY = 1
const val USERNAME = "username"
const val SPOTS = 1

fun createReservation(trip: Trip): Reservation =
    Reservation(tripId = requireNotNull(trip.id), username = USERNAME, spots = SPOTS)
