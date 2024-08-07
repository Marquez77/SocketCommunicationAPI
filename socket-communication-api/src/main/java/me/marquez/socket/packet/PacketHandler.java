package me.marquez.socket.packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketHandler {

    /**
     * @return Identifiers for classifying packets.
     */
    String[] identifiers() default {};
    int priority() default 0;

}
