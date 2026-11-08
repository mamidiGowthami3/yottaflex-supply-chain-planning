-- ============================================================================
-- Migration v2: Structured Tactical Mitigation Execution Fields
-- Project: YottaFlex Supply Chain Planning System
-- Workspace: D:\supply_chain
-- Description: Adds structured operational fields for purchase orders, production
--              plans, arrival dates, transport modes, and split quantities.
-- Preserves existing 29 records with NULL values in newly introduced columns.
-- ============================================================================

ALTER TABLE `mitigation_options`
    ADD COLUMN `mitigated_quantity` DECIMAL(14,2) NULL DEFAULT NULL AFTER `details`,
    ADD COLUMN `purchase_order_id` BIGINT NULL DEFAULT NULL AFTER `mitigated_quantity`,
    ADD COLUMN `production_plan_id` BIGINT NULL DEFAULT NULL AFTER `purchase_order_id`,
    ADD COLUMN `estimated_arrival_date` DATE NULL DEFAULT NULL AFTER `production_plan_id`,
    ADD COLUMN `freight_mode` VARCHAR(30) NULL DEFAULT NULL AFTER `estimated_arrival_date`,
    ADD COLUMN `split_remaining_quantity` DECIMAL(14,2) NULL DEFAULT NULL AFTER `freight_mode`;

ALTER TABLE `mitigation_options`
    ADD CONSTRAINT `fk_mitigation_purchase_order` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id`) ON DELETE SET NULL,
    ADD CONSTRAINT `fk_mitigation_production_plan` FOREIGN KEY (`production_plan_id`) REFERENCES `production_plans` (`id`) ON DELETE SET NULL;

CREATE INDEX `idx_opt_po` ON `mitigation_options` (`purchase_order_id`);
CREATE INDEX `idx_opt_prod_plan` ON `mitigation_options` (`production_plan_id`);

