package com.stacklabs.weather.it

class TestMockServer {

}

fun main(args: Array<String>) {
    MockServerConfig.startServer()
    MockServerConfig.getServerConfig().registerCurrentSuccess("tokyo")
}