package com.stacklabs.weather.service

import com.stacklabs.weather.configuration.EvaluationProperties
import kotlin.math.pow

object WeatherEvaluation {

    /**
     * Evaluates the given value based on the provided EvaluationProperties.
     * When value == EvaluationProperties.optimalValue, returns EvaluationProperties.weight.
     * When value == EvaluationProperties.worstMinValue or value == EvaluationProperties.worstMaxValue, returns 0.
     * The transition between optimal and worst values are define by EvaluationProperties.evaluationFunction
     * (i.e. LINEAR or SQUARE).
     *
     * @param value The value to be evaluated.
     * @param properties The EvaluationProperties containing optimal, worst min, and worst max values, weight, and evaluation function.
     * @return The evaluated value after applying the specified evaluation function.
     */
    fun evaluate(value: Double, properties: EvaluationProperties): Double {
        val maxDistance = if(value < properties.optimalValue) {
            properties.optimalValue - properties.worstMinValue
        } else {
            properties.worstMaxValue - properties.optimalValue
        }
        val ratio =  properties.evaluationFunction.evaluate(
            Math.abs(value - properties.optimalValue) / maxDistance
        )
        return properties.weight - ratio * properties.weight
    }


    enum class EvaluationFunctions(val evaluate: (Double) -> Double) {
        LINEAR({value -> value}),
        SQUARE({value -> value.pow(2) })
    }
}