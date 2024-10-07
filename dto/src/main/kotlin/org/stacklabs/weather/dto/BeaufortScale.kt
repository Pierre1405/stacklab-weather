package org.stacklabs.weather.dto

import com.fasterxml.jackson.annotation.JsonValue
import io.swagger.v3.oas.annotations.media.Schema


@Schema(description = "Beaufort wind scale")
enum class BeaufortScale(@JsonValue val value: Int, val rangeKmPerHour: IntRange, val description: String) {
    CALM(0, 0..1, "Calm"),
    LIGHT_AIR(1, 2..5, "Light Air"),
    LIGHT_BREEZE(2, 6..11, "Light Breeze"),
    GENTLE_BREEZE(3, 12..19, "Gentle Breeze"),
    MODERATE_BREEZE(4, 20..28, "Moderate Breeze"),
    FRESH_BREEZE(5, 29..38, "Fresh Breeze"),
    STRONG_BREEZE(6, 39..49, "Strong Breeze"),
    HIGH_WIND(7, 50..61, "High Wind"),
    GALE(8, 62..74, "Gale"),
    STRONG_GALE(9, 75..88, "Strong Gale"),
    STORM(10, 89..102, "Storm"),
    VIOLENT_STORM(11, 103..117, "Violent Storm"),
    HURRICANE_FORCE(12, 118..Int.MAX_VALUE, "Hurricane Force");

    companion object {
        fun getFromMeterPerSeconds(speedMeterPerSecond: Double): BeaufortScale =
            entries
                .find { value ->
                    value.rangeKmPerHour
                        .contains(
                            meterSecondToKmPerHour(speedMeterPerSecond).toInt()
                        )
                }
                ?: throw RuntimeException("""Cannot find Beaufort scale for speed $speedMeterPerSecond""")


        private const val METERS_PER_KM: Double = 1000.0
        private const val SECOND_PER_HOURS: Double = 3600.0
        fun meterSecondToKmPerHour(speedMeterPerSecond: Double): Double =
            speedMeterPerSecond * SECOND_PER_HOURS / METERS_PER_KM

    }
}