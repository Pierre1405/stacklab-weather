package com.stacklabs.weather.it

class RunMockServer

fun main(args: Array<String>) {
    MockServerConfig.startServer()
    MockServerConfig.getServerConfig().registerCurrentSuccess("tokyo")
}