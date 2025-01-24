package com.tmeras.resellmart.exception;


public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String resourceName, String field, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, field, fieldValue));
    }
}
