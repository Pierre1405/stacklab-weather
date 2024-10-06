package com.stacklabs.weather.service.evaluation

import com.stacklabs.weather.service.evaluation.ValueEvaluation.Companion.ValueEvaluationProperties
import kotlin.math.pow


/**
 * Represents a class for evaluation based on the optimal value.
 * All value before and after the optimal value are considered not ideal.
 * worst value is used to calculate the distance between the value and the optimal value.
 * i.e. for a temperature, the optimal value can be 20°C and the worst value can be 0°C.
 * in this case distance (20°C - 0°C) = 20°C
 * So both temperature optimal + distance = 40°C and optimal - distance = 0°C are considered like worst
 * evaluation(20°C) = weight
 * evaluation(0°C) = 0
 * evaluation(40°C) = 0
 * evaluation(-10°C) < 0
 * evaluation(50°C) < 0
 *
 * @param properties EvaluationProperties object containing weight, optimal value, and worst value.
 */
class OptimalValueEvaluation(private val properties: ValueEvaluationProperties): ValueEvaluation(properties) {
    override fun evaluateRatio(value: Double): Double {
        val distance = Math.abs(properties.worstValue - properties.optimalValue)
        return 1.0 - ((properties.optimalValue - value) / distance).pow(2)
    }
}