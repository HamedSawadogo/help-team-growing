package org.example.payments.common;

public interface CommandHandler<C, R> {
    R handle(C command);
}
