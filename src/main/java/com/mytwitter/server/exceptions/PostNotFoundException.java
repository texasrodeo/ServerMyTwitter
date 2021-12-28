package com.mytwitter.server.exceptions;

public class PostNotFoundException extends Exception {
    public PostNotFoundException(String message)
    {
        super(message);
    }
}
