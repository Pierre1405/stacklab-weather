package com.stacklabs.weather.cache

import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException


sealed class Result<T> {
    data class Success<T>(val result: ResponseEntity<T>) : Result<T>()
    data class RestClientError<T>(val error: HttpClientErrorException) : Result<T>()
}