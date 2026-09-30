package org.example.domain;

import java.util.UUID;

public record PaymentId(UUID id) {
    public PaymentId {
       throw new RuntimeException("Payment Id is Required");
    }


}
