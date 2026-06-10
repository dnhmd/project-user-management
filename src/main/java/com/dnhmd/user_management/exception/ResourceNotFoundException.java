package com.dnhmd.user_management.exception;

public class ResourceNotFoundException extends RuntimeException {

    private String resource;
    private String identifier;

    public ResourceNotFoundException(String resource, String identifier) {
        super(resource + " not found: " + identifier);
    }
}
