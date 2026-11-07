-- ============================================================================
-- YottaFlex Supply Chain Planning System - Database Schema DDL
-- Database: MySQL 8.0+
-- Target Database Name: yottaflex_supply_chain
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `yottaflex_supply_chain` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `yottaflex_supply_chain`;

-- Disable foreign key checks during creation
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Locations
DROP TABLE IF EXISTS `locations`;
CREATE TABLE `locations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `location_code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `type` VARCHAR(30) NOT NULL,
    `address` VARCHAR(255) NULL,
    `city` VARCHAR(100) NULL,
    `state` VARCHAR(100) NULL,
    `country` VARCHAR(100) NULL,
    `postal_code` VARCHAR(20) NULL,
    `capacity` DECIMAL(14,2) NULL,
    `active` BIT(1) NOT NULL DEFAULT b'1',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    UNIQUE KEY `uk_location_code` (`location_code`),
    KEY `idx_location_type` (`type`),
    KEY `idx_location_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. SKUs (Stock Keeping Units)
DROP TABLE IF EXISTS `skus`;
CREATE TABLE `skus` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `sku_code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `description` VARCHAR(500) NULL,
    `category` VARCHAR(100) NOT NULL,
    `unit_of_measure` VARCHAR(20) NOT NULL DEFAULT 'UNITS',
    `unit_cost` DECIMAL(14,2) NOT NULL,
    `selling_price` DECIMAL(14,2) NOT NULL,
    `reorder_point` INT NOT NULL,
    `safety_stock` INT NOT NULL,
    `lead_time_days` INT NOT NULL,
    `active` BIT(1) NOT NULL DEFAULT b'1',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_sku_category` (`category`),
    KEY `idx_sku_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Suppliers
DROP TABLE IF EXISTS `suppliers`;
CREATE TABLE `suppliers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `supplier_code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `contact_person` VARCHAR(100) NULL,
    `email` VARCHAR(150) NULL,
    `phone` VARCHAR(50) NULL,
    `country` VARCHAR(100) NULL,
    `rating` DOUBLE NULL DEFAULT 5.0,
    `reliability_score` DOUBLE NULL DEFAULT 95.0,
    `active` BIT(1) NOT NULL DEFAULT b'1',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    UNIQUE KEY `uk_supplier_code` (`supplier_code`),
    KEY `idx_supplier_name` (`name`),
    KEY `idx_supplier_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Forecast Demands
DROP TABLE IF EXISTS `forecast_demands`;
CREATE TABLE `forecast_demands` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `sku_id` BIGINT NOT NULL,
    `location_id` BIGINT NOT NULL,
    `forecast_date` DATE NOT NULL,
    `period_type` VARCHAR(20) NOT NULL DEFAULT 'DAILY',
    `forecast_quantity` DECIMAL(14,2) NOT NULL,
    `actual_quantity` DECIMAL(14,2) NULL,
    `confidence_score` DOUBLE NULL DEFAULT 0.95,
    `demand_pattern` VARCHAR(30) NULL DEFAULT 'STABLE',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    KEY `idx_fd_sku_location_date` (`sku_id`, `location_id`, `forecast_date`),
    KEY `idx_fd_forecast_date` (`forecast_date`),
    CONSTRAINT `fk_forecast_sku` FOREIGN KEY (`sku_id`) REFERENCES `skus` (`id`),
    CONSTRAINT `fk_forecast_location` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Inventories
DROP TABLE IF EXISTS `inventories`;
CREATE TABLE `inventories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `sku_id` BIGINT NOT NULL,
    `location_id` BIGINT NOT NULL,
    `quantity_on_hand` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    `quantity_reserved` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    `quantity_available` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    `safety_stock_level` DECIMAL(14,2) NULL,
    `reorder_point` DECIMAL(14,2) NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'IN_STOCK',
    `last_audit_date` DATE NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    UNIQUE KEY `uk_inventory_sku_location` (`sku_id`, `location_id`),
    KEY `idx_inventory_sku` (`sku_id`),
    KEY `idx_inventory_location` (`location_id`),
    KEY `idx_inventory_status` (`status`),
    CONSTRAINT `fk_inventory_sku` FOREIGN KEY (`sku_id`) REFERENCES `skus` (`id`),
    CONSTRAINT `fk_inventory_location` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Supplier Capacities
