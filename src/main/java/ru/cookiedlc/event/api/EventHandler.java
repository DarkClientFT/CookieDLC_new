package ru.cookiedlc.event.api;

import ru.cookiedlc.event.api.types.Priority;
import ru.cookiedlc.module.impl.movement.air.EventPriorite;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    byte value() default Priority.MEDIUM;

    int priority() default EventPriorite.MEDIUM;
}