package com.aiops.agent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares an operations tool bean exposed to LLMs.
 * Compatible with Spring AI tool registration semantics.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tool {
    /**
     * Tool unique name.
     */
    String name() default "";

    /**
     * Description of what this tool performs and when the LLM should invoke it.
     */
    String description() default "";

    /**
     * Whether the tool execution response should be returned directly to the user.
     */
    boolean returnDirect() default false;
}
