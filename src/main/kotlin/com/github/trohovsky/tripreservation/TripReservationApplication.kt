package com.github.trohovsky.tripreservation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TripReservationApplication

fun main(args: Array<String>) {
    runApplication<TripReservationApplication>(*args)
}
