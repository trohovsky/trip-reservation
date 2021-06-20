package com.github.trohovsky.tripreservation.service

import com.github.trohovsky.tripreservation.entity.Trip
import com.github.trohovsky.tripreservation.repository.ReservationRepository
import com.github.trohovsky.tripreservation.repository.TripRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
internal class ReservationRepositoryIT {

    @Autowired
    private lateinit var tripRepository: TripRepository

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @AfterEach
    internal fun tearDown() {
        tripRepository.deleteAll()
    }

    @Test
    fun `getNumberOfReservedSpots returns the number of reserverd spots`() {
        val trip = tripRepository.save(Trip(fromCity = FROM_CITY, toCity = TO_CITY, capacity = CAPACITY))
        val reservation = reservationRepository.save(createReservation(trip))
        reservationRepository.save(createReservation(trip))
        reservationRepository.save(createReservation(trip))

        val numberOfReservedSpots =
            reservationRepository.getNumberOfReservedSpots(requireNotNull(trip.id), reservation.id)

        assertThat(numberOfReservedSpots).isEqualTo(2)
    }
}
