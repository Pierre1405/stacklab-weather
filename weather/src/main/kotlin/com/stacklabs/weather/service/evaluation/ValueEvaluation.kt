package com.stacklabs.weather.service.evaluation

abstract class ValueEvaluation(private val properties: ValueEvaluationProperties) {

    init {
        require(properties.optimalValue != properties.worstValue) { "Worst value cannot be equal to optimal value" }
    }

    fun evaluate(value: Double): Double {
        return properties.weight * evaluateRatio(value)
    }

    protected abstract fun evaluateRatio(value: Double): Double

    companion object {
        data class ValueEvaluationProperties(
            val weight: Double,
            val optimalValue: Double,
            val worstValue: Double
        )
    }
}