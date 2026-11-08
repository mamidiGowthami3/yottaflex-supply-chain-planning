-- ============================================================================
-- YottaFlex Supply Chain Planning System - Safe Sample Demonstration Data
-- All records are synthetic mock data for demonstration and validation.
-- Contains zero private credentials or sensitive production data.
-- ============================================================================

USE `yottaflex_supply_chain`;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Locations
INSERT INTO `locations` (`id`, `location_code`, `name`, `type`, `address`, `city`, `state`, `country`, `postal_code`, `capacity`, `active`, `created_at`, `updated_at`)
VALUES
(1, 'LOC-DC-01', 'Chicago Central Distribution Center', 'DISTRIBUTION_CENTER', '1000 Logistics Blvd', 'Chicago', 'IL', 'USA', '60601', 50000.00, b'1', NOW(), NOW()),
(2, 'LOC-PLANT-01', 'Detroit Advanced Manufacturing Plant', 'PLANT', '400 Industrial Pkwy', 'Detroit', 'MI', 'USA', '48201', 75000.00, b'1', NOW(), NOW()),
(3, 'LOC-WH-01', 'Dallas Regional Fulfillment Warehouse', 'WAREHOUSE', '220 Cargo Way', 'Dallas', 'TX', 'USA', '75201', 30000.00, b'1', NOW(), NOW())
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 2. SKUs
INSERT INTO `skus` (`id`, `sku_code`, `name`, `description`, `category`, `unit_of_measure`, `unit_cost`, `selling_price`, `reorder_point`, `safety_stock`, `lead_time_days`, `active`, `created_at`, `updated_at`)
VALUES
(1, 'SKU-VLV-101', 'High-Pressure Hydraulic Valve 50mm', 'Precision engineered hydraulic control valve', 'Hydraulics', 'UNITS', 45.00, 89.99, 100, 40, 10, b'1', NOW(), NOW()),
(2, 'SKU-SNS-202', 'Industrial Optical Pressure Sensor', 'Digital telemetry pressure sensor with CAN-bus interface', 'Electronics', 'UNITS', 18.50, 42.00, 200, 80, 7, b'1', NOW(), NOW()),
(3, 'SKU-ACT-303', 'Linear Electric Actuator 24V', 'High-thrust linear actuator for automated assembly lines', 'Actuators', 'UNITS', 120.00, 240.00, 50, 20, 14, b'1', NOW(), NOW())
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 3. Suppliers
INSERT INTO `suppliers` (`id`, `supplier_code`, `name`, `contact_person`, `email`, `phone`, `country`, `rating`, `reliability_score`, `active`, `created_at`, `updated_at`)
VALUES
(1, 'SUP-APEX-01', 'Apex Precision Engineering Corp', 'Sarah Jenkins', 'sarah.jenkins@example.com', '+1-555-019-2831', 'USA', 4.8, 97.5, b'1', NOW(), NOW()),
(2, 'SUP-GLOBAL-02', 'Global Micro-Sensors Ltd', 'Kenji Takahashi', 'k.takahashi@example.com', '+81-3-5555-0143', 'Japan', 4.9, 98.2, b'1', NOW(), NOW()),
(3, 'SUP-VORTEX-03', 'Vortex Motion Technologies GmbH', 'Lukas Weber', 'lukas.weber@example.com', '+49-89-5555-0189', 'Germany', 4.6, 94.0, b'1', NOW(), NOW())
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 4. Inventories
INSERT INTO `inventories` (`id`, `sku_id`, `location_id`, `quantity_on_hand`, `quantity_reserved`, `quantity_available`, `safety_stock_level`, `reorder_point`, `status`, `last_audit_date`, `created_at`, `updated_at`)
VALUES
(1, 1, 1, 250.00, 50.00, 200.00, 40.00, 100.00, 'IN_STOCK', CURDATE(), NOW(), NOW()),
(2, 2, 1, 95.00, 20.00, 75.00, 80.00, 200.00, 'LOW_STOCK', CURDATE(), NOW(), NOW()),
(3, 3, 2, 18.00, 10.00, 8.00, 20.00, 50.00, 'LOW_STOCK', CURDATE(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `quantity_on_hand`=VALUES(`quantity_on_hand`);

-- 5. Lead Times
INSERT INTO `lead_times` (`id`, `supplier_id`, `sku_id`, `destination_location_id`, `standard_days`, `expedited_days`, `minimum_order_quantity`, `expedited_cost_per_unit`, `transport_mode`, `active`, `created_at`, `updated_at`)
VALUES
(1, 1, 1, 1, 10, 4, 25, 8.50, 'ROAD', b'1', NOW(), NOW()),
(2, 2, 2, 1, 7, 3, 50, 4.00, 'AIR', b'1', NOW(), NOW()),
(3, 3, 3, 2, 14, 6, 10, 25.00, 'AIR', b'1', NOW(), NOW())
ON DUPLICATE KEY UPDATE `standard_days`=VALUES(`standard_days`);

-- 6. Supplier Capacities
INSERT INTO `supplier_capacities` (`id`, `supplier_id`, `sku_id`, `effective_start_date`, `effective_end_date`, `daily_capacity`, `monthly_capacity`, `allocated_capacity`, `available_capacity`, `created_at`, `updated_at`)
VALUES
(1, 1, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY), 100.00, 3000.00, 800.00, 2200.00, NOW(), NOW()),
(2, 2, 2, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY), 300.00, 9000.00, 3200.00, 5800.00, NOW(), NOW()),
(3, 3, 3, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY), 50.00, 1500.00, 450.00, 1050.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE `daily_capacity`=VALUES(`daily_capacity`);

