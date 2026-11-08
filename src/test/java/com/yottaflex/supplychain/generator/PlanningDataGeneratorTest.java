package com.yottaflex.supplychain.generator;

import com.yottaflex.supplychain.dto.generator.PlanningDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.PlanningDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.PlanningDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.PlanningDataStatisticsResponse;
import com.yottaflex.supplychain.entity.LeadTime;
import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.entity.Supplier;
import com.yottaflex.supplychain.entity.SupplierCapacity;
import com.yottaflex.supplychain.entity.enums.LocationType;
import com.yottaflex.supplychain.entity.enums.ProductionPlanStatus;
import com.yottaflex.supplychain.entity.enums.PurchaseOrderStatus;
import com.yottaflex.supplychain.entity.enums.TransportMode;
import com.yottaflex.supplychain.exception.BusinessValidationException;
import com.yottaflex.supplychain.repository.ForecastDemandRepository;
import com.yottaflex.supplychain.repository.InventoryRepository;
import com.yottaflex.supplychain.repository.LeadTimeRepository;
import com.yottaflex.supplychain.repository.LocationRepository;
import com.yottaflex.supplychain.repository.ProductionPlanRepository;
import com.yottaflex.supplychain.repository.PurchaseOrderRepository;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanningDataGeneratorTest {

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

    @InjectMocks
    private SyntheticDataGenerator generator;

    private List<Location> mockLocations;
    private List<Supplier> mockSuppliers;
    private List<SKU> mockSkus;
    private List<LeadTime> mockLeadTimes;
    private List<SupplierCapacity> mockCapacities;
    private PlanningDataGenerationRequest defaultRequest;

    @BeforeEach
    void setUp() {
        mockLocations = new ArrayList<>();
        Location plant = new Location("LOC-SYN-001", "Austin Assembly Plant", LocationType.PLANT, "Austin", "USA", BigDecimal.valueOf(50000));
        plant.setId(1L);
        mockLocations.add(plant);

        Location warehouse = new Location("LOC-SYN-002", "Dallas Warehouse", LocationType.WAREHOUSE, "Dallas", "USA", BigDecimal.valueOf(100000));
        warehouse.setId(2L);
        mockLocations.add(warehouse);

        mockSuppliers = new ArrayList<>();
        Supplier sup = new Supplier("SUP-SYN-001", "Apex Precision", "John", "apex@example.com", "USA", 4.8, 97.0);
        sup.setId(1L);
        mockSuppliers.add(sup);

        mockSkus = new ArrayList<>();
        SKU sku1 = new SKU("SKU-SYN-001", "Pro Sensor Module", "Electronics", BigDecimal.valueOf(25.00), BigDecimal.valueOf(50.00), 100, 20, 7);
        sku1.setId(1L);
        mockSkus.add(sku1);

        SKU sku2 = new SKU("SKU-SYN-002", "Control Board", "Electronics", BigDecimal.valueOf(45.00), BigDecimal.valueOf(90.00), 200, 40, 14);
        sku2.setId(2L);
        mockSkus.add(sku2);

        mockLeadTimes = new ArrayList<>();
        LeadTime lt = new LeadTime(sup, sku1, warehouse, 10, 5, 10, BigDecimal.valueOf(15.00), TransportMode.ROAD);
        lt.setId(1L);
        mockLeadTimes.add(lt);

        mockCapacities = new ArrayList<>();
        SupplierCapacity cap = new SupplierCapacity(sup, sku1, LocalDate.now(), LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(500.0), BigDecimal.valueOf(12500.0));
        cap.setId(1L);
        cap.setAllocatedCapacity(BigDecimal.valueOf(2500.0));
        cap.updateAvailableCapacity();
        mockCapacities.add(cap);

        defaultRequest = new PlanningDataGenerationRequest(20, 10, 20261022L);
    }

    private void mockAvailableParents() {
        when(locationRepository.findAll()).thenReturn(mockLocations);
        when(supplierRepository.findAll()).thenReturn(mockSuppliers);
        when(skuRepository.findAll()).thenReturn(mockSkus);
        lenient().when(leadTimeRepository.findAll()).thenReturn(mockLeadTimes);
        lenient().when(supplierCapacityRepository.findAll()).thenReturn(mockCapacities);
    }

    @Nested
    @DisplayName("Planning Data Preview Tests")
    class PreviewTests {

        @Test
        @DisplayName("Should generate preview with exact counts without persisting records")
        void testPreviewCountsAndZeroPersistence() {
            mockAvailableParents();

            PlanningDataPreviewResponse preview = generator.previewPlanningData(defaultRequest);

            assertThat(preview).isNotNull();
            assertThat(preview.isPersisted()).isFalse();
            assertThat(preview.getSeed()).isEqualTo(20261022L);
            assertThat(preview.getPurchaseOrderCount()).isEqualTo(20);
            assertThat(preview.getProductionPlanCount()).isEqualTo(10);
            assertThat(preview.getTotalRecords()).isEqualTo(30);

            assertThat(preview.getPreviewPurchaseOrders()).hasSize(20);
            assertThat(preview.getPreviewProductionPlans()).hasSize(10);

            // Verify no saves occurred
            verify(purchaseOrderRepository, never()).save(any());
            verify(purchaseOrderRepository, never()).saveAll(any());
            verify(productionPlanRepository, never()).save(any());
            verify(productionPlanRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("Preview should format codes sequentially starting with index 1")
        void testPreviewCodeSequencing() {
            mockAvailableParents();

            PlanningDataPreviewResponse preview = generator.previewPlanningData(defaultRequest);

            assertThat(preview.getPreviewPurchaseOrders().get(0).poNumber()).isEqualTo("PO-SYN-0001");
            assertThat(preview.getPreviewPurchaseOrders().get(19).poNumber()).isEqualTo("PO-SYN-0020");

            assertThat(preview.getPreviewProductionPlans().get(0).planCode()).isEqualTo("PP-SYN-0001");
            assertThat(preview.getPreviewProductionPlans().get(9).planCode()).isEqualTo("PP-SYN-0010");
        }
    }

    @Nested
    @DisplayName("Determinism Tests")
    class DeterminismTests {

        @Test
        @DisplayName("Same seed should generate identical PO numbers and plan codes")
        void testDeterministicGeneration() {
            mockAvailableParents();

            PlanningDataPreviewResponse run1 = generator.previewPlanningData(defaultRequest);
            PlanningDataPreviewResponse run2 = generator.previewPlanningData(defaultRequest);

            for (int i = 0; i < 20; i++) {
                assertThat(run1.getPreviewPurchaseOrders().get(i).poNumber())
                        .isEqualTo(run2.getPreviewPurchaseOrders().get(i).poNumber());
                assertThat(run1.getPreviewPurchaseOrders().get(i).quantityOrdered())
                        .isEqualTo(run2.getPreviewPurchaseOrders().get(i).quantityOrdered());
                assertThat(run1.getPreviewPurchaseOrders().get(i).status())
                        .isEqualTo(run2.getPreviewPurchaseOrders().get(i).status());
            }

            for (int i = 0; i < 10; i++) {
                assertThat(run1.getPreviewProductionPlans().get(i).planCode())
                        .isEqualTo(run2.getPreviewProductionPlans().get(i).planCode());
                assertThat(run1.getPreviewProductionPlans().get(i).plannedQuantity())
                        .isEqualTo(run2.getPreviewProductionPlans().get(i).plannedQuantity());
                assertThat(run1.getPreviewProductionPlans().get(i).capacityUtilization())
                        .isEqualTo(run2.getPreviewProductionPlans().get(i).capacityUtilization());
            }
        }

        @Test
        @DisplayName("Different seeds should generate different distributions")
        void testDifferentSeedsProduceDifferentData() {
            mockAvailableParents();

            PlanningDataGenerationRequest requestSeedA = new PlanningDataGenerationRequest(15, 8, 111111L);
            PlanningDataGenerationRequest requestSeedB = new PlanningDataGenerationRequest(15, 8, 999999L);

            PlanningDataPreviewResponse runA = generator.previewPlanningData(requestSeedA);
            PlanningDataPreviewResponse runB = generator.previewPlanningData(requestSeedB);

            // Check that at least one PO quantity differs
            boolean anyDifference = false;
            for (int i = 0; i < 15; i++) {
                if (!runA.getPreviewPurchaseOrders().get(i).quantityOrdered()
                        .equals(runB.getPreviewPurchaseOrders().get(i).quantityOrdered())) {
                    anyDifference = true;
                    break;
                }
            }
            assertThat(anyDifference).isTrue();
        }
    }

    @Nested
    @DisplayName("Purchase Order Domain Validation Tests")
    class PurchaseOrderDomainTests {

        @Test
        @DisplayName("Purchase orders should obey quantity, pricing, lead time, and status invariants")
        void testPurchaseOrderInvariants() {
            mockAvailableParents();

            PlanningDataPreviewResponse preview = generator.previewPlanningData(defaultRequest);

            for (var po : preview.getPreviewPurchaseOrders()) {
                assertThat(po.quantityOrdered()).isGreaterThan(BigDecimal.ZERO);
                assertThat(po.quantityReceived()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
                assertThat(po.quantityReceived()).isLessThanOrEqualTo(po.quantityOrdered());

                assertThat(po.unitPrice()).isGreaterThan(BigDecimal.ZERO);
                BigDecimal expectedTotal = po.unitPrice().multiply(po.quantityOrdered());
                assertThat(po.totalAmount()).isEqualByComparingTo(expectedTotal);

                assertThat(po.expectedDeliveryDate()).isAfterOrEqualTo(po.orderDate());

                if (po.status() == PurchaseOrderStatus.DELIVERED) {
                    assertThat(po.actualDeliveryDate()).isNotNull();
                    assertThat(po.quantityReceived()).isEqualByComparingTo(po.quantityOrdered());
                } else if (po.status() == PurchaseOrderStatus.PARTIALLY_DELIVERED) {
                    assertThat(po.quantityReceived()).isGreaterThan(BigDecimal.ZERO);
                    assertThat(po.quantityReceived()).isLessThan(po.quantityOrdered());
                } else if (po.status() == PurchaseOrderStatus.IN_TRANSIT ||
                           po.status() == PurchaseOrderStatus.PLACED ||
                           po.status() == PurchaseOrderStatus.DRAFT ||
                           po.status() == PurchaseOrderStatus.CANCELLED) {
                    assertThat(po.actualDeliveryDate()).isNull();
                    assertThat(po.quantityReceived()).isEqualByComparingTo(BigDecimal.ZERO);
                }
            }
        }
    }

    @Nested
    @DisplayName("Production Plan Domain Validation Tests")
    class ProductionPlanDomainTests {

        @Test
        @DisplayName("Production plans should prioritize plants and obey status and capacity invariants")
        void testProductionPlanInvariants() {
            mockAvailableParents();

            PlanningDataPreviewResponse preview = generator.previewPlanningData(defaultRequest);

            for (var plan : preview.getPreviewProductionPlans()) {
                assertThat(plan.plannedQuantity()).isGreaterThan(BigDecimal.ZERO);
                assertThat(plan.producedQuantity()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
                assertThat(plan.producedQuantity()).isLessThanOrEqualTo(plan.plannedQuantity());

                assertThat(plan.endDate()).isAfterOrEqualTo(plan.startDate());

                // Capacity utilization between 65.0% and 110.0%
                assertThat(plan.capacityUtilization()).isBetween(65.0, 110.0);

                // Priority for PLANT location
                assertThat(plan.locationCode()).isEqualTo("LOC-SYN-001");

                if (plan.status() == ProductionPlanStatus.COMPLETED) {
                    assertThat(plan.producedQuantity()).isGreaterThan(BigDecimal.ZERO);
                } else if (plan.status() == ProductionPlanStatus.SCHEDULED ||
                           plan.status() == ProductionPlanStatus.DRAFT) {
                    assertThat(plan.producedQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
                }
            }
        }
    }

    @Nested
    @DisplayName("Persistence & Conflict Avoidance Tests")
    class PersistenceTests {

        @Test
        @DisplayName("Persisting planning data should invoke saveAll on repositories and return accurate results")
        void testGeneratePlanningDataPersistence() {
            mockAvailableParents();

            when(purchaseOrderRepository.existsByPoNumber(anyString())).thenReturn(false);
            when(productionPlanRepository.existsByPlanCode(anyString())).thenReturn(false);

            when(purchaseOrderRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(productionPlanRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            PlanningDataGenerationResult result = generator.generatePlanningData(defaultRequest);

            assertThat(result).isNotNull();
            assertThat(result.getPurchaseOrdersSaved()).isEqualTo(20);
            assertThat(result.getProductionPlansSaved()).isEqualTo(10);
            assertThat(result.getTotalRecordsSaved()).isEqualTo(30);
            assertThat(result.getSeed()).isEqualTo(20261022L);
            assertThat(result.getSavedPurchaseOrderSummaries()).hasSize(20);
            assertThat(result.getSavedProductionPlanSummaries()).hasSize(10);

            verify(purchaseOrderRepository, times(1)).saveAll(anyList());
            verify(productionPlanRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("Sequence numbering should skip existing database codes to avoid duplicate key conflicts")
        void testCollisionAvoidanceSequencing() {
            mockAvailableParents();

            // Simulate PO-SYN-0001 through PO-SYN-0005 existing
            when(purchaseOrderRepository.existsByPoNumber("PO-SYN-0001")).thenReturn(true);
            when(purchaseOrderRepository.existsByPoNumber("PO-SYN-0002")).thenReturn(true);
            when(purchaseOrderRepository.existsByPoNumber("PO-SYN-0003")).thenReturn(true);
            when(purchaseOrderRepository.existsByPoNumber("PO-SYN-0004")).thenReturn(true);
            when(purchaseOrderRepository.existsByPoNumber("PO-SYN-0005")).thenReturn(true);
            lenient().when(purchaseOrderRepository.existsByPoNumber(argThat(s -> s != null && !s.matches("PO-SYN-000[1-5]")))).thenReturn(false);

            // Simulate PP-SYN-0001 through PP-SYN-0003 existing
            when(productionPlanRepository.existsByPlanCode("PP-SYN-0001")).thenReturn(true);
            when(productionPlanRepository.existsByPlanCode("PP-SYN-0002")).thenReturn(true);
            when(productionPlanRepository.existsByPlanCode("PP-SYN-0003")).thenReturn(true);
            lenient().when(productionPlanRepository.existsByPlanCode(argThat(s -> s != null && !s.matches("PP-SYN-000[1-3]")))).thenReturn(false);

            when(purchaseOrderRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(productionPlanRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            PlanningDataGenerationRequest smallRequest = new PlanningDataGenerationRequest(3, 2, 20261022L);
            PlanningDataGenerationResult result = generator.generatePlanningData(smallRequest);

            assertThat(result.getPurchaseOrdersSaved()).isEqualTo(3);
            assertThat(result.getProductionPlansSaved()).isEqualTo(2);

            assertThat(result.getSavedPurchaseOrderSummaries().get(0)).contains("PO: PO-SYN-0006");
            assertThat(result.getSavedPurchaseOrderSummaries().get(1)).contains("PO: PO-SYN-0007");
            assertThat(result.getSavedPurchaseOrderSummaries().get(2)).contains("PO: PO-SYN-0008");

            assertThat(result.getSavedProductionPlanSummaries().get(0)).contains("PLAN: PP-SYN-0004");
            assertThat(result.getSavedProductionPlanSummaries().get(1)).contains("PLAN: PP-SYN-0005");
        }
    }

    @Nested
    @DisplayName("Validation and Error Handling Tests")
    class ValidationTests {

        @Test
        @DisplayName("Negative purchase order count should throw BusinessValidationException")
        void testNegativePurchaseOrderCount() {
            PlanningDataGenerationRequest request = new PlanningDataGenerationRequest(-1, 10, 20261022L);
            assertThatThrownBy(() -> generator.previewPlanningData(request))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Purchase order count must be between 0 and 500");
        }

        @Test
        @DisplayName("Excessive purchase order count should throw BusinessValidationException")
        void testExcessivePurchaseOrderCount() {
            PlanningDataGenerationRequest request = new PlanningDataGenerationRequest(501, 10, 20261022L);
            assertThatThrownBy(() -> generator.previewPlanningData(request))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Purchase order count must be between 0 and 500");
        }

        @Test
        @DisplayName("Excessive production plan count should throw BusinessValidationException")
        void testExcessiveProductionPlanCount() {
            PlanningDataGenerationRequest request = new PlanningDataGenerationRequest(20, 301, 20261022L);
            assertThatThrownBy(() -> generator.previewPlanningData(request))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Production plan count must be between 0 and 300");
        }

        @Test
        @DisplayName("Missing master data should throw BusinessValidationException")
        void testMissingMasterDataThrowsException() {
            when(locationRepository.findAll()).thenReturn(Collections.emptyList());
            when(supplierRepository.findAll()).thenReturn(mockSuppliers);
            when(skuRepository.findAll()).thenReturn(mockSkus);

            assertThatThrownBy(() -> generator.previewPlanningData(defaultRequest))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Parent master data is missing");
        }

        @Test
        @DisplayName("Missing PLANT location should throw BusinessValidationException")
        void testMissingPlantLocationThrowsException() {
            Location warehouseOnly = new Location("LOC-SYN-002", "Dallas Warehouse", LocationType.WAREHOUSE, "Dallas", "USA", BigDecimal.valueOf(100000));
            warehouseOnly.setId(2L);
            when(locationRepository.findAll()).thenReturn(List.of(warehouseOnly));
            when(supplierRepository.findAll()).thenReturn(mockSuppliers);
            when(skuRepository.findAll()).thenReturn(mockSkus);

            assertThatThrownBy(() -> generator.previewPlanningData(defaultRequest))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("PLANT");
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Statistics should return accurate total and synthetic counts")
        void testPlanningDataStatistics() {
            when(purchaseOrderRepository.count()).thenReturn(35L);
            when(purchaseOrderRepository.countBySyntheticPoNumber("PO-SYN-")).thenReturn(20L);
            when(productionPlanRepository.count()).thenReturn(15L);
            when(productionPlanRepository.countBySyntheticPlanCode("PP-SYN-")).thenReturn(10L);

            PlanningDataStatisticsResponse stats = generator.getPlanningDataStatistics();

            assertThat(stats).isNotNull();
            assertThat(stats.getTotalPurchaseOrders()).isEqualTo(35L);
            assertThat(stats.getTotalSyntheticPurchaseOrders()).isEqualTo(20L);

            assertThat(stats.getTotalProductionPlans()).isEqualTo(15L);
            assertThat(stats.getTotalSyntheticProductionPlans()).isEqualTo(10L);

            assertThat(stats.getTotalPlanningRecords()).isEqualTo(50L);
            assertThat(stats.getTotalSyntheticPlanningRecords()).isEqualTo(30L);
        }
    }
}
