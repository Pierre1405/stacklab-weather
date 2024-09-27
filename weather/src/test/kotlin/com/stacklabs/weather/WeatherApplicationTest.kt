package com.stacklabs.weather

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.fail

@SpringBootTest
class WeatherApplicationTest {

	@Test
	fun contextLoads() {
	}	@Test
	fun failedTest() {
		fail("Test failed");
	}

}
