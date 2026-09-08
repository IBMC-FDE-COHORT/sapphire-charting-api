package com.sapphire.charting.temperature;

import java.util.UUID;
import org.springframework.lang.Nullable;
import com.sapphire.charting.common.ChartRange;

/**
 * Encapsulates the validated inputs for a body temperature chart query.
 *
 * Resolved from the HTTP path variable and query parameters inside
 * {@link TemperatureChartController} before being passed to the service layer.
 */
public record TemperatureChartRequest(
    UUID userId,
    ChartRange range,
    @Nullable String deviceSource
) {}
