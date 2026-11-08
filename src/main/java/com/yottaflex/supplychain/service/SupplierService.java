package com.yottaflex.supplychain.service;

import com.yottaflex.supplychain.entity.Supplier;
import com.yottaflex.supplychain.exception.DuplicateResourceException;
import com.yottaflex.supplychain.exception.ResourceNotFoundException;
import com.yottaflex.supplychain.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public List<Supplier> findActive() {
        return supplierRepository.findByActiveTrue();
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }

    public Supplier findBySupplierCode(String code) {
        return supplierRepository.findBySupplierCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "supplierCode", code));
    }

    @Transactional
    public Supplier create(Supplier supplier) {
        if (supplierRepository.existsBySupplierCode(supplier.getSupplierCode())) {
            throw new DuplicateResourceException("Supplier", "supplierCode", supplier.getSupplierCode());
        }
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Long id, Supplier updatedSupplier) {
        Supplier existing = findById(id);
        if (!existing.getSupplierCode().equals(updatedSupplier.getSupplierCode()) && supplierRepository.existsBySupplierCode(updatedSupplier.getSupplierCode())) {
            throw new DuplicateResourceException("Supplier", "supplierCode", updatedSupplier.getSupplierCode());
        }
        existing.setSupplierCode(updatedSupplier.getSupplierCode());
        existing.setName(updatedSupplier.getName());
        existing.setContactPerson(updatedSupplier.getContactPerson());
        existing.setEmail(updatedSupplier.getEmail());
        existing.setPhone(updatedSupplier.getPhone());
        existing.setCountry(updatedSupplier.getCountry());
        existing.setRating(updatedSupplier.getRating());
        existing.setReliabilityScore(updatedSupplier.getReliabilityScore());
        existing.setActive(updatedSupplier.getActive());
        return supplierRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Supplier existing = findById(id);
        supplierRepository.delete(existing);
    }
}

