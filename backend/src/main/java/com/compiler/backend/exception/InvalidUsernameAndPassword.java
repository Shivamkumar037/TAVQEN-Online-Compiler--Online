package com.compiler.backend.exception;

public class InvalidUsernameAndPassword extends RuntimeException {

    public InvalidUsernameAndPassword(String message) {
        super(message);
    }
}