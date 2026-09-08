package com.sapphire.charting.temperature;

import java.time.LocalDate;

/**
 * A single aggregated body temperature data point covering one time-bucket.
 *
 * All temperature values are in Celsius. {@code recordCount} indicates how many
 * raw readings were included in the aggregation window.
 */
public record TemperatureDataPoint(
    LocalDate periodStart,
    double minCelsius,
    double maxCelsius,
    double avgCelsius,
    int recordCount
) {}
