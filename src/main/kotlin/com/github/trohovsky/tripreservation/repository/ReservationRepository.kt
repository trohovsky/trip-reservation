package com.github.trohovsky.tripreservation.repository

import com.github.trohovsky.tripreservation.entity.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import javax.persistence.EntityNotFoundException

@Repository
interface ReservationRepository : JpaRepository<Reservation, Int> {

    fun findByTripId(tripId: Int): List<Reservation>

    @Query(
        """select coalesce(sum(r.spots), 0)
            from Reservation r
            where r.tripId = :tripId
            and r.id <> :id"""
    )
    fun getNumberOfReservedSpots(tripId: Int, id: Int?): Int
}

fun ReservationRepository.findByIdOrThrow(id: Int): Reservation =
    findByIdOrNull(id) ?: throw EntityNotFoundException("Reservation with id $id could not be found.")
