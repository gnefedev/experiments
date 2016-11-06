package com.gnefedev.mmt;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by gerakln on 06.11.16.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({
        ApiClientRegistrar.class,
        MockConfig.class
})
public @interface EnableMmtClient {
    String[] basePackages();
}
