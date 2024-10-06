package com.stacklabs.weather.service.evaluation

import com.stacklabs.weather.service.evaluation.ValueEvaluation.Companion.ValueEvaluationProperties

/**
 * Represents a class for a linear evaluation based on the optimal value and worst value.
 * i.e. for a pressure, the optimal value can be 1100hPa and the worst value can be 900 hPa.
 * evaluation(1100hPa) = weight
 * evaluation(900hPa) = 0
 * evaluation(1200hPa) > weight
 * evaluation(800hPa) < 0
 *
 * @param properties EvaluationProperties object containing weight, optimal value, and worst value.
 */
class LinearValueEvaluation(private val properties: ValueEvaluationProperties): ValueEvaluation(properties) {
    override fun evaluateRatio(value: Double): Double {
        val distance = properties.worstValue - properties.optimalValue
        return (properties.worstValue - value) / distance
    }
}