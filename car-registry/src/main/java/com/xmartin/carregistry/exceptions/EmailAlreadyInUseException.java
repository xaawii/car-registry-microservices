package com.xmartin.carregistry.exceptions;

public class EmailAlreadyInUseException extends Exception{
    public EmailAlreadyInUseException(String message){
        super(message);
    }
}
