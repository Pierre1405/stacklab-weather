package com.stacklabs.weather.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.stacklabs.dto.HelloWorld
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(HelloWorldController::class)
class HelloWorldControllerTests(
    @Autowired
    var mvc: MockMvc
) {

    val mapper = jacksonObjectMapper()


    @Test
    fun helloWorldEndpointShouldReturnHelloWorld() {
        val result = mvc.perform(get("/helloworld"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        val response = result.response.getContentAsString(Charsets.UTF_8)
        assertEquals(HelloWorld("World"), mapper.readValue(response, HelloWorld::class.java))
    }


    @Test
    fun helloNameEndpointShouldReturnHelloName() {
        val result = mvc.perform(get("/helloworld/John"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        val response = result.response.getContentAsString(Charsets.UTF_8)
        assertEquals(HelloWorld("John"), mapper.readValue(response, HelloWorld::class.java))
    }
}