package com.stacklabs.weather.controller

import com.stacklabs.dto.HelloWorld
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/helloworld")
class HelloWorldController {

    @GetMapping(path = ["/{name}"])
    fun helloName(@PathVariable name: String): HelloWorld {
        return HelloWorld(name)
    }

    @GetMapping(path = [""])
    fun helloWorld(): HelloWorld {
        return HelloWorld("World")
    }
}