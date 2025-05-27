package com.soliner.digitalcard.core.types.exceptions;

/**
 * Geçersiz giriş verisi olduğunda fırlatılan özel istisna sınıfı.
 * BaseException'dan türemektedir.
 */
public class InvalidInputException extends BaseException { // BaseException'dan türetildi

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
