package com.soliner.digitalcard.core.types.exceptions;

public class ResourceNotFoundException extends BaseException {
    private static final long serialVersionUID = 1L; // Bu satırı eklemiştik, yine de kalsın.
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        // Bu satırı güncelledik:
        super(String.format("%s, %s : '%s' ile bulunamadı", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }
//
    public Object getFieldValue() {
        return fieldValue;
    }
}