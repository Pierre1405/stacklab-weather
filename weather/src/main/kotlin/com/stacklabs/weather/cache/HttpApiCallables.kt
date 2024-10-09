package com.stacklabs.weather.cache

import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException

interface HttpApiCallables<I,O> {
    fun executeRequest (cacheKey: String): ResponseEntity<I>
    fun onHttpError (cacheKey: String, exception: HttpClientErrorException): O
    fun onSuccess (cacheKey: String, response: ResponseEntity<I>): O
}