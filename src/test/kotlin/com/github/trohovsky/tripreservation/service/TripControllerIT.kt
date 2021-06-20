package com.github.trohovsky.tripreservation.service

import com.github.trohovsky.tripreservation.api.CreateTripDto
import com.github.trohovsky.tripreservation.api.ErrorDto
import com.github.trohovsky.tripreservation.api.TripDto
import com.github.trohovsky.tripreservation.entity.Trip
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

private val TRIP = Trip(fromCity = FROM_CITY, toCity = TO_CITY, capacity = CAPACITY)
private const val NON_EXISTING_TRIP_ID = 42

private const val TRIP_NOT_FOUND_MESSAGE = "Trip with id 42 could not be found."

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
internal class TripControllerIT {

    @LocalServerPort
    private val port = 0

    @Autowired
    private lateinit var tripRepository: TripRepository

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @AfterEach
    internal fun tearDown() {
        tripRepository.deleteAll()
    }

    @Test
    fun `get trips gets all trips and returns 200 status`() {
        val trip = tripRepository.save(TRIP)

        val responseEntity = restTemplate.getForEntity(getTripUrl(), Array<TripDto>::class.java)

        assertThat(responseEntity.statusCode).isEqualTo(OK);
        assertThat(responseEntity.body).isEqualTo(
            arrayOf(
                TripDto(
                    requireNotNull(trip.id),
                    FROM_CITY,
                    TO_CITY,
                    CAPACITY
                )
            )
        )
    }

    @Test
    fun `get by id gets the trip and returns 200 status`() {
        val trip = tripRepository.save(TRIP)

        val responseEntity = restTemplate.getForEntity(getTripUrl(trip.id), TripDto::class.java)

        assertThat(responseEntity.statusCode).isEqualTo(OK);
        assertThat(responseEntity.body).isEqualTo(TripDto(requireNotNull(trip.id), FROM_CITY, TO_CITY, CAPACITY))
    }

    @Test
    fun `get by id returns 404 status if the trip does not exist`() {
        val responseEntity = restTemplate.getForEntity(getTripUrl(NON_EXISTING_TRIP_ID), ErrorDto::class.java)

        assertThat(responseEntity.statusCode).isEqualTo(NOT_FOUND)
        assertThat(requireNotNull(responseEntity.body).message).isEqualTo(TRIP_NOT_FOUND_MESSAGE)
    }

    @Test
    fun `post creates a trip and returns 201 status`() {
        val responseEntity =
            restTemplate.postForEntity(getTripUrl(), CreateTripDto(FROM_CITY, TO_CITY, CAPACITY), TripDto::class.java)

        assertThat(responseEntity.statusCode).isEqualTo(CREATED);
        assertThat(responseEntity.body)
            .usingRecursiveComparison().ignoringFields("id").isEqualTo(TripDto(1, FROM_CITY, TO_CITY, CAPACITY))
    }

    @Test
    fun `delete deletes the trip and returns 204 status`() {
        val trip = tripRepository.save(TRIP)

        val responseEntity = restTemplate.exchange(
            getTripUrl(trip.id),
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            String::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(NO_CONTENT)
        assertThat(responseEntity.body).isNull()
        assertThat(tripRepository.findByIdOrNull(trip.id)).isNull()
    }

    @Test
    fun `delete returns 404 status if the trip does not exist`() {

        val responseEntity = restTemplate.exchange(
            getTripUrl(NON_EXISTING_TRIP_ID),
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            ErrorDto::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(NOT_FOUND)
        assertThat(responseEntity.body).isEqualTo(ErrorDto(TRIP_NOT_FOUND_MESSAGE))
    }

    private fun getTripUrl(tripId: Int? = null): String =
        "http://localhost:${port}/trip/" + (tripId ?: "")
}
