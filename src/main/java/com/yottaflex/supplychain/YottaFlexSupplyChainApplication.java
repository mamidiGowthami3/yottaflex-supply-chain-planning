package com.yottaflex.supplychain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Main entry point for the YottaFlex Supply Chain Planning
 * and Synthetic Data Generation System.
 */
@SpringBootApplication
public class YottaFlexSupplyChainApplication {

    private static final Logger log = LoggerFactory.getLogger(YottaFlexSupplyChainApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(YottaFlexSupplyChainApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("====================================================================");
        log.info("  YottaFlex Supply Chain Planning System is READY and OPERATIONAL   ");
        log.info("  Health API available at: /api/v1/health                          ");
        log.info("  Actuator Health endpoint: /actuator/health                       ");
        log.info("====================================================================");
    }
}

