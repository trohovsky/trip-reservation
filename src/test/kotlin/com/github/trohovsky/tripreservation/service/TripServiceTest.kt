package com.github.trohovsky.tripreservation.service

import com.github.trohovsky.tripreservation.api.CreateTripDto
import com.github.trohovsky.tripreservation.api.TripDto
import com.github.trohovsky.tripreservation.entity.Trip
import com.github.trohovsky.tripreservation.repository.TripRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import javax.persistence.EntityNotFoundException

private const val ID = 0

private val TRIP = Trip(
    id = ID,
    fromCity = FROM_CITY,
    toCity = TO_CITY,
    capacity = CAPACITY
)
private val TRIP_DTO = TripDto(
    id = ID,
    fromCity = FROM_CITY,
    toCity = TO_CITY,
    capacity = CAPACITY
)
private val CREATE_TRIP = TRIP.copy(id = null)
private val CREATE_TRIP_DTO = CreateTripDto(
    fromCity = FROM_CITY,
    toCity = TO_CITY,
    capacity = CAPACITY
)

internal class TripServiceTest {

    private val tripRepository: TripRepository = mockk()
    private val tripService = TripService(tripRepository)

    @Test
    fun `getAll returns all trips`() {
        every { tripRepository.findAll() } returns listOf(TRIP)

        val trips = tripService.getAll()

        assertThat(trips).containsExactly(TRIP_DTO)
    }

    @Test
    fun `getById returns the trip`() {
        every { tripRepository.findByIdOrNull(ID) } returns TRIP

        val retrievedTrip = tripService.getById(ID)

        assertThat(retrievedTrip).isEqualTo(TRIP_DTO)
    }

    @Test
    fun `getById throws EntityNotFoundException if the trip does not exist`() {
        every { tripRepository.findByIdOrNull(ID) } returns null

        assertThrows<EntityNotFoundException> { tripService.getById(ID) }
    }

    @Test
    fun `create returns the saved trip`() {
        every { tripRepository.save(CREATE_TRIP) } returns TRIP

        val savedTrip = tripService.create(CREATE_TRIP_DTO)

        assertThat(savedTrip).isEqualTo(TRIP_DTO)
    }

    @Test
    fun `delete calls tripRepository#deleteById`() {
        every { tripRepository.findByIdOrNull(ID) } returns TRIP
        every { tripRepository.deleteById(ID) } returns Unit

        tripService.delete(ID)

        verify { tripRepository.deleteById(ID) }
    }

    @Test
    fun `delete throws EntityNotFoundException if the trip does not exist`() {
        every { tripRepository.findByIdOrNull(ID) } returns null
        every { tripRepository.deleteById(ID) } returns Unit

        assertThrows<EntityNotFoundException> { tripService.delete(ID) }
    }
}
