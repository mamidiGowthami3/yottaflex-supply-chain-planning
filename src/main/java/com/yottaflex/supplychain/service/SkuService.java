package com.yottaflex.supplychain.service;

import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.exception.DuplicateResourceException;
import com.yottaflex.supplychain.exception.ResourceNotFoundException;
import com.yottaflex.supplychain.repository.SKURepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SkuService {

    private final SKURepository skuRepository;

    public SkuService(SKURepository skuRepository) {
        this.skuRepository = skuRepository;
    }

    public List<SKU> findAll() {
        return skuRepository.findAll();
    }

    public List<SKU> findActive() {
        return skuRepository.findByActiveTrue();
    }

    public SKU findById(Long id) {
        return skuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SKU", "id", id));
    }

    public SKU findBySkuCode(String skuCode) {
        return skuRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("SKU", "skuCode", skuCode));
    }

    @Transactional
    public SKU create(SKU sku) {
        if (skuRepository.existsBySkuCode(sku.getSkuCode())) {
            throw new DuplicateResourceException("SKU", "skuCode", sku.getSkuCode());
        }
        return skuRepository.save(sku);
    }

    @Transactional
    public SKU update(Long id, SKU updatedSku) {
        SKU existing = findById(id);
        if (!existing.getSkuCode().equals(updatedSku.getSkuCode()) && skuRepository.existsBySkuCode(updatedSku.getSkuCode())) {
            throw new DuplicateResourceException("SKU", "skuCode", updatedSku.getSkuCode());
        }
        existing.setSkuCode(updatedSku.getSkuCode());
        existing.setName(updatedSku.getName());
        existing.setDescription(updatedSku.getDescription());
        existing.setCategory(updatedSku.getCategory());
        existing.setUnitOfMeasure(updatedSku.getUnitOfMeasure());
        existing.setUnitCost(updatedSku.getUnitCost());
        existing.setSellingPrice(updatedSku.getSellingPrice());
        existing.setReorderPoint(updatedSku.getReorderPoint());
        existing.setSafetyStock(updatedSku.getSafetyStock());
        existing.setLeadTimeDays(updatedSku.getLeadTimeDays());
        existing.setActive(updatedSku.getActive());
        return skuRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        SKU existing = findById(id);
        skuRepository.delete(existing);
    }
}

