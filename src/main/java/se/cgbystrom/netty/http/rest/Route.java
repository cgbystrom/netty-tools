package se.cgbystrom.netty.http.rest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
    String path();
    String[] methods() default {"GET", "POST", "PUT", "DELETE"};
}
