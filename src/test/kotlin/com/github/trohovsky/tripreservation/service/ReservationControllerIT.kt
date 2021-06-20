package com.github.trohovsky.tripreservation.service

import com.github.trohovsky.tripreservation.api.*
import com.github.trohovsky.tripreservation.entity.Reservation
import com.github.trohovsky.tripreservation.entity.Trip
import com.github.trohovsky.tripreservation.repository.ReservationRepository
import com.github.trohovsky.tripreservation.repository.TripRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.*
import org.springframework.test.context.ActiveProfiles

private const val UPDATED_SPOTS = 2
private const val NON_EXISTING_ENTITY_ID = 42
private val TRIP = Trip(fromCity = FROM_CITY, toCity = TO_CITY, capacity = CAPACITY)
private val NON_EXISTING_TRIP = TRIP.copy(id = NON_EXISTING_ENTITY_ID)

private const val NOT_ENOUGH_FREE_SPOTS_MESSAGE = "Not enough free spots, only 1 spots are available."
private const val TRIP_NOT_FOUND_MESSAGE = "Trip with id $NON_EXISTING_ENTITY_ID could not be found."
private const val RESERVATION_NOT_FOUND_MESSAGE = "Reservation with id $NON_EXISTING_ENTITY_ID could not be found."

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
internal class ReservationControllerIT {

    @LocalServerPort
    private val port = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var tripRepository: TripRepository

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @AfterEach
    internal fun tearDown() {
        tripRepository.deleteAll()
    }

    @Test
    fun `get trips gets all reservations and returns 200 status`() {
        val trip = tripRepository.save(TRIP)
        val reservation = reservationRepository.save(createReservation(trip))

        val responseEntity = restTemplate.getForEntity(getReservationUrl(trip), Array<ReservationDto>::class.java)

        assertThat(responseEntity.statusCode).isEqualTo(OK);
        assertThat(responseEntity.body).isEqualTo(
            arrayOf(
                ReservationDto(
                    requireNotNull(reservation.id),
                    reservation.tripId,
                    reservation.username,
                    reservation.spots
                )
            )
        )
    }

    @Test
    fun `get returns 404 status if the trip does not exist`() {

        val responseEntity =
            restTemplate.getForEntity(getReservationUrl(NON_EXISTING_TRIP), Array<ReservationDto>::class.java)

        assertThat(responseEntity.statusCode).isEqualTo(OK);
        assertThat(responseEntity.body).isEqualTo(ErrorDto(TRIP_NOT_FOUND_MESSAGE))
    }

