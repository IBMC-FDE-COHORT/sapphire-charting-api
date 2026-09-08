package com.sapphire.charting.temperature;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import com.sapphire.charting.common.ChartRange;

/**
 * Business logic for body temperature charting.
 *
 * <p>Converts a {@link ChartRange} to a concrete time window and SQL bucket,
 * delegates the aggregation query to {@link TemperatureChartRepository},
 * and assembles the final {@link TemperatureChartResponse}.
 */
@Service
public class TemperatureChartService {

    private static final String METRIC_TYPE = "body_temperature";
    private static final String UNIT = "CELSIUS";
    private static final String EMPTY_MESSAGE =
            "No body temperature records found for the requested range";

    private static final int DAY_HOURS = 24;
    private static final int WEEK_DAYS = 7;
    private static final int MONTH_DAYS = 30;

    private final TemperatureChartRepository repository;

    /**
     * Construct the service with its required repository dependency.
     *
     * @param repository the body temperature aggregation repository
     */
    public TemperatureChartService(final TemperatureChartRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieve aggregated body temperature chart data for the given request.
     *
     * <p>Returns a response with an empty {@code dataPoints} list and a
     * descriptive message when no records exist for the period — never null.
     *
     * @param request validated chart request containing userId, range, and optional deviceSource
     * @return assembled chart response
     */
    public TemperatureChartResponse getTemperatureChart(final TemperatureChartRequest request) {
        final Instant now = Instant.now();
        final RangeWindow window = toWindow(request.range(), now);

        final List<TemperatureDataPoint> dataPoints = repository.findAggregatedByUserAndRange(
                request.userId(),
                window.start(),
                window.end(),
                window.bucket(),
                request.deviceSource()
        );

        final String message = dataPoints.isEmpty() ? EMPTY_MESSAGE : null;

        return new TemperatureChartResponse(
                request.userId(),
                METRIC_TYPE,
                request.range(),
                UNIT,
                dataPoints,
                message
        );
    }

    private static RangeWindow toWindow(final ChartRange range, final Instant now) {
        return switch (range) {
            case DAY   -> new RangeWindow(now.minus(DAY_HOURS, ChronoUnit.HOURS), now, "hour");
            case WEEK  -> new RangeWindow(now.minus(WEEK_DAYS, ChronoUnit.DAYS),  now, "day");
            case MONTH -> new RangeWindow(now.minus(MONTH_DAYS, ChronoUnit.DAYS), now, "day");
        };
    }

    private record RangeWindow(Instant start, Instant end, String bucket) {}
}
