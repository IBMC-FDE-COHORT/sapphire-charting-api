package com.sapphire.charting.temperature;

import java.util.List;
import java.util.UUID;
import org.springframework.lang.Nullable;
import com.sapphire.charting.common.ChartRange;

/**
 * Response body for {@code GET /charts/{userId}/body-temperature}.
 *
 * {@code dataPoints} is empty (never null) when no records exist for the requested
 * range. {@code message} is populated only when the data set is empty.
 */
public record TemperatureChartResponse(
    UUID userId,
    String metricType,
    ChartRange range,
    String unit,
    List<TemperatureDataPoint> dataPoints,
    @Nullable String message
) {}
