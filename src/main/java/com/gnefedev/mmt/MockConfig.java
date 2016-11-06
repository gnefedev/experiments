package com.gnefedev.mmt;

import com.gnefedev.api.MmtImplementation;
import org.springframework.context.annotation.Bean;

/**
 * Created by gerakln on 06.11.16.
 */
public class MockConfig {
    @Bean
    public MmtImplementation mmtImplementation() {
        return new MmtImplementation();
    }
}
