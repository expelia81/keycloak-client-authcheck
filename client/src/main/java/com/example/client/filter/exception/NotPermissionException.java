package com.example.client.filter.exception;

import org.springframework.beans.factory.annotation.Value;

public class NotPermissionException extends RuntimeException{

    public NotPermissionException() {
        super("권한이 없습니다.");
    } // To do 동적으로


}
