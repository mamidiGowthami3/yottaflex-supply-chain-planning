package com.yottaflex.supplychain.service;

import com.yottaflex.supplychain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DatasetExportServiceTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private SKURepository skuRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private ForecastDemandRepository forecastDemandRepository;
    @Mock
    private SupplierCapacityRepository supplierCapacityRepository;
    @Mock
    private LeadTimeRepository leadTimeRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private ProductionPlanRepository productionPlanRepository;
    @Mock
    private ShortageRepository shortageRepository;
    @Mock
    private MitigationOptionRepository mitigationOptionRepository;

    private DatasetExportService datasetExportService;

    @BeforeEach
    void setUp() {
        datasetExportService = new DatasetExportService(
                locationRepository,
                supplierRepository,
                skuRepository,
                inventoryRepository,
                forecastDemandRepository,
                supplierCapacityRepository,
                leadTimeRepository,
                purchaseOrderRepository,
                productionPlanRepository,
                shortageRepository,
                mitigationOptionRepository
        );
    }

    @Test
    @DisplayName("Should generate complete challenge dataset with all 11 entities, 3000 SKUs, 50 locations, 500 suppliers, 52 weeks forecast, and 10,000 POs")
    void testGenerateChallengeDataset(@TempDir Path tempDir) throws IOException {
        long seed = 20261022L;

        DatasetExportService.ExportManifest manifest = datasetExportService.generateAndExportChallengeDataset(seed, tempDir);

        // Verify Manifest Scale & Counts
        assertThat(manifest).isNotNull();
        assertThat(manifest.seed()).isEqualTo(seed);
        assertThat(manifest.locationCount()).isEqualTo(50);
        assertThat(manifest.supplierCount()).isEqualTo(500);
        assertThat(manifest.skuCount()).isEqualTo(3000);
        assertThat(manifest.forecastDemandWeeks()).isEqualTo(52);
        assertThat(manifest.forecastDemandCount()).isEqualTo(5200); // 52 weeks * 100 sample SKUs
        assertThat(manifest.purchaseOrderCount()).isEqualTo(10000);
        assertThat(manifest.inventoryCount()).isEqualTo(3000);
        assertThat(manifest.supplierCapacityCount()).isEqualTo(1500);
        assertThat(manifest.leadTimeCount()).isEqualTo(1500);
        assertThat(manifest.productionPlanCount()).isEqualTo(500);
        assertThat(manifest.shortageCount()).isEqualTo(100);
        assertThat(manifest.mitigationOptionCount()).isEqualTo(200);

        // Verify CSV Directory and Files
        Path csvDir = tempDir.resolve("csv");
        assertThat(Files.isDirectory(csvDir)).isTrue();
        assertThat(Files.exists(csvDir.resolve("locations.csv"))).isTrue();
        assertThat(Files.exists(csvDir.resolve("suppliers.csv"))).isTrue();
        assertThat(Files.exists(csvDir.resolve("skus.csv"))).isTrue();
        assertThat(Files.exists(csvDir.resolve("inventories.csv"))).isTrue();
        assertThat(Files.exists(csvDir.resolve("forecast_demands.csv"))).isTrue();
        assertThat(Files.exists(csvDir.resolve("supplier_capacities.csv"))).isTrue();
        assertThat(Files.exists(csvDir.resolve("lead_times.csv"))).isTrue();
        assertThat(Files.exists(csvDir.resolve("purchase_orders.csv"))).isTrue();
        assertThat(Files.exists(csvDir.resolve("production_plans.csv"))).isTrue();
        assertThat(Files.exists(csvDir.resolve("shortages.csv"))).isTrue();
        assertThat(Files.exists(csvDir.resolve("mitigation_options.csv"))).isTrue();

        // Verify JSON Directory and Files
        Path jsonDir = tempDir.resolve("json");
        assertThat(Files.isDirectory(jsonDir)).isTrue();
        assertThat(Files.exists(jsonDir.resolve("locations.json"))).isTrue();
        assertThat(Files.exists(jsonDir.resolve("suppliers.json"))).isTrue();
        assertThat(Files.exists(jsonDir.resolve("skus.json"))).isTrue();
        assertThat(Files.exists(jsonDir.resolve("inventories.json"))).isTrue();
        assertThat(Files.exists(jsonDir.resolve("forecast_demands.json"))).isTrue();
        assertThat(Files.exists(jsonDir.resolve("supplier_capacities.json"))).isTrue();
        assertThat(Files.exists(jsonDir.resolve("lead_times.json"))).isTrue();
        assertThat(Files.exists(jsonDir.resolve("purchase_orders.json"))).isTrue();
        assertThat(Files.exists(jsonDir.resolve("production_plans.json"))).isTrue();
        assertThat(Files.exists(jsonDir.resolve("shortages.json"))).isTrue();
        assertThat(Files.exists(jsonDir.resolve("mitigation_options.json"))).isTrue();

        // Verify SQL Directory and Inserts
        Path sqlDir = tempDir.resolve("sql");
        assertThat(Files.isDirectory(sqlDir)).isTrue();
        Path sqlFile = sqlDir.resolve("challenge_dataset_insert.sql");
        assertThat(Files.exists(sqlFile)).isTrue();
        assertThat(Files.size(sqlFile)).isGreaterThan(1000L);

        // Verify Manifest JSON file and Checksums
        Path manifestFile = tempDir.resolve("manifest.json");
        assertThat(Files.exists(manifestFile)).isTrue();
        assertThat(manifest.fileChecksums()).isNotEmpty();
        assertThat(manifest.fileChecksums().get("csv/locations.csv")).hasSize(64); // SHA-256 length

        // Verify getManifest
        Optional<DatasetExportService.ExportManifest> readBack = datasetExportService.getManifest(tempDir);
        assertThat(readBack).isPresent();
        assertThat(readBack.get().skuCount()).isEqualTo(3000);

        // Verify resolveExportFile
        Path resolved = datasetExportService.resolveExportFile(tempDir, "csv", "locations.csv");
        assertThat(Files.exists(resolved)).isTrue();

        // Verify path traversal prevention
        assertThatThrownBy(() -> datasetExportService.resolveExportFile(tempDir, "csv", "../secrets.txt"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

