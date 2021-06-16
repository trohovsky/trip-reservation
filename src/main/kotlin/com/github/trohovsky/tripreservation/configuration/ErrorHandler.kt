package com.github.trohovsky.tripreservation.configuration

import com.github.trohovsky.tripreservation.api.ErrorDto
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.persistence.EntityNotFoundException

@ControllerAdvice
class ErrorHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    protected fun handleBadRequest(e: Exception): ResponseEntity<ErrorDto> {
        return ResponseEntity.badRequest().body(ErrorDto(e.message))
    }

    @ExceptionHandler(EntityNotFoundException::class)
    protected fun handleNotFound(e: Exception): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(NOT_FOUND).body(ErrorDto(e.message))
    }
}
