package com.github.trohovsky.tripreservation.service

import com.github.trohovsky.tripreservation.api.CreateTripDto
import com.github.trohovsky.tripreservation.api.TripDto
import com.github.trohovsky.tripreservation.entity.Trip
import com.github.trohovsky.tripreservation.repository.TripRepository
import com.github.trohovsky.tripreservation.repository.findByIdOrThrow
import org.springframework.stereotype.Service

@Service
class TripService(private val tripRepository: TripRepository) {

    fun getAll(): List<TripDto> = tripRepository.findAll().map(Trip::toTripDto)

    fun getById(id: Int): TripDto = tripRepository.findByIdOrThrow(id).toTripDto()

    fun create(createTripDto: CreateTripDto): TripDto = tripRepository.save(createTripDto.toTrip()).toTripDto()

    fun delete(id: Int) {
        tripRepository.findByIdOrThrow(id)
        tripRepository.deleteById(id)
    }
}

private fun Trip.toTripDto(): TripDto = TripDto(
    id = checkNotNull(id),
    fromCity = fromCity,
    toCity = toCity,
    capacity = capacity
)

private fun CreateTripDto.toTrip(id: Int? = null): Trip = Trip(
    id = id,
    fromCity = fromCity,
    toCity = toCity,
    capacity = capacity
)
