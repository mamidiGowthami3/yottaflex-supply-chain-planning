package com.yottaflex.supplychain.engine;

import com.yottaflex.supplychain.dto.generator.ShortageDetectionRequest;
import com.yottaflex.supplychain.dto.generator.ShortageGenerationResult;
import com.yottaflex.supplychain.dto.generator.ShortagePreviewResponse;
import com.yottaflex.supplychain.dto.generator.ShortageStatisticsResponse;
import com.yottaflex.supplychain.entity.*;
import com.yottaflex.supplychain.entity.enums.*;
import com.yottaflex.supplychain.exception.BusinessValidationException;
import com.yottaflex.supplychain.repository.*;
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
class SupplyChainPlanningEngineTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ForecastDemandRepository forecastDemandRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Mock
    private SupplierCapacityRepository supplierCapacityRepository;

    @Mock
    private LeadTimeRepository leadTimeRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SKURepository skuRepository;

    @Mock
    private ShortageRepository shortageRepository;

    @Mock
    private MitigationOptionRepository mitigationOptionRepository;

    @InjectMocks
    private SupplyChainPlanningEngine planningEngine;

    private SKU testSku;
    private Location plantLocation;
    private Location warehouseLocation;
    private Supplier primarySupplier;
    private Supplier alternateSupplier;

    @BeforeEach
    void setUp() {
        testSku = new SKU("SKU-SYN-001", "Standard Test Widget", "WIDGET",
                BigDecimal.valueOf(25.00), BigDecimal.valueOf(35.00), 10, 5, 3);
        testSku.setId(1L);

        plantLocation = new Location("LOC-SYN-001", "Austin Plant 1", LocationType.PLANT, "Austin", "USA", BigDecimal.valueOf(50000.0));
        plantLocation.setId(1L);

        warehouseLocation = new Location("LOC-SYN-002", "Dallas Warehouse", LocationType.WAREHOUSE, "Dallas", "USA", BigDecimal.valueOf(100000.0));
        warehouseLocation.setId(2L);

        primarySupplier = new Supplier("SUP-SYN-001", "Primary Supplier Co", "John Doe", "john@primary.com", "USA", 4.8, 98.0);
        primarySupplier.setId(1L);

        alternateSupplier = new Supplier("SUP-SYN-002", "Backup Supplier Corp", "Jane Doe", "jane@backup.com", "USA", 4.5, 94.0);
        alternateSupplier.setId(2L);
    }

    private void setupBaseRepositories() {
        when(skuRepository.findAll()).thenReturn(List.of(testSku));
        when(locationRepository.findAll()).thenReturn(List.of(plantLocation, warehouseLocation));
        when(supplierRepository.findAll()).thenReturn(List.of(primarySupplier, alternateSupplier));
    }

    @Nested
    @DisplayName("Request Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Null request should throw BusinessValidationException")
        void testNullRequestThrowsException() {
            assertThatThrownBy(() -> planningEngine.previewShortages(null))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Horizon weeks <= 0 or > 12 should throw BusinessValidationException")
        void testInvalidHorizonWeeks() {
            ShortageDetectionRequest reqLow = new ShortageDetectionRequest(0, 50, 20261022L);
            assertThatThrownBy(() -> planningEngine.previewShortages(reqLow))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Horizon weeks must be between 1 and 12");

            ShortageDetectionRequest reqHigh = new ShortageDetectionRequest(13, 50, 20261022L);
            assertThatThrownBy(() -> planningEngine.previewShortages(reqHigh))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Horizon weeks must be between 1 and 12");
        }

        @Test
        @DisplayName("Max shortages <= 0 or > 200 should throw BusinessValidationException")
        void testInvalidMaxShortages() {
            ShortageDetectionRequest reqLow = new ShortageDetectionRequest(4, 0, 20261022L);
            assertThatThrownBy(() -> planningEngine.previewShortages(reqLow))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Max shortages must be between 1 and 200");

            ShortageDetectionRequest reqHigh = new ShortageDetectionRequest(4, 201, 20261022L);
            assertThatThrownBy(() -> planningEngine.previewShortages(reqHigh))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Max shortages must be between 1 and 200");
        }
    }

    @Nested
    @DisplayName("Time-Phased Shortage Detection Tests")
    class DetectionTests {

        @Test
        @DisplayName("When demand exceeds available supply, detects shortage with accurate quantity")
        void testDetectShortageWhenDemandExceedsSupply() {
            setupBaseRepositories();

            // Initial Inventory: on hand 50, reserved 10 -> net available 40
            Inventory inv = new Inventory(testSku, plantLocation, BigDecimal.valueOf(50.0), BigDecimal.valueOf(10.0),
                    BigDecimal.valueOf(15.0), BigDecimal.valueOf(20.0));
            when(inventoryRepository.findAll()).thenReturn(List.of(inv));

            // Forecast Demand in week 1: 140
            LocalDate forecastDate = LocalDate.now().plusDays(7);
            ForecastDemand demand = new ForecastDemand(testSku, plantLocation, forecastDate, DemandPeriodType.WEEKLY,
                    BigDecimal.valueOf(140.0), 0.95, DemandPattern.SEASONAL);
            when(forecastDemandRepository.findAll()).thenReturn(List.of(demand));

            // Inbound PO: 20 units arriving before forecast date
            PurchaseOrder po = new PurchaseOrder("PO-SYN-0001", primarySupplier, testSku, plantLocation,
                    LocalDate.now().minusDays(5), forecastDate.minusDays(2), BigDecimal.valueOf(20.0),
                    BigDecimal.valueOf(25.0));
            po.setStatus(PurchaseOrderStatus.IN_TRANSIT);
            po.setId(1L);
            when(purchaseOrderRepository.findAll()).thenReturn(List.of(po));

            when(productionPlanRepository.findAll()).thenReturn(Collections.emptyList());
            when(supplierCapacityRepository.findAll()).thenReturn(Collections.emptyList());
            when(leadTimeRepository.findAll()).thenReturn(Collections.emptyList());

            ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);
            ShortagePreviewResponse preview = planningEngine.previewShortages(request);

            // Starting available = 40. Inbound PO = 20. Total supply = 60. Demand = 140.
            // Deficit = 140 - 60 = 80 units
            assertThat(preview).isNotNull();
            assertThat(preview.getShortageCount()).isGreaterThanOrEqualTo(1);

            var firstShortage = preview.getPreviewShortages().get(0);
            assertThat(firstShortage.shortageQuantity()).isEqualByComparingTo(BigDecimal.valueOf(80.0));
            assertThat(firstShortage.startingInventory()).isEqualByComparingTo(BigDecimal.valueOf(40.0));
            assertThat(firstShortage.incomingSupply()).isEqualByComparingTo(BigDecimal.valueOf(20.0));
            assertThat(firstShortage.forecastDemand()).isEqualByComparingTo(BigDecimal.valueOf(140.0));
            assertThat(firstShortage.projectedBalance()).isEqualByComparingTo(BigDecimal.valueOf(-80.0));

            // Verify zero persistence during preview
            verify(shortageRepository, never()).save(any());
            verify(shortageRepository, never()).saveAll(anyList());
            verify(mitigationOptionRepository, never()).save(any());
            verify(mitigationOptionRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("When supply is sufficient for demand, zero shortages are detected")
        void testZeroShortageWhenSupplySufficient() {
            setupBaseRepositories();

            // On hand 200, reserved 0 -> net available 200
            Inventory inv = new Inventory(testSku, plantLocation, BigDecimal.valueOf(200.0), BigDecimal.ZERO,
                    BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0));
            when(inventoryRepository.findAll()).thenReturn(List.of(inv));

            // Demand: 100
            ForecastDemand demand = new ForecastDemand(testSku, plantLocation, LocalDate.now().plusDays(7),
                    DemandPeriodType.WEEKLY, BigDecimal.valueOf(100.0), 0.95, DemandPattern.STABLE);
            when(forecastDemandRepository.findAll()).thenReturn(List.of(demand));

            when(purchaseOrderRepository.findAll()).thenReturn(Collections.emptyList());
            when(productionPlanRepository.findAll()).thenReturn(Collections.emptyList());
            when(supplierCapacityRepository.findAll()).thenReturn(Collections.emptyList());
            when(leadTimeRepository.findAll()).thenReturn(Collections.emptyList());

            ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);
            ShortagePreviewResponse preview = planningEngine.previewShortages(request);

            assertThat(preview.getShortageCount()).isZero();
            assertThat(preview.getPreviewShortages()).isEmpty();
        }

        @Test
        @DisplayName("Rule 5: Production plan at PLANT is NOT counted as supply at WAREHOUSE")
        void testRule5ProductionAtPlantNotCountedAtWarehouse() {
            // Only examine warehouse location
            when(skuRepository.findAll()).thenReturn(List.of(testSku));
            when(locationRepository.findAll()).thenReturn(List.of(warehouseLocation));
            when(supplierRepository.findAll()).thenReturn(List.of(primarySupplier));

            // Warehouse inventory is 0
            Inventory whInv = new Inventory(testSku, warehouseLocation, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            when(inventoryRepository.findAll()).thenReturn(List.of(whInv));

            // Warehouse demand: 150
            LocalDate week1 = LocalDate.now().plusDays(5);
            ForecastDemand demand = new ForecastDemand(testSku, warehouseLocation, week1,
                    DemandPeriodType.WEEKLY, BigDecimal.valueOf(150.0), 0.95, DemandPattern.TRENDING);
            when(forecastDemandRepository.findAll()).thenReturn(List.of(demand));

            when(purchaseOrderRepository.findAll()).thenReturn(Collections.emptyList());

            // There is a production plan of 500 units at PLANT (not warehouse!)
            ProductionPlan plantPlan = new ProductionPlan("PP-SYN-0001", testSku, plantLocation,
                    LocalDate.now(), week1, BigDecimal.valueOf(500.0), 90.0);
            when(productionPlanRepository.findAll()).thenReturn(List.of(plantPlan));

            when(supplierCapacityRepository.findAll()).thenReturn(Collections.emptyList());
            when(leadTimeRepository.findAll()).thenReturn(Collections.emptyList());

            ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);
            ShortagePreviewResponse preview = planningEngine.previewShortages(request);

            // Plant production must NOT count towards warehouse supply; shortage of full 150 must occur
            assertThat(preview.getShortageCount()).isEqualTo(1);
            var item = preview.getPreviewShortages().get(0);
            assertThat(item.locationCode()).isEqualTo("LOC-SYN-002");
            assertThat(item.incomingSupply()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(item.shortageQuantity()).isEqualByComparingTo(BigDecimal.valueOf(150.0));
        }
    }

    @Nested
    @DisplayName("Tactical Mitigations & Feasibility (Rule 6) Tests")
    class MitigationFeasibilityTests {

        @Test
        @DisplayName("Rule 6: Feasible mitigation options are verified and sorted by recommendation score descending")
        void testFeasibleMitigationsGeneratedAndRanked() {
            setupBaseRepositories();

            // Shortage node: PLANT has deficit of 100
            Inventory plantInv = new Inventory(testSku, plantLocation, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            // Surplus node: WAREHOUSE has onHand 300, safety stock 50 -> surplus = 250 (allows REALLOCATE_INVENTORY)
            Inventory whInv = new Inventory(testSku, warehouseLocation, BigDecimal.valueOf(300.0), BigDecimal.ZERO,
                    BigDecimal.valueOf(50.0), BigDecimal.valueOf(30.0));
            when(inventoryRepository.findAll()).thenReturn(List.of(plantInv, whInv));

            LocalDate forecastDate = LocalDate.now().plusDays(10);
            ForecastDemand plantDemand = new ForecastDemand(testSku, plantLocation, forecastDate,
                    DemandPeriodType.WEEKLY, BigDecimal.valueOf(100.0), 0.95, DemandPattern.STABLE);
            when(forecastDemandRepository.findAll()).thenReturn(List.of(plantDemand));

            // Open PO 1 exists for expedite test (150 units, delivery after forecast)
            PurchaseOrder openPo = new PurchaseOrder("PO-SYN-0001", primarySupplier, testSku, plantLocation,
                    LocalDate.now().minusDays(2), forecastDate.plusDays(5), BigDecimal.valueOf(150.0),
                    BigDecimal.valueOf(25.0));
            openPo.setStatus(PurchaseOrderStatus.PLACED);
            openPo.setId(10L);

            // Open PO 2 exists for split shipment test (200 units, delivery after forecast)
            PurchaseOrder openPo2 = new PurchaseOrder("PO-SYN-0002", primarySupplier, testSku, plantLocation,
                    LocalDate.now().minusDays(2), forecastDate.plusDays(6), BigDecimal.valueOf(200.0),
                    BigDecimal.valueOf(0.0));
            openPo2.setStatus(PurchaseOrderStatus.PLACED);
            openPo2.setId(20L);

            when(purchaseOrderRepository.findAll()).thenReturn(List.of(openPo, openPo2));

            // Lead time allows expedited delivery (standard 14, expedited 5)
            LeadTime lt = new LeadTime(primarySupplier, testSku, plantLocation, 14, 5, 10, BigDecimal.valueOf(3.50), TransportMode.AIR);
            when(leadTimeRepository.findAll()).thenReturn(List.of(lt));

            // Alternate supplier with positive capacity (2000 capacity, 500 allocated -> 1500 available)
            SupplierCapacity cap = new SupplierCapacity(alternateSupplier, testSku, LocalDate.now().minusDays(1),
                    LocalDate.now().plusMonths(3), BigDecimal.valueOf(100.0), BigDecimal.valueOf(2000.0));
            cap.setAllocatedCapacity(BigDecimal.valueOf(500.0));
            cap.updateAvailableCapacity();
            when(supplierCapacityRepository.findAll()).thenReturn(List.of(cap));

            when(productionPlanRepository.findAll()).thenReturn(Collections.emptyList());

            ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);
            ShortagePreviewResponse preview = planningEngine.previewShortages(request);

            assertThat(preview.getShortageCount()).isGreaterThanOrEqualTo(1);
            var plantShortage = preview.getPreviewShortages().stream()
                    .filter(s -> s.locationCode().equals("LOC-SYN-001"))
                    .findFirst()
                    .orElseThrow();

            List<ShortagePreviewResponse.MitigationOptionPreviewItem> mitigations = plantShortage.mitigationOptions();
            assertThat(mitigations).isNotEmpty();

            // Verify scores are in strictly descending order
            for (int i = 0; i < mitigations.size() - 1; i++) {
                assertThat(mitigations.get(i).recommendationScore())
                        .isGreaterThanOrEqualTo(mitigations.get(i + 1).recommendationScore());
            }

            // Verify generated strategies contain expedite, alternative supplier, and split shipment
            List<MitigationStrategy> strategies = mitigations.stream()
                    .map(ShortagePreviewResponse.MitigationOptionPreviewItem::strategy)
                    .toList();
            assertThat(strategies).contains(MitigationStrategy.EXPEDITE_PURCHASE_ORDER);
            assertThat(strategies).contains(MitigationStrategy.ALTERNATIVE_SUPPLIER);
            assertThat(strategies).contains(MitigationStrategy.SPLIT_SHIPMENT);

            // Per Decision 3 & Decision 1: Reallocation and production adjustment are omitted
            assertThat(strategies).doesNotContain(MitigationStrategy.REALLOCATE_INVENTORY);
            assertThat(strategies).doesNotContain(MitigationStrategy.ADJUST_PRODUCTION_SCHEDULE);

            // Verify structured fields on mitigation preview items
            for (var m : mitigations) {
                assertThat(m.mitigatedQuantity()).isNotNull().isGreaterThan(BigDecimal.ZERO);
                assertThat(m.estimatedArrivalDate()).isNotNull().isBeforeOrEqualTo(plantShortage.shortageDate());
                assertThat(m.freightMode()).isNotNull();
                assertThat(m.recommendationScore()).isBetween(0.0, 100.0);
            }

            var expediteOpt = mitigations.stream()
                    .filter(m -> m.strategy() == MitigationStrategy.EXPEDITE_PURCHASE_ORDER)
                    .findFirst().orElseThrow();
            assertThat(expediteOpt.purchaseOrderCode()).isEqualTo("PO-SYN-0001");

            var splitOpt = mitigations.stream()
                    .filter(m -> m.strategy() == MitigationStrategy.SPLIT_SHIPMENT)
                    .findFirst().orElseThrow();
            assertThat(splitOpt.purchaseOrderCode()).isEqualTo("PO-SYN-0002");
            assertThat(splitOpt.splitRemainingQuantity()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Critical Date Invariant: PO arriving after shortage date is rejected")
        void testLateArrivalRejection() {
            setupBaseRepositories();

            Inventory plantInv = new Inventory(testSku, plantLocation, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            when(inventoryRepository.findAll()).thenReturn(List.of(plantInv));

            LocalDate forecastDate = LocalDate.now().plusDays(5);
            ForecastDemand plantDemand = new ForecastDemand(testSku, plantLocation, forecastDate,
                    DemandPeriodType.WEEKLY, BigDecimal.valueOf(100.0), 0.95, DemandPattern.STABLE);
            when(forecastDemandRepository.findAll()).thenReturn(List.of(plantDemand));

            // PO delivery is far out in future: 25 days out. Lead time saves 4 days -> new arrival 21 days out (still > 5 days)
            PurchaseOrder openPo = new PurchaseOrder("PO-SYN-0002", primarySupplier, testSku, plantLocation,
                    LocalDate.now().minusDays(2), forecastDate.plusDays(20), BigDecimal.valueOf(150.0),
                    BigDecimal.valueOf(25.0));
            openPo.setStatus(PurchaseOrderStatus.PLACED);
            openPo.setId(20L);
            when(purchaseOrderRepository.findAll()).thenReturn(List.of(openPo));

            // Standard 14, expedited 10 -> saves 4 days, arrival is forecastDate + 16 (after forecastDate)
            LeadTime lt = new LeadTime(primarySupplier, testSku, plantLocation, 14, 10, 10, BigDecimal.valueOf(3.50), TransportMode.AIR);
            when(leadTimeRepository.findAll()).thenReturn(List.of(lt));

            when(productionPlanRepository.findAll()).thenReturn(Collections.emptyList());
            when(supplierCapacityRepository.findAll()).thenReturn(Collections.emptyList());

            ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);
            ShortagePreviewResponse preview = planningEngine.previewShortages(request);

            assertThat(preview.getShortageCount()).isEqualTo(1);
            var shortage = preview.getPreviewShortages().get(0);
            List<MitigationStrategy> strategies = shortage.mitigationOptions().stream()
                    .map(ShortagePreviewResponse.MitigationOptionPreviewItem::strategy)
                    .toList();
            // Must NOT recommend expedite or split since neither can arrive on or before forecastDate
            assertThat(strategies).doesNotContain(MitigationStrategy.EXPEDITE_PURCHASE_ORDER);
            assertThat(strategies).doesNotContain(MitigationStrategy.SPLIT_SHIPMENT);
        }

        @Test
        @DisplayName("Supplier capacity monthly recurrent quota ledger prevents over-allocation")
        void testSupplierCapacityMonthlyRecurrentLedger() {
            setupBaseRepositories();

            Inventory plantInv = new Inventory(testSku, plantLocation, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            when(inventoryRepository.findAll()).thenReturn(List.of(plantInv));

            // Shortage 1: 150 units on Day 8
            LocalDate date1 = LocalDate.now().plusDays(8);
            ForecastDemand d1 = new ForecastDemand(testSku, plantLocation, date1,
                    DemandPeriodType.WEEKLY, BigDecimal.valueOf(150.0), 0.95, DemandPattern.STABLE);
            // Shortage 2: 200 units on Day 15 (same calendar month)
            LocalDate date2 = LocalDate.now().plusDays(15);
            ForecastDemand d2 = new ForecastDemand(testSku, plantLocation, date2,
                    DemandPeriodType.WEEKLY, BigDecimal.valueOf(200.0), 0.95, DemandPattern.STABLE);
            when(forecastDemandRepository.findAll()).thenReturn(List.of(d1, d2));

            // Supplier capacity: only 200 monthly capacity total
            SupplierCapacity cap = new SupplierCapacity(alternateSupplier, testSku, LocalDate.now().minusDays(5),
                    LocalDate.now().plusMonths(2), BigDecimal.valueOf(10.0), BigDecimal.valueOf(200.0));
            cap.setAvailableCapacity(BigDecimal.valueOf(200.0));
            cap.setId(50L);
            when(supplierCapacityRepository.findAll()).thenReturn(List.of(cap));

            // Lead time 3 days (can arrive on time for both)
            LeadTime lt = new LeadTime(alternateSupplier, testSku, plantLocation, 3, 2, 1, BigDecimal.valueOf(4.0), TransportMode.ROAD);
            when(leadTimeRepository.findAll()).thenReturn(List.of(lt));

            when(purchaseOrderRepository.findAll()).thenReturn(Collections.emptyList());
            when(productionPlanRepository.findAll()).thenReturn(Collections.emptyList());

            ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);
            ShortagePreviewResponse preview = planningEngine.previewShortages(request);

            assertThat(preview.getShortageCount()).isEqualTo(2);
            var shortage1 = preview.getPreviewShortages().get(0);
            var shortage2 = preview.getPreviewShortages().get(1);

            // Shortage 1 takes 150 units of the 200 monthly capacity
            var opt1 = shortage1.mitigationOptions().stream()
                    .filter(m -> m.strategy() == MitigationStrategy.ALTERNATIVE_SUPPLIER)
                    .findFirst().orElseThrow();
            assertThat(opt1.mitigatedQuantity()).isEqualByComparingTo(BigDecimal.valueOf(150.0));

            // Shortage 2 can only take the remaining 50 units (200 - 150), NOT another 200!
            var opt2 = shortage2.mitigationOptions().stream()
                    .filter(m -> m.strategy() == MitigationStrategy.ALTERNATIVE_SUPPLIER)
                    .findFirst().orElseThrow();
            assertThat(opt2.mitigatedQuantity()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        }

        @Test
        @DisplayName("Dynamic weight renormalization scoring without arbitrary defaults")
        void testDynamicWeightRenormalizationScoring() {
            setupBaseRepositories();

            // SKU with null unitCost to test dynamic weight renormalization
            SKU noCostSku = new SKU("SKU-SYN-099", "No Cost Widget", "WIDGET",
                    null, null, 10, 5, 3);
            noCostSku.setId(99L);
            when(skuRepository.findAll()).thenReturn(List.of(noCostSku));

            Inventory plantInv = new Inventory(noCostSku, plantLocation, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            when(inventoryRepository.findAll()).thenReturn(List.of(plantInv));

            LocalDate forecastDate = LocalDate.now().plusDays(10);
            ForecastDemand demand = new ForecastDemand(noCostSku, plantLocation, forecastDate,
                    DemandPeriodType.WEEKLY, BigDecimal.valueOf(100.0), 0.95, DemandPattern.STABLE);
            when(forecastDemandRepository.findAll()).thenReturn(List.of(demand));

            // Supplier with null reliability score to test double renormalization
            Supplier noRelSupplier = new Supplier("SUP-SYN-099", "No Rel Supplier", "Jane", "jane@norel.com", "USA", null, null);
            noRelSupplier.setId(99L);

            PurchaseOrder openPo = new PurchaseOrder("PO-SYN-0099", noRelSupplier, noCostSku, plantLocation,
                    LocalDate.now().minusDays(2), forecastDate.plusDays(4), BigDecimal.valueOf(150.0),
                    BigDecimal.valueOf(25.0));
            openPo.setStatus(PurchaseOrderStatus.PLACED);
            openPo.setId(99L);
            when(purchaseOrderRepository.findAll()).thenReturn(List.of(openPo));

            LeadTime lt = new LeadTime(noRelSupplier, noCostSku, plantLocation, 14, 5, 10, BigDecimal.valueOf(3.50), TransportMode.AIR);
            when(leadTimeRepository.findAll()).thenReturn(List.of(lt));

            when(productionPlanRepository.findAll()).thenReturn(Collections.emptyList());
            when(supplierCapacityRepository.findAll()).thenReturn(Collections.emptyList());

            ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);
            ShortagePreviewResponse preview = planningEngine.previewShortages(request);

            assertThat(preview.getShortageCount()).isEqualTo(1);
            var shortage = preview.getPreviewShortages().get(0);
            assertThat(shortage.mitigationOptions()).isNotEmpty();
            var opt = shortage.mitigationOptions().get(0);

            // Score must be calculated cleanly over available dimensions and stay bounded in [0, 100]
            assertThat(opt.recommendationScore()).isBetween(0.0, 100.0);
            assertThat(opt.recommendationScore()).isNotEqualTo(80.0); // Not hardcoded default 80
        }

        @Test
        @DisplayName("Reallocation and production adjustment are omitted per architecture decisions")
        void testReallocationAndProductionAdjustmentOmitted() {
            setupBaseRepositories();

            Inventory plantInv = new Inventory(testSku, plantLocation, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            Inventory whInv = new Inventory(testSku, warehouseLocation, BigDecimal.valueOf(500.0), BigDecimal.ZERO,
                    BigDecimal.valueOf(50.0), BigDecimal.valueOf(30.0));
            when(inventoryRepository.findAll()).thenReturn(List.of(plantInv, whInv));

            LocalDate forecastDate = LocalDate.now().plusDays(7);
            ForecastDemand demand = new ForecastDemand(testSku, plantLocation, forecastDate,
                    DemandPeriodType.WEEKLY, BigDecimal.valueOf(80.0), 0.95, DemandPattern.STABLE);
            when(forecastDemandRepository.findAll()).thenReturn(List.of(demand));

            when(purchaseOrderRepository.findAll()).thenReturn(Collections.emptyList());
            when(productionPlanRepository.findAll()).thenReturn(Collections.emptyList());
            when(supplierCapacityRepository.findAll()).thenReturn(Collections.emptyList());
            when(leadTimeRepository.findAll()).thenReturn(Collections.emptyList());

            ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);
            ShortagePreviewResponse preview = planningEngine.previewShortages(request);

            var plantShortage = preview.getPreviewShortages().stream()
                    .filter(s -> s.locationCode().equals("LOC-SYN-001"))
                    .findFirst()
                    .orElseThrow();

            List<MitigationStrategy> strategies = plantShortage.mitigationOptions().stream()
                    .map(ShortagePreviewResponse.MitigationOptionPreviewItem::strategy)
                    .toList();
            assertThat(strategies).doesNotContain(MitigationStrategy.REALLOCATE_INVENTORY);
            assertThat(strategies).doesNotContain(MitigationStrategy.ADJUST_PRODUCTION_SCHEDULE);
        }
    }

    @Nested
    @DisplayName("Persistence & Deduplication Tests")
    class PersistenceTests {

        @Test
        @DisplayName("detectAndMitigateShortages persists new shortages and mitigations into database")
        void testPersistNewShortages() {
            setupBaseRepositories();

            Inventory plantInv = new Inventory(testSku, plantLocation, BigDecimal.valueOf(10.0), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            when(inventoryRepository.findAll()).thenReturn(List.of(plantInv));

            LocalDate forecastDate = LocalDate.now().plusDays(7);
            ForecastDemand demand = new ForecastDemand(testSku, plantLocation, forecastDate,
                    DemandPeriodType.WEEKLY, BigDecimal.valueOf(100.0), 0.95, DemandPattern.STABLE);
            when(forecastDemandRepository.findAll()).thenReturn(List.of(demand));

            when(purchaseOrderRepository.findAll()).thenReturn(Collections.emptyList());
            when(productionPlanRepository.findAll()).thenReturn(Collections.emptyList());
            when(supplierCapacityRepository.findAll()).thenReturn(Collections.emptyList());
            when(leadTimeRepository.findAll()).thenReturn(Collections.emptyList());

            // Mock repository duplicate check and saveAll
            when(shortageRepository.existsBySkuIdAndLocationIdAndShortageDateAndResolvedFalse(any(), any(), any()))
                    .thenReturn(false);
            when(shortageRepository.existsByShortageCode(any())).thenReturn(false);

            when(shortageRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);
            ShortageGenerationResult result = planningEngine.detectAndMitigateShortages(request);

            assertThat(result.getShortagesSaved()).isGreaterThanOrEqualTo(1);
            verify(shortageRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("detectAndMitigateShortages skips persisting when an active shortage already exists")
        void testDeduplicateExistingActiveShortage() {
            setupBaseRepositories();

            Inventory plantInv = new Inventory(testSku, plantLocation, BigDecimal.valueOf(10.0), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            when(inventoryRepository.findAll()).thenReturn(List.of(plantInv));

            LocalDate forecastDate = LocalDate.now().plusDays(7);
            ForecastDemand demand = new ForecastDemand(testSku, plantLocation, forecastDate,
                    DemandPeriodType.WEEKLY, BigDecimal.valueOf(100.0), 0.95, DemandPattern.STABLE);
            when(forecastDemandRepository.findAll()).thenReturn(List.of(demand));

            when(purchaseOrderRepository.findAll()).thenReturn(Collections.emptyList());
            when(productionPlanRepository.findAll()).thenReturn(Collections.emptyList());
            when(supplierCapacityRepository.findAll()).thenReturn(Collections.emptyList());
            when(leadTimeRepository.findAll()).thenReturn(Collections.emptyList());

            // An unresolved shortage already exists for this SKU, Location, and Date
            when(shortageRepository.existsBySkuIdAndLocationIdAndShortageDateAndResolvedFalse(any(), any(), any()))
                    .thenReturn(true);

            when(shortageRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);
            ShortageGenerationResult result = planningEngine.detectAndMitigateShortages(request);

            // Should be 0 saved due to deduplication
            assertThat(result.getShortagesSaved()).isZero();
            assertThat(result.getMitigationsSaved()).isZero();
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("getShortageStatistics retrieves correct aggregated counts from repositories")
        void testGetShortageStatistics() {
            when(shortageRepository.count()).thenReturn(10L);
            when(shortageRepository.countBySyntheticShortageCode("SH-SYN-")).thenReturn(8L);
            when(shortageRepository.countByResolvedFalse()).thenReturn(7L);
            when(shortageRepository.countByResolvedTrue()).thenReturn(3L);

            when(shortageRepository.countBySeverity(ShortageSeverity.CRITICAL)).thenReturn(2L);
            when(shortageRepository.countBySeverity(ShortageSeverity.HIGH)).thenReturn(4L);
            when(shortageRepository.countBySeverity(ShortageSeverity.MEDIUM)).thenReturn(3L);
            when(shortageRepository.countBySeverity(ShortageSeverity.LOW)).thenReturn(1L);

            when(mitigationOptionRepository.count()).thenReturn(25L);
            when(mitigationOptionRepository.countBySyntheticOptionCode("MIT-SYN-")).thenReturn(20L);
            when(mitigationOptionRepository.countByStatus(MitigationStatus.PROPOSED)).thenReturn(18L);
            when(mitigationOptionRepository.countByStatus(MitigationStatus.APPROVED)).thenReturn(5L);
            when(mitigationOptionRepository.countByStatus(MitigationStatus.EXECUTED)).thenReturn(2L);

            ShortageStatisticsResponse stats = planningEngine.getShortageStatistics();

            assertThat(stats.getTotalShortages()).isEqualTo(10L);
            assertThat(stats.getSyntheticShortages()).isEqualTo(8L);
            assertThat(stats.getActiveShortages()).isEqualTo(7L);
            assertThat(stats.getResolvedShortages()).isEqualTo(3L);
            assertThat(stats.getCriticalSeverityShortages()).isEqualTo(2L);
            assertThat(stats.getHighSeverityShortages()).isEqualTo(4L);
            assertThat(stats.getTotalMitigationOptions()).isEqualTo(25L);
            assertThat(stats.getProposedMitigations()).isEqualTo(18L);
            assertThat(stats.getApprovedMitigations()).isEqualTo(5L);
            assertThat(stats.getExecutedMitigations()).isEqualTo(2L);
        }
    }
}
