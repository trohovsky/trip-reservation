package com.github.trohovsky.tripreservation.repository

import com.github.trohovsky.tripreservation.entity.Trip
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import javax.persistence.EntityNotFoundException

interface TripRepository : JpaRepository<Trip, Int>

fun TripRepository.findByIdOrThrow(id: Int): Trip =
    findByIdOrNull(id) ?: throw EntityNotFoundException("Trip with id $id could not be found.")
