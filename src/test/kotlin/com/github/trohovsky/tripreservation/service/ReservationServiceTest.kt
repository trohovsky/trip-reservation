package com.github.trohovsky.tripreservation.service

import com.github.trohovsky.tripreservation.api.CreateReservationDto
import com.github.trohovsky.tripreservation.api.ReservationDto
import com.github.trohovsky.tripreservation.api.UpdateReservationDto
import com.github.trohovsky.tripreservation.entity.Reservation
import com.github.trohovsky.tripreservation.entity.Trip
import com.github.trohovsky.tripreservation.repository.ReservationRepository
import com.github.trohovsky.tripreservation.repository.TripRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import javax.persistence.EntityNotFoundException

private const val TRIP_ID = 0
private const val TRIP_CAPACITY = 2
private const val RESERVATION_ID = 0
private const val RESERVED_SPOTS = 1
private const val INVALID_SPOTS = 0
private const val RESERVED_SPOTS_IN_TOTAL = 0

private val TRIP = Trip(
    id = TRIP_ID,
    fromCity = FROM_CITY,
    toCity = TO_CITY,
    capacity = TRIP_CAPACITY
)
private val RESERVATION = Reservation(
    id = RESERVATION_ID,
    tripId = TRIP_ID,
    username = USERNAME,
    spots = RESERVED_SPOTS
)
private val RESERVATION_DTO = ReservationDto(
    id = RESERVATION_ID,
    tripId = TRIP_ID,
    username = USERNAME,
    spots = RESERVED_SPOTS
)
private val CREATE_RESERVATION = RESERVATION.copy(id = null)
private val CREATE_RESERVATION_DTO = CreateReservationDto(
    username = USERNAME,
    spots = RESERVED_SPOTS
)
private val UPDATE_RESERVATION_DTO = UpdateReservationDto(
    spots = RESERVED_SPOTS
)

internal class ReservationServiceTest {

    private val tripRepository: TripRepository = mockk()
    private val reservationRepository: ReservationRepository = mockk()
    private val reservationService = ReservationService(tripRepository, reservationRepository)

    @Test
    fun `getAll returns all entries`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns TRIP
        every { reservationRepository.findByTripId(TRIP_ID) } returns listOf(RESERVATION)

        val entries = reservationService.getAll(TRIP_ID)

        assertThat(entries).containsExactly(RESERVATION_DTO)
    }

    @Test
    fun `getAll throws EntityNotFoundException if the trip does not exist`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns null

        assertThrows<EntityNotFoundException> { reservationService.getAll(TRIP_ID) }
    }

    @Test
    fun `create returns the saved reservation`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns TRIP
        every { reservationRepository.getNumberOfReservedSpots(TRIP_ID, null) } returns RESERVED_SPOTS_IN_TOTAL
        every { reservationRepository.save(CREATE_RESERVATION) } returns RESERVATION

        val savedReservation = reservationService.create(TRIP_ID, CREATE_RESERVATION_DTO)

        assertThat(savedReservation).isEqualTo(RESERVATION_DTO)
    }

    @Test
    fun `create throws EntityNotFoundException if the trip does not exist`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns null

        assertThrows<EntityNotFoundException> { reservationService.create(TRIP_ID, CREATE_RESERVATION_DTO) }
    }

    @Test
    fun `create throws IllegalArgumentException if reservation's spots are less than 1`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns TRIP

        assertThrows<IllegalArgumentException> {
            reservationService.create(TRIP_ID, CREATE_RESERVATION_DTO.copy(spots = INVALID_SPOTS))
        }
    }

    @Test
    fun `create throws IllegalArgumentException if reservation's spots exceed capacity`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns TRIP
        every { reservationRepository.getNumberOfReservedSpots(TRIP_ID, null) } returns TRIP_CAPACITY

        assertThrows<IllegalArgumentException> { reservationService.create(TRIP_ID, CREATE_RESERVATION_DTO) }
    }


    @Test
    fun `update returns the updated reservation`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns TRIP
        every { reservationRepository.findByIdOrNull(RESERVATION_ID) } returns RESERVATION
        every { reservationRepository.getNumberOfReservedSpots(TRIP_ID, RESERVATION_ID) } returns
                RESERVED_SPOTS_IN_TOTAL
        every { reservationRepository.save(RESERVATION) } returns RESERVATION

        val savedReservation = reservationService.update(TRIP_ID, RESERVATION_ID, UPDATE_RESERVATION_DTO)

        assertThat(savedReservation).isEqualTo(RESERVATION_DTO)
    }

    @Test
    fun `update throws EntityNotFoundException if the trip does not exist`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns null

        assertThrows<EntityNotFoundException> {
            reservationService.update(TRIP_ID, RESERVATION_ID, UPDATE_RESERVATION_DTO)
        }
    }

    @Test
    fun `update throws EntityNotFoundException if the reservation does not exist`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns TRIP
        every { reservationRepository.findByIdOrNull(RESERVATION_ID) } returns null

        assertThrows<EntityNotFoundException> {
            reservationService.update(TRIP_ID, RESERVATION_ID, UPDATE_RESERVATION_DTO)
        }
    }

    @Test
    fun `update throws IllegalArgumentException if reservation's spots are less than 1`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns TRIP
        every { reservationRepository.findByIdOrNull(RESERVATION_ID) } returns RESERVATION

        assertThrows<IllegalArgumentException> {
            reservationService.update(TRIP_ID, RESERVATION_ID, UPDATE_RESERVATION_DTO.copy(spots = INVALID_SPOTS))
        }
    }

    @Test
    fun `update throws IllegalArgumentException if reservation's spots exceed capacity`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns TRIP
        every { reservationRepository.findByIdOrNull(RESERVATION_ID) } returns RESERVATION
        every { reservationRepository.getNumberOfReservedSpots(TRIP_ID, RESERVATION_ID) } returns TRIP_CAPACITY

        assertThrows<IllegalArgumentException> {
            reservationService.update(TRIP_ID, RESERVATION_ID, UPDATE_RESERVATION_DTO)
        }
    }

    @Test
    fun `delete calls reservationRepository#deleteById`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns TRIP
        every { reservationRepository.findByIdOrNull(RESERVATION_ID) } returns RESERVATION
        every { reservationRepository.deleteById(RESERVATION_ID) } returns Unit

        reservationService.delete(TRIP_ID, RESERVATION_ID)

        verify { reservationRepository.deleteById(RESERVATION_ID) }
    }

    @Test
    fun `delete throws EntityNotFoundException if the trip does not exist`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns null

        assertThrows<EntityNotFoundException> { reservationService.delete(TRIP_ID, RESERVATION_ID) }
    }

    @Test
    fun `delete throws EntityNotFoundException if the reservation does not exist`() {
        every { tripRepository.findByIdOrNull(TRIP_ID) } returns TRIP
        every { tripRepository.findByIdOrNull(RESERVATION_ID) } returns null

        assertThrows<EntityNotFoundException> { reservationService.delete(TRIP_ID, RESERVATION_ID) }
    }
}
