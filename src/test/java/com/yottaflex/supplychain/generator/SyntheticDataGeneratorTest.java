package com.yottaflex.supplychain.generator;

import com.yottaflex.supplychain.dto.generator.MasterDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.MasterDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.MasterDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.MasterDataStatisticsResponse;
import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.entity.Supplier;
import com.yottaflex.supplychain.exception.BusinessValidationException;
import com.yottaflex.supplychain.repository.LocationRepository;
import com.yottaflex.supplychain.repository.SKURepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyntheticDataGeneratorTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SKURepository skuRepository;

    @Mock
    private com.yottaflex.supplychain.repository.InventoryRepository inventoryRepository;

    @Mock
    private com.yottaflex.supplychain.repository.ForecastDemandRepository forecastDemandRepository;

    @Mock
    private com.yottaflex.supplychain.repository.SupplierCapacityRepository supplierCapacityRepository;

    @Mock
    private com.yottaflex.supplychain.repository.LeadTimeRepository leadTimeRepository;

    @Mock
    private com.yottaflex.supplychain.repository.PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private com.yottaflex.supplychain.repository.ProductionPlanRepository productionPlanRepository;

    @InjectMocks
    private SyntheticDataGenerator generator;

    private MasterDataGenerationRequest defaultRequest;

    @BeforeEach
    void setUp() {
        defaultRequest = new MasterDataGenerationRequest(3, 5, 10, 20261022L);
    }

    @Nested
    @DisplayName("Preview Generation Tests")
    class PreviewTests {

        @Test
        @DisplayName("Should generate preview with exact requested counts without database persistence")
        void testPreviewCountsAndZeroPersistence() {
            MasterDataPreviewResponse preview = generator.previewMasterData(defaultRequest);

            assertThat(preview).isNotNull();
            assertThat(preview.isPersisted()).isFalse();
            assertThat(preview.getSeed()).isEqualTo(20261022L);
            assertThat(preview.getLocationCount()).isEqualTo(3);
            assertThat(preview.getSupplierCount()).isEqualTo(5);
            assertThat(preview.getSkuCount()).isEqualTo(10);
            assertThat(preview.getTotalRecords()).isEqualTo(18);

            assertThat(preview.getPreviewLocations()).hasSize(3);
            assertThat(preview.getPreviewSuppliers()).hasSize(5);
            assertThat(preview.getPreviewSkus()).hasSize(10);

            // Verify no repository save methods were called
            verify(locationRepository, never()).save(any());
            verify(locationRepository, never()).saveAll(anyList());
            verify(supplierRepository, never()).save(any());
            verify(supplierRepository, never()).saveAll(anyList());
            verify(skuRepository, never()).save(any());
            verify(skuRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Should produce strictly deterministic results for identical seed and counts")
        void testPreviewDeterminism() {
            MasterDataPreviewResponse run1 = generator.previewMasterData(new MasterDataGenerationRequest(2, 2, 2, 99999L));
            MasterDataPreviewResponse run2 = generator.previewMasterData(new MasterDataGenerationRequest(2, 2, 2, 99999L));

            assertThat(run1.getPreviewLocations().get(0).getName())
                    .isEqualTo(run2.getPreviewLocations().get(0).getName());
            assertThat(run1.getPreviewLocations().get(0).getCapacity())
                    .isEqualByComparingTo(run2.getPreviewLocations().get(0).getCapacity());

            assertThat(run1.getPreviewSuppliers().get(0).getName())
                    .isEqualTo(run2.getPreviewSuppliers().get(0).getName());
            assertThat(run1.getPreviewSuppliers().get(0).getRating())
                    .isEqualTo(run2.getPreviewSuppliers().get(0).getRating());

            assertThat(run1.getPreviewSkus().get(0).getUnitCost())
                    .isEqualByComparingTo(run2.getPreviewSkus().get(0).getUnitCost());
            assertThat(run1.getPreviewSkus().get(0).getSellingPrice())
                    .isEqualByComparingTo(run2.getPreviewSkus().get(0).getSellingPrice());
        }

        @Test
        @DisplayName("Should generate valid business attributes adhering to domain constraints")
        void testBusinessRuleConstraints() {
            MasterDataPreviewResponse preview = generator.previewMasterData(new MasterDataGenerationRequest(5, 5, 20, 12345L));

            // Location assertions
            for (Location loc : preview.getPreviewLocations()) {
                assertThat(loc.getLocationCode()).startsWith("LOC-SYN-");
                assertThat(loc.getName()).isNotBlank();
                assertThat(loc.getType()).isNotNull();
                assertThat(loc.getCapacity()).isNotNull();
                assertThat(loc.getCapacity()).isGreaterThan(BigDecimal.ZERO);
                assertThat(loc.getActive()).isTrue();
            }

            // Supplier assertions
            for (Supplier sup : preview.getPreviewSuppliers()) {
                assertThat(sup.getSupplierCode()).startsWith("SUP-SYN-");
                assertThat(sup.getName()).isNotBlank();
                assertThat(sup.getEmail()).contains("@example.com");
                assertThat(sup.getRating()).isBetween(0.0, 5.0);
                assertThat(sup.getReliabilityScore()).isBetween(0.0, 100.0);
                assertThat(sup.getActive()).isTrue();
            }

            // SKU assertions
            for (SKU sku : preview.getPreviewSkus()) {
                assertThat(sku.getSkuCode()).startsWith("SKU-SYN-");
                assertThat(sku.getName()).isNotBlank();
                assertThat(sku.getCategory()).isNotBlank();
                assertThat(sku.getUnitCost()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
                assertThat(sku.getSellingPrice()).isGreaterThan(sku.getUnitCost());
                assertThat(sku.getReorderPoint()).isGreaterThanOrEqualTo(sku.getSafetyStock());
                assertThat(sku.getSafetyStock()).isGreaterThan(0);
                assertThat(sku.getLeadTimeDays()).isGreaterThan(0);
                assertThat(sku.getActive()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Persistence Generation Tests")
    class PersistenceTests {

        @Test
        @DisplayName("Should persist entities via repository saveAll and return summary result")
        void testGenerateMasterDataPersistence() {
            when(locationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(supplierRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(skuRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            MasterDataGenerationResult result = generator.generateMasterData(defaultRequest);

            assertThat(result).isNotNull();
            assertThat(result.getLocationsSaved()).isEqualTo(3);
            assertThat(result.getSuppliersSaved()).isEqualTo(5);
            assertThat(result.getSkusSaved()).isEqualTo(10);
            assertThat(result.getTotalRecordsSaved()).isEqualTo(18);
            assertThat(result.getGeneratedLocationCodes()).hasSize(3);
            assertThat(result.getGeneratedSupplierCodes()).hasSize(5);
            assertThat(result.getGeneratedSkuCodes()).hasSize(10);
            assertThat(result.getExecutionDurationMs()).isGreaterThanOrEqualTo(0);

            verify(locationRepository, times(1)).saveAll(anyList());
            verify(supplierRepository, times(1)).saveAll(anyList());
            verify(skuRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("Should handle duplicate code collision safely by incrementing sequence")
        void testDuplicateCollisionAvoidance() {
            // Simulate LOC-SYN-001 and LOC-SYN-002 already existing in database
            when(locationRepository.existsByLocationCode("LOC-SYN-001")).thenReturn(true);
            when(locationRepository.existsByLocationCode("LOC-SYN-002")).thenReturn(true);
            when(locationRepository.existsByLocationCode("LOC-SYN-003")).thenReturn(false);
            when(locationRepository.existsByLocationCode("LOC-SYN-004")).thenReturn(false);

            when(locationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(supplierRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(skuRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            MasterDataGenerationRequest request = new MasterDataGenerationRequest(2, 1, 1, 42L);
            MasterDataGenerationResult result = generator.generateMasterData(request);

            assertThat(result.getGeneratedLocationCodes()).containsExactly("LOC-SYN-003", "LOC-SYN-004");
        }
    }

    @Nested
    @DisplayName("Validation and Statistics Tests")
    class ValidationAndStatsTests {

        @Test
        @DisplayName("Should throw BusinessValidationException for negative counts")
        void testNegativeCounts() {
            MasterDataGenerationRequest req = new MasterDataGenerationRequest(-1, 5, 10, 1L);
            assertThatThrownBy(() -> generator.previewMasterData(req))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Location count must be between 0 and 20");
        }

        @Test
        @DisplayName("Should throw BusinessValidationException for counts exceeding max limits")
        void testExceedingMaxCounts() {
            MasterDataGenerationRequest req = new MasterDataGenerationRequest(25, 5, 10, 1L);
            assertThatThrownBy(() -> generator.generateMasterData(req))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Location count must be between 0 and 20");
        }

        @Test
        @DisplayName("Should return accurate master data statistics")
        void testStatisticsQuery() {
            when(locationRepository.count()).thenReturn(10L);
            when(supplierRepository.count()).thenReturn(15L);
            when(skuRepository.count()).thenReturn(25L);

            when(locationRepository.countByLocationCodeStartingWith("LOC-SYN-")).thenReturn(7L);
            when(supplierRepository.countBySupplierCodeStartingWith("SUP-SYN-")).thenReturn(12L);
            when(skuRepository.countBySkuCodeStartingWith("SKU-SYN-")).thenReturn(22L);

            MasterDataStatisticsResponse stats = generator.getMasterDataStatistics();

            assertThat(stats.getTotalLocations()).isEqualTo(10L);
            assertThat(stats.getTotalSuppliers()).isEqualTo(15L);
            assertThat(stats.getTotalSkus()).isEqualTo(25L);
            assertThat(stats.getTotalSyntheticLocations()).isEqualTo(7L);
            assertThat(stats.getTotalSyntheticSuppliers()).isEqualTo(12L);
            assertThat(stats.getTotalSyntheticSkus()).isEqualTo(22L);
            assertThat(stats.getTotalMasterDataRecords()).isEqualTo(50L);
        }
    }
}

