package com.modula.coreprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EntityScan({
        "com.modula.common.domain",
        "modula.com.core_builder.domain"
})
@SpringBootApplication
public class CoreProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreProcessorApplication.class, args);
    }

}
