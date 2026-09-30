package com.polaris;

import com.polaris.shared.config.ZonaHoraria;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PolarisApplication {

    public static void main(String[] args) {
        ZonaHoraria.aplicar(ZonaHoraria.POR_DEFECTO);
        SpringApplication.run(PolarisApplication.class, args);
    }
}
