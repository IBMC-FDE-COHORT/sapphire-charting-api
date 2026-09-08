package com.sapphire.charting.temperature;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.sapphire.charting.common.ChartRange;

/**
 * Web-layer tests for {@link TemperatureChartController}.
 * The service is mocked; only HTTP binding, authorisation, and response shape are verified.
 */
@WebMvcTest(TemperatureChartController.class)
class TemperatureChartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemperatureChartService service;

    private static final String USER_ID = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

    @Test
    @WithMockUser(username = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    void returns200WithDataPoints_whenDataExists() throws Exception {
        final List<TemperatureDataPoint> points = List.of(
                new TemperatureDataPoint(LocalDate.of(2026, 9, 1), 36.4, 37.8, 37.1, 4)
        );
        when(service.getTemperatureChart(any()))
                .thenReturn(new TemperatureChartResponse(
                        UUID.fromString(USER_ID), "body_temperature",
                        ChartRange.WEEK, "CELSIUS", points, null));

        mockMvc.perform(get("/charts/{userId}/body-temperature", USER_ID).param("range", "WEEK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricType").value("body_temperature"))
                .andExpect(jsonPath("$.dataPoints[0].minCelsius").value(36.4));
    }

    @Test
    @WithMockUser(username = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    void returns200WithEmptyList_whenNoData() throws Exception {
        when(service.getTemperatureChart(any()))
                .thenReturn(new TemperatureChartResponse(
                        UUID.fromString(USER_ID), "body_temperature",
                        ChartRange.WEEK, "CELSIUS", Collections.emptyList(),
                        "No body temperature records found for the requested range"));

        mockMvc.perform(get("/charts/{userId}/body-temperature", USER_ID).param("range", "WEEK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPoints").isArray())
                .andExpect(jsonPath("$.dataPoints").isEmpty())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    void returns400_whenRangeParameterIsInvalid() throws Exception {
        mockMvc.perform(get("/charts/{userId}/body-temperature", USER_ID).param("range", "QUARTER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "different-user-id")
    void returns403_whenUserIdDoesNotMatchJwtSubject() throws Exception {
        mockMvc.perform(get("/charts/{userId}/body-temperature", USER_ID).param("range", "WEEK"))
                .andExpect(status().isForbidden());
    }

    @Test
    void returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/charts/{userId}/body-temperature", USER_ID).param("range", "WEEK"))
                .andExpect(status().isUnauthorized());
    }
}
