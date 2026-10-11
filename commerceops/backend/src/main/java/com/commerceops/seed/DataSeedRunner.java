package com.commerceops.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeedRunner.class);

    private final DataGeneratorService generatorService;
    private final JdbcTemplate jdbc;
    private final boolean seedEnabled;
    private final SeedProfile profile;

    public DataSeedRunner(DataGeneratorService generatorService, JdbcTemplate jdbc,
                           @Value("${commerceops.seed.enabled:false}") boolean seedEnabled,
                           @Value("${commerceops.seed.profile:SMALL}") String profile) {
        this.generatorService = generatorService;
        this.jdbc = jdbc;
        this.seedEnabled = seedEnabled;
        this.profile = SeedProfile.valueOf(profile.toUpperCase());
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("commerceops.seed.enabled=false - skipping automatic data generation");
            return;
        }
        Long existingCustomers = jdbc.queryForObject("SELECT count(*) FROM customers", Long.class);
        if (existingCustomers != null && existingCustomers > 0) {
            log.info("Customers table already has {} rows - skipping automatic data generation", existingCustomers);
            return;
        }
        generatorService.generate(profile);
    }
}
