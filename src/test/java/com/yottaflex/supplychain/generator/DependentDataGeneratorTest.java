package com.yottaflex.supplychain.generator;

import com.yottaflex.supplychain.dto.generator.DependentDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.DependentDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.DependentDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.DependentDataStatisticsResponse;
import com.yottaflex.supplychain.entity.ForecastDemand;
import com.yottaflex.supplychain.entity.Inventory;
import com.yottaflex.supplychain.entity.LeadTime;
import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.entity.Supplier;
import com.yottaflex.supplychain.entity.SupplierCapacity;
import com.yottaflex.supplychain.entity.enums.LocationType;
import com.yottaflex.supplychain.exception.BusinessValidationException;
import com.yottaflex.supplychain.repository.ForecastDemandRepository;
import com.yottaflex.supplychain.repository.InventoryRepository;
import com.yottaflex.supplychain.repository.LeadTimeRepository;
import com.yottaflex.supplychain.repository.LocationRepository;
import com.yottaflex.supplychain.repository.SKURepository;
import com.yottaflex.supplychain.repository.SupplierCapacityRepository;
import com.yottaflex.supplychain.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DependentDataGeneratorTest {

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
    private com.yottaflex.supplychain.repository.PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private com.yottaflex.supplychain.repository.ProductionPlanRepository productionPlanRepository;

    @InjectMocks
    private SyntheticDataGenerator generator;

    private List<Location> mockLocations;
    private List<Supplier> mockSuppliers;
    private List<SKU> mockSkus;
    private DependentDataGenerationRequest defaultRequest;

    @BeforeEach
    void setUp() {
        mockLocations = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Location loc = new Location("LOC-SYN-00" + i, "Location " + i, LocationType.WAREHOUSE, "City " + i, "USA", BigDecimal.valueOf(50000));
            loc.setId((long) i);
            mockLocations.add(loc);
        }

        mockSuppliers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Supplier sup = new Supplier("SUP-SYN-00" + i, "Supplier " + i, "Contact " + i, "sup" + i + "@example.com", "USA", 4.5, 95.0);
            sup.setId((long) i);
            mockSuppliers.add(sup);
        }

        mockSkus = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            SKU sku = new SKU("SKU-SYN-00" + i, "SKU Name " + i, "Electronics", BigDecimal.valueOf(20.0), BigDecimal.valueOf(40.0), 50, 20, 7);
            sku.setId((long) i);
            mockSkus.add(sku);
        }

        defaultRequest = new DependentDataGenerationRequest(5, 8, 4, 4, 20261022L);
    }

    private void mockMasterData() {
        when(locationRepository.findAll()).thenReturn(mockLocations);
        when(supplierRepository.findAll()).thenReturn(mockSuppliers);
        when(skuRepository.findAll()).thenReturn(mockSkus);
    }

    @Nested
    @DisplayName("Dependent Data Preview Tests")
    class PreviewTests {

        @Test
        @DisplayName("Should generate in-memory preview with exact counts without database writes")
        void testPreviewCountsAndZeroPersistence() {
            mockMasterData();

            DependentDataPreviewResponse preview = generator.previewDependentData(defaultRequest);

            assertThat(preview).isNotNull();
            assertThat(preview.isPersisted()).isFalse();
            assertThat(preview.getSeed()).isEqualTo(20261022L);
            assertThat(preview.getInventoryCount()).isEqualTo(5);
            assertThat(preview.getForecastDemandCount()).isEqualTo(8);
            assertThat(preview.getSupplierCapacityCount()).isEqualTo(4);
            assertThat(preview.getLeadTimeCount()).isEqualTo(4);
            assertThat(preview.getTotalRecords()).isEqualTo(21);

            assertThat(preview.getPreviewInventories()).hasSize(5);
            assertThat(preview.getPreviewForecastDemands()).hasSize(8);
            assertThat(preview.getPreviewSupplierCapacities()).hasSize(4);
            assertThat(preview.getPreviewLeadTimes()).hasSize(4);

            // Verify no repository save calls
            verify(inventoryRepository, never()).save(any());
            verify(inventoryRepository, never()).saveAll(anyList());
            verify(forecastDemandRepository, never()).save(any());
            verify(forecastDemandRepository, never()).saveAll(anyList());
            verify(supplierCapacityRepository, never()).save(any());
            verify(supplierCapacityRepository, never()).saveAll(anyList());
            verify(leadTimeRepository, never()).save(any());
            verify(leadTimeRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Should produce deterministic preview values for identical seed")
        void testPreviewDeterminism() {
            mockMasterData();

            DependentDataGenerationRequest req1 = new DependentDataGenerationRequest(3, 3, 3, 3, 88888L);
            DependentDataGenerationRequest req2 = new DependentDataGenerationRequest(3, 3, 3, 3, 88888L);

            DependentDataPreviewResponse run1 = generator.previewDependentData(req1);
            DependentDataPreviewResponse run2 = generator.previewDependentData(req2);

            assertThat(run1.getPreviewInventories().get(0).quantityOnHand())
                    .isEqualByComparingTo(run2.getPreviewInventories().get(0).quantityOnHand());
            assertThat(run1.getPreviewForecastDemands().get(0).forecastQuantity())
                    .isEqualByComparingTo(run2.getPreviewForecastDemands().get(0).forecastQuantity());
            assertThat(run1.getPreviewSupplierCapacities().get(0).dailyCapacity())
                    .isEqualByComparingTo(run2.getPreviewSupplierCapacities().get(0).dailyCapacity());
            assertThat(run1.getPreviewLeadTimes().get(0).standardDays())
                    .isEqualTo(run2.getPreviewLeadTimes().get(0).standardDays());
        }

        @Test
        @DisplayName("Should validate domain constraints across all 4 dependent entity types")
        void testDomainConstraints() {
            mockMasterData();

            DependentDataPreviewResponse preview = generator.previewDependentData(defaultRequest);

            // Inventory constraints: reserved <= onHand, available = onHand - reserved
            for (var inv : preview.getPreviewInventories()) {
                assertThat(inv.quantityOnHand()).isGreaterThan(BigDecimal.ZERO);
                assertThat(inv.quantityReserved()).isLessThanOrEqualTo(inv.quantityOnHand());
                assertThat(inv.quantityAvailable()).isEqualByComparingTo(inv.quantityOnHand().subtract(inv.quantityReserved()));
                assertThat(inv.status()).isNotNull();
            }

            // Forecast Demand constraints: positive quantity, weekly date, confidence [0.0, 1.0]
            for (var fd : preview.getPreviewForecastDemands()) {
                assertThat(fd.forecastQuantity()).isGreaterThan(BigDecimal.ZERO);
                assertThat(fd.confidenceScore()).isBetween(0.0, 1.0);
                assertThat(fd.forecastDate()).isNotNull();
                assertThat(fd.periodType()).isNotNull();
            }

            // Supplier Capacity constraints: positive daily & monthly capacity, available = monthly - allocated
            for (var sc : preview.getPreviewSupplierCapacities()) {
                assertThat(sc.dailyCapacity()).isGreaterThan(BigDecimal.ZERO);
                assertThat(sc.monthlyCapacity()).isGreaterThan(BigDecimal.ZERO);
                assertThat(sc.allocatedCapacity()).isLessThanOrEqualTo(sc.monthlyCapacity());
                assertThat(sc.availableCapacity()).isEqualByComparingTo(sc.monthlyCapacity().subtract(sc.allocatedCapacity()));
                assertThat(sc.effectiveEndDate()).isAfter(sc.effectiveStartDate());
            }

            // Lead Time constraints: standardDays >= 1, expeditedDays >= 1, expeditedDays <= standardDays
            for (var lt : preview.getPreviewLeadTimes()) {
                assertThat(lt.standardDays()).isGreaterThanOrEqualTo(1);
                assertThat(lt.expeditedDays()).isGreaterThanOrEqualTo(1);
                assertThat(lt.expeditedDays()).isLessThanOrEqualTo(lt.standardDays());
                assertThat(lt.minimumOrderQuantity()).isGreaterThanOrEqualTo(1);
                assertThat(lt.transportMode()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Dependent Data Persistence Tests")
    class PersistenceTests {

        @Test
        @DisplayName("Should persist all 4 entity types via repository saveAll and return summary")
        void testGenerateDependentDataPersistence() {
            mockMasterData();

            when(inventoryRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(forecastDemandRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(supplierCapacityRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(leadTimeRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            DependentDataGenerationResult result = generator.generateDependentData(defaultRequest);

            assertThat(result).isNotNull();
            assertThat(result.getInventoriesSaved()).isEqualTo(5);
            assertThat(result.getForecastDemandsSaved()).isEqualTo(8);
            assertThat(result.getSupplierCapacitiesSaved()).isEqualTo(4);
            assertThat(result.getLeadTimesSaved()).isEqualTo(4);
            assertThat(result.getTotalRecordsSaved()).isEqualTo(21);
            assertThat(result.getExecutionDurationMs()).isGreaterThanOrEqualTo(0);

            assertThat(result.getSavedInventorySummaries()).hasSize(5);
            assertThat(result.getSavedForecastSummaries()).hasSize(8);
            assertThat(result.getSavedCapacitySummaries()).hasSize(4);
            assertThat(result.getSavedLeadTimeSummaries()).hasSize(4);

            verify(inventoryRepository, times(1)).saveAll(anyList());
            verify(forecastDemandRepository, times(1)).saveAll(anyList());
            verify(supplierCapacityRepository, times(1)).saveAll(anyList());
            verify(leadTimeRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("Should prevent duplicates by avoiding already persisted combinations")
        void testDuplicateAvoidance() {
            mockMasterData();

            // Simulate that SKU 1 @ Location 1 already has an inventory record in DB
            lenient().when(inventoryRepository.existsBySkuIdAndLocationId(anyLong(), anyLong()))
                    .thenAnswer(inv -> {
                        Long skuId = inv.getArgument(0);
                        Long locId = inv.getArgument(1);
                        return Long.valueOf(1L).equals(skuId) && Long.valueOf(1L).equals(locId);
                    });

            when(inventoryRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(forecastDemandRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(supplierCapacityRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(leadTimeRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            DependentDataGenerationRequest req = new DependentDataGenerationRequest(1, 0, 0, 0, 42L);
            DependentDataGenerationResult result = generator.generateDependentData(req);

            assertThat(result.getInventoriesSaved()).isEqualTo(1);
            // Verify it did not use (1, 1)
            String summary = result.getSavedInventorySummaries().get(0);
            assertThat(summary).doesNotContain("SKU: SKU-SYN-001 @ LOC: LOC-SYN-001 |");
        }
    }

    @Nested
    @DisplayName("Validation & Missing Master Data Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw BusinessValidationException when master data is missing")
        void testMissingMasterDataThrowsException() {
            when(locationRepository.findAll()).thenReturn(List.of()); // Empty locations

            assertThatThrownBy(() -> generator.previewDependentData(defaultRequest))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Parent master data is missing");
        }

        @Test
        @DisplayName("Should throw BusinessValidationException for negative counts")
        void testNegativeCounts() {
            DependentDataGenerationRequest req = new DependentDataGenerationRequest(-5, 10, 10, 10, 1L);
            assertThatThrownBy(() -> generator.previewDependentData(req))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Inventory count must be between 0 and 500");
        }

        @Test
        @DisplayName("Should throw BusinessValidationException for excessive counts")
        void testExcessiveCounts() {
            DependentDataGenerationRequest req = new DependentDataGenerationRequest(10, 2500, 10, 10, 1L);
            assertThatThrownBy(() -> generator.generateDependentData(req))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Forecast demand count must be between 0 and 2000");
        }

        @Test
        @DisplayName("Should handle zero counts gracefully without error")
        void testZeroCounts() {
            mockMasterData();

            DependentDataGenerationRequest req = new DependentDataGenerationRequest(0, 0, 0, 0, 1L);
            DependentDataPreviewResponse preview = generator.previewDependentData(req);

            assertThat(preview.getTotalRecords()).isEqualTo(0);
            assertThat(preview.getPreviewInventories()).isEmpty();
            assertThat(preview.getPreviewForecastDemands()).isEmpty();
            assertThat(preview.getPreviewSupplierCapacities()).isEmpty();
            assertThat(preview.getPreviewLeadTimes()).isEmpty();
        }

        @Test
        @DisplayName("Should query and return dependent data statistics accurately")
        void testStatisticsQuery() {
            when(inventoryRepository.count()).thenReturn(20L);
            when(forecastDemandRepository.count()).thenReturn(50L);
            when(supplierCapacityRepository.count()).thenReturn(15L);
            when(leadTimeRepository.count()).thenReturn(15L);

            when(inventoryRepository.countBySyntheticSku(anyString())).thenReturn(20L);
            when(forecastDemandRepository.countBySyntheticSku(anyString())).thenReturn(50L);
            when(supplierCapacityRepository.countBySyntheticSupplier(anyString())).thenReturn(15L);
            when(leadTimeRepository.countBySyntheticSupplier(anyString())).thenReturn(15L);

            DependentDataStatisticsResponse stats = generator.getDependentDataStatistics();

            assertThat(stats.getTotalInventories()).isEqualTo(20L);
            assertThat(stats.getTotalForecastDemands()).isEqualTo(50L);
            assertThat(stats.getTotalSupplierCapacities()).isEqualTo(15L);
            assertThat(stats.getTotalLeadTimes()).isEqualTo(15L);
            assertThat(stats.getTotalDependentRecords()).isEqualTo(100L);
            assertThat(stats.getTotalSyntheticDependentRecords()).isEqualTo(100L);
        }
    }
}
