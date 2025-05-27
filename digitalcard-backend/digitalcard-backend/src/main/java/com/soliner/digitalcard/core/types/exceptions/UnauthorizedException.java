package com.soliner.digitalcard.core.types.exceptions;

/**
 * Kullanıcının belirli bir kaynağa veya işleme erişim yetkisi olmadığında fırlatılan özel istisna.
 * Bu istisna, web katmanında 401 Unauthorized veya 403 Forbidden yanıtına dönüştürülebilir.
 */
public class UnauthorizedException extends BaseException {
	 public UnauthorizedException(String message) {
	        super(message);
	    }

	    public UnauthorizedException(String message, Throwable cause) {
	        super(message, cause);
	    }
	}