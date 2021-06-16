package com.github.trohovsky.tripreservation.controller

import com.github.trohovsky.tripreservation.api.CreateReservationDto
import com.github.trohovsky.tripreservation.api.ReservationDto
import com.github.trohovsky.tripreservation.api.UpdateReservationDto
import com.github.trohovsky.tripreservation.service.ReservationService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/trip/{tripId}/reservation")
class ReservationController(private val reservationService: ReservationService) {

    @GetMapping
    fun getAll(@PathVariable tripId: Int): List<ReservationDto> {
        return reservationService.getAll(tripId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable tripId: Int, @RequestBody createReservationDto: CreateReservationDto): ReservationDto {
        return reservationService.create(tripId, createReservationDto)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable tripId: Int,
        @PathVariable id: Int,
        @RequestBody updateReservationDto: UpdateReservationDto
    ): ReservationDto {
        return reservationService.update(tripId, id, updateReservationDto)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable tripId: Int, @PathVariable id: Int) {
        reservationService.delete(tripId, id)
    }
}
