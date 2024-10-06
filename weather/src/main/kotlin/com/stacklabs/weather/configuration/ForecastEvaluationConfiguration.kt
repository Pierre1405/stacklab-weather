package com.stacklabs.weather.configuration

import com.stacklabs.weather.service.evaluation.LinearValueEvaluation
import com.stacklabs.weather.service.evaluation.OptimalValueEvaluation
import com.stacklabs.weather.service.evaluation.ValueEvaluation
import com.stacklabs.weather.service.evaluation.ValueEvaluation.Companion.ValueEvaluationProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding


@ConfigurationProperties(prefix = "evaluation")
data class ForecastEvaluationConfiguration @ConstructorBinding constructor(
    val pressure: ValueEvaluationConfiguration,
    val temperature: ValueEvaluationConfiguration
)

data class ValueEvaluationConfiguration(
    val weight: Double,
    val optimalValue: Double,
    val worstValue: Double,
    val valueEvaluation: ValueEvaluationEnum
) {

    fun toValueEvaluation(): ValueEvaluation =
        valueEvaluation.create(ValueEvaluationProperties(weight, optimalValue, worstValue))
}

enum class ValueEvaluationEnum(val create: (ValueEvaluationProperties) -> ValueEvaluation) {
    @Suppress("unused")
    LINEAR({ config -> LinearValueEvaluation(config) }),
    @Suppress("unused")
    OPTIMAL({ config -> OptimalValueEvaluation(config) })
}