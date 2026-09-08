package com.sapphire.charting.temperature;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.sapphire.charting.common.ChartRange;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for body temperature trend chart queries.
 *
 * <p>Mirrors the existing BloodPressureChartController pattern.
 * Authentication is enforced by Spring Security; this controller only
 * performs authorisation (userId must match the JWT subject claim).
 */
@Slf4j
@RestController
@RequestMapping("/charts/{userId}/body-temperature")
public class TemperatureChartController {

    private final TemperatureChartService service;

    /**
     * Construct the controller with its required service dependency.
     *
     * @param service the body temperature chart service
     */
    public TemperatureChartController(final TemperatureChartService service) {
        this.service = service;
    }

    /**
     * Return aggregated body temperature data for a user over the requested range.
     *
     * @param userId       path variable — the user whose data is requested
     * @param range        query parameter — aggregation granularity (DAY | WEEK | MONTH)
     * @param deviceSource optional query parameter — filter to a specific device
     * @param auth         injected by Spring Security; subject claim used for authorisation
     * @return 200 OK with {@link TemperatureChartResponse}; 403 on userId mismatch
     */
    @GetMapping
    public ResponseEntity<TemperatureChartResponse> getTemperatureChart(
            @PathVariable final UUID userId,
            @RequestParam final ChartRange range,
            @RequestParam(required = false) @Nullable final String deviceSource,
            final Authentication auth
    ) {
        if (!userId.toString().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied to the requested user's data");
        }

        log.info("temperature_chart_request userId={} range={} deviceSource={}",
                userId, range, deviceSource);

        final TemperatureChartRequest request =
                new TemperatureChartRequest(userId, range, deviceSource);
        final TemperatureChartResponse response = service.getTemperatureChart(request);

        return ResponseEntity.ok(response);
    }
}
