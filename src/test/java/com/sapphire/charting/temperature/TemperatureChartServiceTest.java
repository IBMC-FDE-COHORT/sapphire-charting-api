package com.sapphire.charting.temperature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sapphire.charting.common.ChartRange;

/**
 * Unit tests for {@link TemperatureChartService}.
 * All repository calls are mocked; no Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
class TemperatureChartServiceTest {

    @Mock
    private TemperatureChartRepository repository;

    @InjectMocks
    private TemperatureChartService service;

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void returnsDataPoints_whenRepositoryHasResults() {
        final List<TemperatureDataPoint> points = List.of(
                new TemperatureDataPoint(LocalDate.of(2026, 9, 1), 36.4, 37.8, 37.1, 4)
        );
        when(repository.findAggregatedByUserAndRange(eq(USER_ID), any(), any(), any(), isNull()))
                .thenReturn(points);

        final TemperatureChartResponse response =
                service.getTemperatureChart(new TemperatureChartRequest(USER_ID, ChartRange.WEEK, null));

        assertThat(response.dataPoints()).hasSize(1);
        assertThat(response.message()).isNull();
        assertThat(response.unit()).isEqualTo("CELSIUS");
    }

    @Test
    void returnsEmptyDataPointsWithMessage_whenNoRecordsExist() {
        when(repository.findAggregatedByUserAndRange(any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        final TemperatureChartResponse response =
                service.getTemperatureChart(new TemperatureChartRequest(USER_ID, ChartRange.MONTH, null));

        assertThat(response.dataPoints()).isEmpty();
        assertThat(response.message()).isNotNull().isNotBlank();
    }

    @Test
    void dayRange_passesHourBucketToRepository() {
        when(repository.findAggregatedByUserAndRange(any(), any(), any(), eq("hour"), any()))
                .thenReturn(Collections.emptyList());
        service.getTemperatureChart(new TemperatureChartRequest(USER_ID, ChartRange.DAY, null));
    }

    @Test
    void weekRange_passesDayBucketToRepository() {
        when(repository.findAggregatedByUserAndRange(any(), any(), any(), eq("day"), any()))
                .thenReturn(Collections.emptyList());
        service.getTemperatureChart(new TemperatureChartRequest(USER_ID, ChartRange.WEEK, null));
    }

    @Test
    void monthRange_passesDayBucketToRepository() {
        when(repository.findAggregatedByUserAndRange(any(), any(), any(), eq("day"), any()))
                .thenReturn(Collections.emptyList());
        service.getTemperatureChart(new TemperatureChartRequest(USER_ID, ChartRange.MONTH, null));
    }
}
