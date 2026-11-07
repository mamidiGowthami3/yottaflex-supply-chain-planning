package com.yottaflex.supplychain.service;

import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.exception.DuplicateResourceException;
import com.yottaflex.supplychain.exception.ResourceNotFoundException;
import com.yottaflex.supplychain.repository.SKURepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkuServiceTest {

    @Mock
    private SKURepository skuRepository;

    @InjectMocks
    private SkuService skuService;

    private SKU testSku;

    @BeforeEach
    void setUp() {
        testSku = new SKU(
                "SKU-1001",
                "Industrial Valve 50mm",
                "Valves",
                new BigDecimal("45.50"),
                new BigDecimal("79.99"),
                50,
                20,
                7
        );
        testSku.setId(1L);
    }

    @Test
    @DisplayName("Should return all active SKUs")
    void testFindActiveSkus() {
        when(skuRepository.findByActiveTrue()).thenReturn(List.of(testSku));

        List<SKU> activeSkus = skuService.findActive();

        assertThat(activeSkus).hasSize(1);
        assertThat(activeSkus.get(0).getSkuCode()).isEqualTo("SKU-1001");
        verify(skuRepository, times(1)).findByActiveTrue();
    }

    @Test
    @DisplayName("Should find SKU by ID")
    void testFindByIdSuccess() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));

        SKU found = skuService.findById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Industrial Valve 50mm");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when SKU ID does not exist")
    void testFindByIdNotFound() {
        when(skuRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skuService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SKU not found with id: '999'");
    }

    @Test
    @DisplayName("Should create new SKU when code is unique")
    void testCreateSkuSuccess() {
        when(skuRepository.existsBySkuCode("SKU-1001")).thenReturn(false);
        when(skuRepository.save(any(SKU.class))).thenReturn(testSku);

        SKU created = skuService.create(testSku);

        assertThat(created).isNotNull();
        assertThat(created.getSkuCode()).isEqualTo("SKU-1001");
        verify(skuRepository, times(1)).save(testSku);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating duplicate SKU code")
    void testCreateSkuDuplicateCode() {
        when(skuRepository.existsBySkuCode("SKU-1001")).thenReturn(true);

        assertThatThrownBy(() -> skuService.create(testSku))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SKU already exists with skuCode: 'SKU-1001'");

        verify(skuRepository, never()).save(any(SKU.class));
    }
}

