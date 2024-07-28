package com.xmartin.brand_service.exceptions;

public class FailedToLoadCarsException extends Exception{
    public FailedToLoadCarsException(String message){
        super(message);
    }
}
