package com.libreria.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class EcommerceLibreriaApplication {

    // Fija la zona horaria del JVM antes de que arranque el contexto de Spring, para que
    // LocalDateTime.now() (created_at de pedidos, cortes de caja, etc.) refleje la hora de
    // México sin importar en que zona horaria corra el contenedor Docker (usualmente UTC).
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Mexico_City"));
    }

    public static void main(String[] args) {
        SpringApplication.run(EcommerceLibreriaApplication.class, args);
    }
}
