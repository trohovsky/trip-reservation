package com.github.trohovsky.tripreservation.api

abstract class BaseTripDto {
    abstract val fromCity: String
    abstract val toCity: String
    abstract val capacity: Int
}
