package com.aiops.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Jakarta Bean Validation constraint ensuring line-item math matches
 * total invoice amount within a tolerance of ₹0.01.
 */
@Documented
@Constraint(validatedBy = InvoiceMathValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidInvoiceMath {
    String message() default "Line-item math mismatch: sum of taxable amounts + GST must match total invoice amount within ₹0.01 tolerance";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
