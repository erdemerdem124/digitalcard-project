package com.soliner.digitalcard.core.types.exceptions;

public abstract class BaseException extends RuntimeException { // 'abstract' anahtar kelimesi eklendi

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
