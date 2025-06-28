package com.soliner.digitalcard.core.types.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Geçersiz giriş verileri (örneğin, zaten kullanımda olan bir kullanıcı adı veya e-posta) durumunda fırlatılan özel istisna.
 * HTTP 409 CONFLICT (Çakışma) yanıt kodu döndürür.
 */
@Getter
@Setter
@ResponseStatus(HttpStatus.CONFLICT) // HTTP 409 CONFLICT durum kodu döndürür
public class InvalidInputException extends BaseException {

    private final String fieldName; // Hatanın ilgili olduğu alan adı
    private final String fieldValue; // Hatanın ilgili olduğu alan değeri (opsiyonel)

    // Genel mesaj constructor'ı
    public InvalidInputException(String message) {
        super(message);
        this.fieldName = null;
        this.fieldValue = null;
    }

    // Alan adı ve mesaj constructor'ı
    public InvalidInputException(String fieldName, String message) {
        super(String.format("%s: %s", fieldName, message));
        this.fieldName = fieldName;
        this.fieldValue = null;
    }

    // Alan adı, alan değeri ve mesaj constructor'ı
    public InvalidInputException(String fieldName, String fieldValue, String message) {
        super(String.format("%s '%s' %s", fieldName, fieldValue, message));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
