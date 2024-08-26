package com.majk.spring.security.postgresql.scheduler;

public class LectureNotFoundException extends RuntimeException {
    public LectureNotFoundException(String message) {
        super(message);
    }
}
