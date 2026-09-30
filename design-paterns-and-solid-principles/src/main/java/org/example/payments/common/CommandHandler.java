package org.example.common;

public interface CommandHandler<C, R> {
    R handle(C command);
}
