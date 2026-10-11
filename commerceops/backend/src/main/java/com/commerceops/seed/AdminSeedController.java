package com.commerceops.seed;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Local lab tooling to reset/regenerate data on demand, without restarting
 * the app. Protected by Spring Security's default authenticated rule for
 * /api/admin/** (see SecurityConfig) - not for exposure outside the lab.
 */
@RestController
@RequestMapping("/api/admin/seed")
public class AdminSeedController {

    private final DataGeneratorService generatorService;

    public AdminSeedController(DataGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @PostMapping
    public Map<String, String> seed(
            @RequestParam(defaultValue = "SMALL") String profile,
            @RequestParam(defaultValue = "true") boolean reset) {
        SeedProfile seedProfile = SeedProfile.valueOf(profile.toUpperCase());
        if (reset) {
            generatorService.reset();
        }
        generatorService.generate(seedProfile);
        return Map.of("status", "done", "profile", seedProfile.name());
    }
}
