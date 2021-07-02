package com.github.trohovsky.tripreservation.service

import com.github.trohovsky.tripreservation.api.CreateReservationDto
import com.github.trohovsky.tripreservation.api.ReservationDto
import com.github.trohovsky.tripreservation.api.UpdateReservationDto
import com.github.trohovsky.tripreservation.entity.Reservation
import com.github.trohovsky.tripreservation.entity.Trip
import com.github.trohovsky.tripreservation.repository.ReservationRepository
import com.github.trohovsky.tripreservation.repository.TripRepository
import com.github.trohovsky.tripreservation.repository.findByIdOrThrow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class ReservationService(
    private val tripRepository: TripRepository,
    private val reservationRepository: ReservationRepository,
) {

    fun getAll(tripId: Int): List<ReservationDto> {
        tripRepository.findByIdOrThrow(tripId)
        return reservationRepository.findByTripId(tripId).map(Reservation::toReservationDto)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun create(tripId: Int, createReservationDto: CreateReservationDto): ReservationDto {
        val trip = tripRepository.findByIdOrThrow(tripId)
        val reservation = createReservationDto.toReservation(tripId)
        validateCapacity(trip, reservation)
        return reservationRepository.save(reservation).toReservationDto()
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun update(tripId: Int, reservationId: Int, updateReservationDto: UpdateReservationDto): ReservationDto {
        val trip = tripRepository.findByIdOrThrow(tripId)
        val reservationToBeUpdated = reservationRepository.findByIdOrThrow(reservationId)
        val reservation = updateReservationDto.toReservation(reservationToBeUpdated)
        validateCapacity(trip, reservation)
        return reservationRepository.save(reservation).toReservationDto()
    }

    private fun validateCapacity(trip: Trip, reservation: Reservation) {
        if (reservation.spots <= 0) {
            throw IllegalArgumentException("A reservation must have more than 0 spots.")
        }
        val reservedSpots = reservationRepository.getNumberOfReservedSpots(reservation.tripId, reservation.id)
        if (reservedSpots + reservation.spots > trip.capacity) {
            throw IllegalArgumentException(
                "Not enough free spots, only ${trip.capacity - reservedSpots} spots are available."
            )
        }
    }

    fun delete(tripId: Int, id: Int) {
        tripRepository.findByIdOrThrow(tripId)
        reservationRepository.findByIdOrThrow(id)
        reservationRepository.deleteById(id)
    }
}

private fun Reservation.toReservationDto(): ReservationDto = ReservationDto(
    id = checkNotNull(id),
    tripId = tripId,
    username = username,
    spots = spots
)

private fun CreateReservationDto.toReservation(tripId: Int): Reservation = Reservation(
    tripId = tripId,
    username = username,
    spots = spots
)

private fun UpdateReservationDto.toReservation(reservation: Reservation): Reservation = Reservation(
    id = reservation.id,
    tripId = reservation.tripId,
    username = reservation.username,
    spots = spots
)
