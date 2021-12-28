package com.mytwitter.server.exceptions;

public class DuplicatedUserException extends Exception {
    public DuplicatedUserException(String message)
    {
        super(message);
    }
}
