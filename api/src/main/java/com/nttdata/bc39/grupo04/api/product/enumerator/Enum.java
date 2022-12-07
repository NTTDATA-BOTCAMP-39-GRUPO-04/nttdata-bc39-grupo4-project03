package com.nttdata.bc39.grupo04.api.product.enumerator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;
 
@Documented
@Constraint(validatedBy = {EnumTypeProductValueValidator.class})
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Enum
{
    public abstract String message() default "Valor Inválido Tipo de Producto. Sólo se permite CUENTA BANCARIA, CREDITO";
         
    public abstract Class<?>[] groups() default {};
  
    public abstract Class<? extends Payload>[] payload() default {};
     
    public abstract Class<? extends java.lang.Enum<?>> enumClass();
     
    public abstract boolean ignoreCase() default false;
}