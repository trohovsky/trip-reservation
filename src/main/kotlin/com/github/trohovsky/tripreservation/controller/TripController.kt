package com.github.trohovsky.tripreservation.controller

import com.github.trohovsky.tripreservation.api.CreateTripDto
import com.github.trohovsky.tripreservation.api.TripDto
import com.github.trohovsky.tripreservation.service.TripService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/trip")
class TripController(private val tripService: TripService) {

    @GetMapping
    fun getAll(): List<TripDto> = tripService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Int): TripDto = tripService.getById(id)

    @PostMapping
    @ResponseStatus(CREATED)
    fun create(@RequestBody createTripDto: CreateTripDto): TripDto =
        tripService.create(createTripDto)

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    fun delete(@PathVariable id: Int): Unit = tripService.delete(id)
}

