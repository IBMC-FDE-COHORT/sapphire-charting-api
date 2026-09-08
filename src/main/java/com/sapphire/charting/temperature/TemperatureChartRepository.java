package com.sapphire.charting.temperature;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

/**
 * Read-only repository for body temperature aggregation queries against the
 * {@code health_metrics} TimescaleDB hypertable.
 *
 * <p>All queries operate on {@code value_celsius} to ensure consistent
 * aggregation regardless of the unit used at ingestion time (D-003).
 */
public interface TemperatureChartRepository extends Repository<Object, UUID> {

    /**
     * Returns aggregated min/max/avg temperature data points grouped by the
     * supplied time bucket for a user over the given range.
     *
     * <p>{@code deviceSource} is optional — pass {@code null} to include all devices.
     *
     * @param userId       the authenticated user's UUID
     * @param rangeStart   inclusive start of the query window (UTC)
     * @param rangeEnd     inclusive end of the query window (UTC)
     * @param bucket       PostgreSQL date_trunc bucket string: {@code 'hour'}, {@code 'day'}
     * @param deviceSource optional device identifier filter; {@code null} means all devices
     * @return ordered list of aggregated data points, earliest first
     */
    @Query(
        value = """
            SELECT
              DATE_TRUNC(:bucket, recorded_at)          AS period_start,
              MIN(value_celsius)                        AS min_celsius,
              MAX(value_celsius)                        AS max_celsius,
              ROUND(AVG(value_celsius)::numeric, 2)     AS avg_celsius,
              COUNT(*)                                  AS record_count
            FROM health_metrics
            WHERE user_id      = :userId
              AND metric_type  = 'body_temperature'
              AND recorded_at  BETWEEN :rangeStart AND :rangeEnd
              AND (:deviceSource IS NULL OR device_source = :deviceSource)
            GROUP BY DATE_TRUNC(:bucket, recorded_at)
            ORDER BY period_start ASC
            """,
        nativeQuery = true
    )
    List<TemperatureDataPoint> findAggregatedByUserAndRange(
        @Param("userId") UUID userId,
        @Param("rangeStart") Instant rangeStart,
        @Param("rangeEnd") Instant rangeEnd,
        @Param("bucket") String bucket,
        @Param("deviceSource") @Nullable String deviceSource
    );
}