-- 7. Forecast Demands
INSERT INTO `forecast_demands` (`id`, `sku_id`, `location_id`, `forecast_date`, `period_type`, `forecast_quantity`, `actual_quantity`, `confidence_score`, `demand_pattern`, `created_at`, `updated_at`)
VALUES
(1, 1, 1, CURDATE(), 'DAILY', 45.00, NULL, 0.94, 'STABLE', NOW(), NOW()),
(2, 2, 1, CURDATE(), 'DAILY', 110.00, NULL, 0.91, 'SEASONAL', NOW(), NOW()),
(3, 3, 2, CURDATE(), 'DAILY', 15.00, NULL, 0.96, 'TRENDING', NOW(), NOW())
ON DUPLICATE KEY UPDATE `forecast_quantity`=VALUES(`forecast_quantity`);

-- 8. Purchase Orders
INSERT INTO `purchase_orders` (`id`, `po_number`, `supplier_id`, `sku_id`, `destination_location_id`, `order_date`, `expected_delivery_date`, `actual_delivery_date`, `quantity_ordered`, `quantity_received`, `unit_price`, `total_amount`, `status`, `notes`, `created_at`, `updated_at`)
VALUES
(1, 'PO-2026-0001', 1, 1, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 DAY), NULL, 150.00, 0.00, 45.00, 6750.00, 'PLACED', 'Standard replenishment order', NOW(), NOW())
ON DUPLICATE KEY UPDATE `status`=VALUES(`status`);

-- 9. Production Plans
INSERT INTO `production_plans` (`id`, `plan_code`, `sku_id`, `location_id`, `start_date`, `end_date`, `planned_quantity`, `produced_quantity`, `capacity_utilization`, `status`, `notes`, `created_at`, `updated_at`)
VALUES
(1, 'PLAN-2026-W45', 3, 2, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 120.00, 30.00, 85.0, 'IN_PROGRESS', 'Weekly actuator assembly cycle', NOW(), NOW())
ON DUPLICATE KEY UPDATE `status`=VALUES(`status`);

-- 10. Shortages
INSERT INTO `shortages` (`id`, `shortage_code`, `sku_id`, `location_id`, `shortage_date`, `shortage_quantity`, `severity`, `root_cause`, `resolved`, `resolution_date`, `notes`, `created_at`, `updated_at`)
VALUES
(1, 'SHRT-2026-001', 2, 1, CURDATE(), 35.00, 'HIGH', 'DEMAND_SURGE', b'0', NULL, 'Sudden influx of sensor orders exceeded available buffer', NOW(), NOW())
ON DUPLICATE KEY UPDATE `shortage_quantity`=VALUES(`shortage_quantity`);

-- 11. Mitigation Options
INSERT INTO `mitigation_options` (`id`, `option_code`, `shortage_id`, `strategy`, `alternate_supplier_id`, `source_location_id`, `cost_impact`, `time_impact_days`, `status`, `recommendation_score`, `details`, `created_at`, `updated_at`)
VALUES
(1, 'MIT-OPT-001A', 1, 'EXPEDITE_PURCHASE_ORDER', 2, NULL, 140.00, 3, 'APPROVED', 92.5, 'Expedite air freight from Global Micro-Sensors', NOW(), NOW())
ON DUPLICATE KEY UPDATE `status`=VALUES(`status`);

SET FOREIGN_KEY_CHECKS = 1;

