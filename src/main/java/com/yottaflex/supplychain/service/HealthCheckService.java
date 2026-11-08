package com.yottaflex.supplychain.service;

import com.yottaflex.supplychain.dto.HealthCheckResponse;
import com.yottaflex.supplychain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service responsible for comprehensive system health diagnostics,
 * database connectivity status, and entity table statistics.
 */
@Service
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    @Value("${spring.application.name:yottaflex-supply-chain}")
    private String applicationName;

    private final DataSource dataSource;
    private final SKURepository skuRepository;
    private final LocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;
    private final ForecastDemandRepository forecastDemandRepository;
    private final SupplierCapacityRepository supplierCapacityRepository;
    private final LeadTimeRepository leadTimeRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ShortageRepository shortageRepository;
    private final MitigationOptionRepository mitigationOptionRepository;

    public HealthCheckService(
            DataSource dataSource,
            SKURepository skuRepository,
            LocationRepository locationRepository,
            SupplierRepository supplierRepository,
            InventoryRepository inventoryRepository,
            ForecastDemandRepository forecastDemandRepository,
            SupplierCapacityRepository supplierCapacityRepository,
            LeadTimeRepository leadTimeRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            ProductionPlanRepository productionPlanRepository,
            ShortageRepository shortageRepository,
            MitigationOptionRepository mitigationOptionRepository) {
        this.dataSource = dataSource;
        this.skuRepository = skuRepository;
        this.locationRepository = locationRepository;
        this.supplierRepository = supplierRepository;
        this.inventoryRepository = inventoryRepository;
        this.forecastDemandRepository = forecastDemandRepository;
        this.supplierCapacityRepository = supplierCapacityRepository;
        this.leadTimeRepository = leadTimeRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productionPlanRepository = productionPlanRepository;
        this.shortageRepository = shortageRepository;
        this.mitigationOptionRepository = mitigationOptionRepository;
    }

    public HealthCheckResponse getSystemHealth() {
        HealthCheckResponse response = new HealthCheckResponse();
        response.setApplicationName(applicationName);
        response.setVersion("1.0.0-SNAPSHOT");

        // Database status check
        String dbProduct = "UNKNOWN";
        boolean dbConnected = false;
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            dbProduct = metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion();
            dbConnected = !connection.isClosed();
            response.setDatabaseStatus("CONNECTED");
            response.setDatabaseProduct(dbProduct);
        } catch (Exception ex) {
            log.error("Database connection check failed: {}", ex.getMessage());
            response.setDatabaseStatus("DOWN: " + ex.getMessage());
            response.setDatabaseProduct(dbProduct);
        }

        // Check Entity Table counts
        Map<String, Long> tableCounts = new LinkedHashMap<>();
        if (dbConnected) {
            try {
                tableCounts.put("skus", skuRepository.count());
                tableCounts.put("locations", locationRepository.count());
                tableCounts.put("suppliers", supplierRepository.count());
                tableCounts.put("inventories", inventoryRepository.count());
                tableCounts.put("forecast_demands", forecastDemandRepository.count());
                tableCounts.put("supplier_capacities", supplierCapacityRepository.count());
                tableCounts.put("lead_times", leadTimeRepository.count());
                tableCounts.put("purchase_orders", purchaseOrderRepository.count());
                tableCounts.put("production_plans", productionPlanRepository.count());
                tableCounts.put("shortages", shortageRepository.count());
                tableCounts.put("mitigation_options", mitigationOptionRepository.count());
            } catch (Exception ex) {
                log.error("Failed to query entity table counts: {}", ex.getMessage());
            }
        }
        response.setEntityTableCounts(tableCounts);

        // Overall status
        response.setStatus(dbConnected ? "UP" : "DEGRADED");

        // Environment & runtime info
        Map<String, Object> envInfo = new LinkedHashMap<>();
        Runtime runtime = Runtime.getRuntime();
        envInfo.put("javaVersion", System.getProperty("java.version"));
        envInfo.put("javaVendor", System.getProperty("java.vendor"));
        envInfo.put("osName", System.getProperty("os.name"));
        envInfo.put("availableProcessors", runtime.availableProcessors());
        envInfo.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        envInfo.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        envInfo.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
        response.setEnvironmentInfo(envInfo);

        return response;
    }
}

