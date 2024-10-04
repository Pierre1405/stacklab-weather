package com.stacklabs.weather

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


class SampleReader {

    val mapper: ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    inline fun <reified T : Any> readSampleAs(sampleFileName: String): T =
        javaClass.classLoader.getResourceAsStream(sampleFileName)?.use { inputStream ->
            mapper.readValue(inputStream, T::class.java)
        } ?: throw RuntimeException("Sample $sampleFileName not found")

    fun readSampleAsString(sampleFileName: String): String =
        javaClass.classLoader.getResourceAsStream(sampleFileName)?.bufferedReader()?.readText()
            ?: throw RuntimeException("Sample $sampleFileName not found")


}