DROP TABLE IF EXISTS `supplier_capacities`;
CREATE TABLE `supplier_capacities` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `supplier_id` BIGINT NOT NULL,
    `sku_id` BIGINT NOT NULL,
    `effective_start_date` DATE NOT NULL,
    `effective_end_date` DATE NOT NULL,
    `daily_capacity` DECIMAL(14,2) NOT NULL,
    `monthly_capacity` DECIMAL(14,2) NOT NULL,
    `allocated_capacity` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    `available_capacity` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    KEY `idx_sc_supplier_sku` (`supplier_id`, `sku_id`),
    KEY `idx_sc_dates` (`effective_start_date`, `effective_end_date`),
    CONSTRAINT `fk_supp_cap_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
    CONSTRAINT `fk_supp_cap_sku` FOREIGN KEY (`sku_id`) REFERENCES `skus` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Lead Times
DROP TABLE IF EXISTS `lead_times`;
CREATE TABLE `lead_times` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `supplier_id` BIGINT NOT NULL,
    `sku_id` BIGINT NOT NULL,
    `destination_location_id` BIGINT NOT NULL,
    `standard_days` INT NOT NULL,
    `expedited_days` INT NULL,
    `minimum_order_quantity` INT NULL DEFAULT 1,
    `expedited_cost_per_unit` DECIMAL(14,2) NULL,
    `transport_mode` VARCHAR(30) NOT NULL DEFAULT 'ROAD',
    `active` BIT(1) NOT NULL DEFAULT b'1',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    KEY `idx_lt_supplier_sku_dest` (`supplier_id`, `sku_id`, `destination_location_id`),
    CONSTRAINT `fk_lead_time_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
    CONSTRAINT `fk_lead_time_sku` FOREIGN KEY (`sku_id`) REFERENCES `skus` (`id`),
    CONSTRAINT `fk_lead_time_dest_location` FOREIGN KEY (`destination_location_id`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Purchase Orders
DROP TABLE IF EXISTS `purchase_orders`;
CREATE TABLE `purchase_orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `po_number` VARCHAR(50) NOT NULL,
    `supplier_id` BIGINT NOT NULL,
    `sku_id` BIGINT NOT NULL,
    `destination_location_id` BIGINT NOT NULL,
    `order_date` DATE NOT NULL,
    `expected_delivery_date` DATE NOT NULL,
    `actual_delivery_date` DATE NULL,
    `quantity_ordered` DECIMAL(14,2) NOT NULL,
    `quantity_received` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    `unit_price` DECIMAL(14,2) NOT NULL,
    `total_amount` DECIMAL(14,2) NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PLACED',
    `notes` VARCHAR(500) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    UNIQUE KEY `uk_po_number` (`po_number`),
    KEY `idx_po_supplier` (`supplier_id`),
    KEY `idx_po_sku` (`sku_id`),
    KEY `idx_po_destination` (`destination_location_id`),
    KEY `idx_po_status` (`status`),
    KEY `idx_po_order_date` (`order_date`),
    CONSTRAINT `fk_po_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
    CONSTRAINT `fk_po_sku` FOREIGN KEY (`sku_id`) REFERENCES `skus` (`id`),
    CONSTRAINT `fk_po_destination_location` FOREIGN KEY (`destination_location_id`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Production Plans
DROP TABLE IF EXISTS `production_plans`;
CREATE TABLE `production_plans` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `plan_code` VARCHAR(50) NOT NULL,
    `sku_id` BIGINT NOT NULL,
    `location_id` BIGINT NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `planned_quantity` DECIMAL(14,2) NOT NULL,
    `produced_quantity` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    `capacity_utilization` DOUBLE NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    `notes` VARCHAR(500) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    UNIQUE KEY `uk_plan_code` (`plan_code`),
    KEY `idx_pp_sku` (`sku_id`),
    KEY `idx_pp_location` (`location_id`),
    KEY `idx_pp_status` (`status`),
    KEY `idx_pp_dates` (`start_date`, `end_date`),
    CONSTRAINT `fk_prod_plan_sku` FOREIGN KEY (`sku_id`) REFERENCES `skus` (`id`),
    CONSTRAINT `fk_prod_plan_location` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Shortages
DROP TABLE IF EXISTS `shortages`;
CREATE TABLE `shortages` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `shortage_code` VARCHAR(50) NOT NULL,
    `sku_id` BIGINT NOT NULL,
    `location_id` BIGINT NOT NULL,
    `shortage_date` DATE NOT NULL,
    `shortage_quantity` DECIMAL(14,2) NOT NULL,
    `severity` VARCHAR(30) NOT NULL DEFAULT 'HIGH',
    `root_cause` VARCHAR(50) NOT NULL DEFAULT 'DEMAND_SURGE',
    `resolved` BIT(1) NOT NULL DEFAULT b'0',
    `resolution_date` DATE NULL,
    `notes` VARCHAR(500) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    UNIQUE KEY `uk_shortage_code` (`shortage_code`),
    KEY `idx_shortage_sku` (`sku_id`),
    KEY `idx_shortage_location` (`location_id`),
    KEY `idx_shortage_severity` (`severity`),
    KEY `idx_shortage_resolved` (`resolved`),
    KEY `idx_shortage_date` (`shortage_date`),
    CONSTRAINT `fk_shortage_sku` FOREIGN KEY (`sku_id`) REFERENCES `skus` (`id`),
    CONSTRAINT `fk_shortage_location` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Mitigation Options
DROP TABLE IF EXISTS `mitigation_options`;
CREATE TABLE `mitigation_options` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `option_code` VARCHAR(50) NOT NULL,
    `shortage_id` BIGINT NOT NULL,
    `strategy` VARCHAR(40) NOT NULL,
    `alternate_supplier_id` BIGINT NULL,
    `source_location_id` BIGINT NULL,
    `cost_impact` DECIMAL(14,2) NULL DEFAULT 0.00,
    `time_impact_days` INT NULL DEFAULT 0,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PROPOSED',
    `recommendation_score` DOUBLE NULL DEFAULT 80.0,
    `details` VARCHAR(1000) NULL,
    `mitigated_quantity` DECIMAL(14,2) NULL,
    `purchase_order_id` BIGINT NULL,
    `production_plan_id` BIGINT NULL,
    `estimated_arrival_date` DATE NULL,
    `freight_mode` VARCHAR(30) NULL,
    `split_remaining_quantity` DECIMAL(14,2) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    UNIQUE KEY `uk_option_code` (`option_code`),
    KEY `idx_opt_shortage` (`shortage_id`),
    KEY `idx_opt_strategy` (`strategy`),
    KEY `idx_opt_status` (`status`),
    KEY `idx_opt_po` (`purchase_order_id`),
    KEY `idx_opt_prod_plan` (`production_plan_id`),
    CONSTRAINT `fk_mitigation_shortage` FOREIGN KEY (`shortage_id`) REFERENCES `shortages` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_mitigation_supplier` FOREIGN KEY (`alternate_supplier_id`) REFERENCES `suppliers` (`id`),
    CONSTRAINT `fk_mitigation_source_location` FOREIGN KEY (`source_location_id`) REFERENCES `locations` (`id`),
    CONSTRAINT `fk_mitigation_purchase_order` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_mitigation_production_plan` FOREIGN KEY (`production_plan_id`) REFERENCES `production_plans` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

