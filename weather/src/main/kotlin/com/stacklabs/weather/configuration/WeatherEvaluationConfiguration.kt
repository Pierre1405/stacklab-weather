package com.stacklabs.weather.configuration

import com.stacklabs.weather.service.WeatherEvaluation
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding


@ConfigurationProperties(prefix = "evaluation")
data class WeatherEvaluationConfiguration @ConstructorBinding constructor(
    val pressure: EvaluationProperties,
    val temperature: EvaluationProperties
)

data class EvaluationProperties(
    val optimalValue: Double,
    val worstMinValue: Double,
    val worstMaxValue: Double,
    val weight: Double,
    val evaluationFunction: WeatherEvaluation.EvaluationFunctions
)