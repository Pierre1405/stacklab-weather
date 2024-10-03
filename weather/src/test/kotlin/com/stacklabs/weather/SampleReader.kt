package com.stacklabs.weather

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


class SampleReader {

    val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    inline fun <reified T : Any> readSample(sampleFileName: String): T {
        val sampleAsStream = javaClass.classLoader.getResourceAsStream(sampleFileName)
        val sample = mapper.readValue(sampleAsStream, T::class.java)
        return sample
    }
}