package com.example.client.filter.exception;

public class BadTokenException extends RuntimeException{
    public BadTokenException() {
        super("잘못된 토큰 형식입니다. 올바르지 않은 요청일 가능성이 있습니다."); // To do 동적으로
    }
}
