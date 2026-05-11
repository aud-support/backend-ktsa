package com.ktsa.foosball.exception;

public class AccountInactiveException extends RuntimeException{

    public AccountInactiveException(String message) {
        super(message);
    }
}
