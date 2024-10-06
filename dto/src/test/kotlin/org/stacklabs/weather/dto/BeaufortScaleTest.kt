package org.stacklabs.weather.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class BeaufortScaleTest {

    @Test
    fun test_getFromMeterPerSeconds_correct_mapping() {
        assertEquals(BeaufortScale.CALM, BeaufortScale.getFromMeterPerSeconds(0.0))
        assertEquals(BeaufortScale.LIGHT_AIR, BeaufortScale.getFromMeterPerSeconds(1.5))
        assertEquals(BeaufortScale.LIGHT_BREEZE, BeaufortScale.getFromMeterPerSeconds(3.0))
        assertEquals(BeaufortScale.GENTLE_BREEZE, BeaufortScale.getFromMeterPerSeconds(5.0))
        assertEquals(BeaufortScale.MODERATE_BREEZE, BeaufortScale.getFromMeterPerSeconds(8.0))
        assertEquals(BeaufortScale.FRESH_BREEZE, BeaufortScale.getFromMeterPerSeconds(10.0))
        assertEquals(BeaufortScale.STRONG_BREEZE, BeaufortScale.getFromMeterPerSeconds(13.0))
        assertEquals(BeaufortScale.HIGH_WIND, BeaufortScale.getFromMeterPerSeconds(16.0))
        assertEquals(BeaufortScale.GALE, BeaufortScale.getFromMeterPerSeconds(20.0))
        assertEquals(BeaufortScale.STRONG_GALE, BeaufortScale.getFromMeterPerSeconds(24.0))
        assertEquals(BeaufortScale.STORM, BeaufortScale.getFromMeterPerSeconds(28.0))
        assertEquals(BeaufortScale.VIOLENT_STORM, BeaufortScale.getFromMeterPerSeconds(32.0))
        assertEquals(BeaufortScale.HURRICANE_FORCE, BeaufortScale.getFromMeterPerSeconds(35.0))
    }

    @Test
    fun test_getFromMeterPerSeconds_negative_speed() {
        assertThrows(RuntimeException::class.java) {
            BeaufortScale.getFromMeterPerSeconds(-1.0)
        }
    }
}