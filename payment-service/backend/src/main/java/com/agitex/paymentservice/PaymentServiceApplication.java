package com.agitex.paymentservice;

import com.agitex.paymentservice.domain.Account;
import com.agitex.paymentservice.domain.repositories.AccountRepository;
import com.agitex.paymentservice.domain.repositories.ClientRepository;
import com.agitex.paymentservice.domain.repositories.EmployeeRepository;
import com.agitex.paymentservice.domain.repositories.OderRepository;
import com.agitex.paymentservice.domain.repositories.PaymentRepository;
import com.agitex.paymentservice.domain.repositories.PromotionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Repository scanning is restricted on purpose: PromotionRepository and EmployeeRepository
 * are unfinished stubs whose target types aren't mapped JPA entities (no Promotion entity
 * exists at all; Employee isn't @Entity). Scanning them crashes the context at boot, so they
 * are excluded here instead of being left to break every startup.
 */
@Configuration
@SpringBootApplication
@EnableJpaRepositories(
        basePackageClasses = {
                PaymentRepository.class,
                ClientRepository.class,
                AccountRepository.class,
                OderRepository.class,
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { PromotionRepository.class, EmployeeRepository.class }
        )
)
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(AccountRepository accountRepository) {
        return args -> {
            if (accountRepository.count() == 0) {
                accountRepository.save(Account.builder()
                        .user("treasury")
                        .amount(new BigDecimal("50000000"))
                        .createdAt(Instant.now())
                        .description("Compte principal")
                        .location("Dakar")
                        .isActive(true)
                        .build());
            }
        };
    }

}
