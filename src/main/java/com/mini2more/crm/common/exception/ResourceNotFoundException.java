/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entity, Long id) {
        super(entity + " not found with id: " + id);
    }

    public ResourceNotFoundException(String entity, String field, String value) {
        super(entity + " not found with " + field + ": " + value);
    }
}
