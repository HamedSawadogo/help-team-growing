package com.commerceops.customers;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final RestClient restClient = RestClient.create();


    public Object search() {
        Map<Object, Object> dataMap = new HashMap<>();
        Object body = fetchData();
        System.err.println("Data:  " + body);
        dataMap.put("Data", body);
        dataMap.put("Result", "Result");
        return dataMap;
    }

    @CircuitBreaker(name = "backend", fallbackMethod = "defaultData")
    private Object fetchData() {
        return restClient.get()
                .uri("http://localhost:8080/api/v1/frontOfficeController/housesAvailable?propertyId=0a60fcac-21be-48af-aefd-f509047360f6")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class);
    }


    public String defaultData(Exception e) {
        return ".... DATA NON DISponible" + e.getMessage();
    }


    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
    }

    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }
}