    @Test
    fun `post creates a reservation and returns 201 status`() {
        val trip = tripRepository.save(TRIP)

        val responseEntity = restTemplate.postForEntity(
            getReservationUrl(trip),
            CreateReservationDto(USERNAME, SPOTS),
            ReservationDto::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(CREATED)
        assertThat(responseEntity.body)
            .usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(ReservationDto(1, requireNotNull(trip.id), USERNAME, SPOTS))
    }

    @Test
    fun `post returns 404 status if the trip does not exist`() {

        val responseEntity = restTemplate.postForEntity(
            getReservationUrl(NON_EXISTING_TRIP),
            CreateReservationDto(USERNAME, SPOTS),
            ReservationDto::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(NOT_FOUND)
        assertThat(responseEntity.body).isEqualTo(ErrorDto(TRIP_NOT_FOUND_MESSAGE))
    }

    @Test
    fun `post returns 400 status if the number of reserved spots exceeds the trip's capacity`() {
        val trip = tripRepository.save(TRIP)

        val responseEntity = restTemplate.postForEntity(
            getReservationUrl(trip),
            CreateReservationDto(USERNAME, UPDATED_SPOTS),
            ErrorDto::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(BAD_REQUEST)
        assertThat(responseEntity.body).isEqualTo(ErrorDto(NOT_ENOUGH_FREE_SPOTS_MESSAGE))
    }

    @Test
    fun `put updates the reservation and returns 200 status`() {
        val trip = tripRepository.save(TRIP.copy(capacity = 2))
        val reservation = reservationRepository.save(createReservation(trip))

        val responseEntity = restTemplate.exchange(
            getReservationUrl(reservation),
            HttpMethod.PUT,
            HttpEntity(UpdateReservationDto(UPDATED_SPOTS)),
            ReservationDto::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(OK)
        assertThat(responseEntity.body)
            .isEqualTo(ReservationDto(requireNotNull(reservation.id), requireNotNull(trip.id), USERNAME, UPDATED_SPOTS))
    }

    @Test
    fun `put returns 404 status if the trip does not exist`() {
        val reservation = createReservation(NON_EXISTING_TRIP).copy(id = NON_EXISTING_ENTITY_ID)

        val responseEntity = restTemplate.exchange(
            getReservationUrl(reservation),
            HttpMethod.PUT,
            HttpEntity(UpdateReservationDto(UPDATED_SPOTS)),
            ErrorDto::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(NOT_FOUND)
        assertThat(responseEntity.body).isEqualTo(ErrorDto(TRIP_NOT_FOUND_MESSAGE))
    }

    @Test
    fun `put returns 404 status if the reservation does not exist`() {
        val trip = tripRepository.save(TRIP)
        val reservation = createReservation(trip).copy(id = NON_EXISTING_ENTITY_ID)

        val responseEntity = restTemplate.exchange(
            getReservationUrl(reservation),
            HttpMethod.PUT,
            HttpEntity(UpdateReservationDto(UPDATED_SPOTS)),
            ErrorDto::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(NOT_FOUND)
        assertThat(responseEntity.body).isEqualTo(ErrorDto(RESERVATION_NOT_FOUND_MESSAGE))
    }

    @Test
    fun `put returns 400 status if the number of reserved spots exceeds the trip's capacity`() {
        val trip = tripRepository.save(TRIP)
        val reservation = reservationRepository.save(createReservation(trip))

        val responseEntity = restTemplate.exchange(
            getReservationUrl(reservation),
            HttpMethod.PUT,
            HttpEntity(UpdateReservationDto(UPDATED_SPOTS)),
            ErrorDto::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(BAD_REQUEST)
        assertThat(responseEntity.body).isEqualTo(ErrorDto(NOT_ENOUGH_FREE_SPOTS_MESSAGE))
    }

    @Test
    fun `delete deletes the reservation and returns 204 status`() {
        val trip = tripRepository.save(TRIP)
        val reservation = reservationRepository.save(createReservation(trip))

        val responseEntity = restTemplate.exchange(
            getReservationUrl(reservation),
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            String::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(NO_CONTENT)
        assertThat(responseEntity.body).isNull()
        assertThat(reservationRepository.findByIdOrNull(reservation.id)).isNull()
    }

    @Test
    fun `delete returns 404 status if the trip does not exist`() {
        val reservation = createReservation(NON_EXISTING_TRIP).copy(id = NON_EXISTING_ENTITY_ID)

        val responseEntity = restTemplate.exchange(
            getReservationUrl(reservation),
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            ErrorDto::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(NOT_FOUND)
        assertThat(responseEntity.body).isEqualTo(ErrorDto(TRIP_NOT_FOUND_MESSAGE))
    }

    @Test
    fun `delete returns 404 status if the reservation does not exist`() {
        val trip = tripRepository.save(TRIP)
        val reservation = createReservation(trip).copy(id = NON_EXISTING_ENTITY_ID)

        val responseEntity = restTemplate.exchange(
            getReservationUrl(reservation),
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            ErrorDto::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(NOT_FOUND)
        assertThat(responseEntity.body).isEqualTo(ErrorDto(RESERVATION_NOT_FOUND_MESSAGE))
    }

    private fun createReservation(trip: Trip): Reservation =
        Reservation(tripId = requireNotNull(trip.id), username = USERNAME, spots = SPOTS)

    private fun getReservationUrl(trip: Trip): String =
        "http://localhost:${port}/trip/${trip.id}/reservation"

    private fun getReservationUrl(reservation: Reservation): String =
        "http://localhost:${port}/trip/${reservation.tripId}/reservation/${reservation.id}"
}
