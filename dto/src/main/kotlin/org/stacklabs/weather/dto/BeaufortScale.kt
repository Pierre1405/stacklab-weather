package org.stacklabs.weather.dto

enum class BeaufortScale(val range: IntRange, val description: String) {
    CALM(0..1, "Calm"),
    LIGHT_AIR(2..5, "Light Air"),
    LIGHT_BREEZE(6..11, "Light Breeze"),
    GENTLE_BREEZE(12..19, "Gentle Breeze"),
    MODERATE_BREEZE(20..28, "Moderate Breeze"),
    FRESH_BREEZE(29..38, "Fresh Breeze"),
    STRONG_BREEZE(39..49, "Strong Breeze"),
    HIGH_WIND(50..61, "High Wind"),
    GALE(62..74, "Gale"),
    STRONG_GALE(75..88, "Strong Gale"),
    STORM(89..102, "Storm"),
    VIOLENT_STORM(103..117, "Violent Storm"),
    HURRICANE_FORCE(118..Int.MAX_VALUE, "Hurricane Force");

    companion object {
        fun fromMeterPerSeconds(speed: Double): BeaufortScale = entries
            .find { value -> value.range.contains((speed / 1000).toInt()) }
            ?: throw RuntimeException("""Cannot find Beaufort scale for speed $speed""")
    }
}