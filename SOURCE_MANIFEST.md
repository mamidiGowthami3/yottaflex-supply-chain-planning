# YottaFlex Supply Chain System – Source Manifest & Data Dictionary

## 1. System Metadata

- **System Name**: YottaFlex Supply Chain Planning and Synthetic Data Generation System
- **Version**: `1.0.0-SNAPSHOT`
- **Specification Compliance**: YottaFlex Challenge Milestone 2 Requirements
- **Deterministic Generation Seed**: `20261022L`
- **Output Directories**:
  - `exports/csv/` – RFC 4180 compliant CSV exports
  - `exports/json/` – Standard JSON exports
  - `exports/sql/` – Deterministic SQL insert dumps (`challenge_dataset_insert.sql`)
  - `exports/manifest.json` – Machine-readable metadata with SHA-256 cryptographic checksums
  - `data/ground_truth/` – Canonical baseline verification dataset
  - `data/edge_cases/` – 5 extreme supply chain stress testing datasets

---

## 2. Dataset Scale & Volume Specification

| Entity | Primary Key / Unique Identifier | Required Scale | Generated Scale | Format Support |
| :--- | :--- | :--- | :--- | :--- |
| **Locations** | `LOC-0001` .. `LOC-0050` | 50 | **50** | CSV, JSON, SQL |
| **Suppliers** | `SUP-0001` .. `SUP-0500` | 500 | **500** | CSV, JSON, SQL |
| **SKUs** | `SKU-0001` .. `SKU-3000` | 3,000 | **3,000** | CSV, JSON, SQL |
| **Forecast Demand** | Weekly (52 weeks x sample SKUs) | 52 weeks | **5,200 records (52 weeks)** | CSV, JSON, SQL |
| **Purchase Orders** | `PO-00001` .. `PO-10000` | 10,000 | **10,000** | CSV, JSON, SQL |
| **Inventories** | SKU x Location stocking nodes | 3,000+ | **3,000** | CSV, JSON, SQL |
| **Supplier Capacity** | Supplier x SKU quotas | 1,500+ | **1,500** | CSV, JSON, SQL |
| **Lead Times** | Supplier x SKU x Destination | 1,500+ | **1,500** | CSV, JSON, SQL |
| **Production Plans** | `PP-0001` .. `PP-0500` | 500 | **500** (Plants only) | CSV, JSON, SQL |
| **Shortages** | `SH-0001` .. `SH-0100` | 100+ | **100** | CSV, JSON, SQL |
| **Mitigation Options** | `MIT-0001` .. `MIT-0200` | 200+ | **200** | CSV, JSON, SQL |

---

## 3. Data Dictionary & Field Specifications

### 3.1 Location (`locations.csv`, `locations.json`)
- `id` (BIGINT): Unique database primary key.
- `location_code` (VARCHAR(50), UNIQUE): Canonical facility code (e.g. `LOC-0001`).
- `name` (VARCHAR(150)): Human-readable descriptive name (e.g. `Chicago Manufacturing Plant`).
- `type` (VARCHAR(30)): Facility classification: `PLANT`, `WAREHOUSE`, `DISTRIBUTION_CENTER`.
- `city` (VARCHAR(100)): Municipality location.
- `state` (VARCHAR(50)): Two-letter US state or regional identifier.
- `country` (VARCHAR(50)): Country code (e.g. `USA`).
- `postal_code` (VARCHAR(20)): Postal routing code.
- `address` (VARCHAR(255)): Street address.
- `capacity` (DECIMAL(12,2)): Facility throughput capacity.
- `active` (BOOLEAN): Operational status flag.

### 3.2 Supplier (`suppliers.csv`, `suppliers.json`)
- `id` (BIGINT): Unique database primary key.
- `supplier_code` (VARCHAR(50), UNIQUE): Canonical vendor identifier (e.g. `SUP-0001`).
- `name` (VARCHAR(150)): Legal corporate enterprise name.
- `contact_person` (VARCHAR(100)): Account representative or operations director.
- `email` (VARCHAR(100)): Contact email address.
- `phone` (VARCHAR(50)): International telephone contact.
- `country` (VARCHAR(50)): Primary corporate registry nation.
- `rating` (DECIMAL(3,1)): Supplier rating on a 1.0 to 5.0 scale.
- `reliability_score` (DECIMAL(5,2)): Historical on-time delivery reliability percentage (0.00% to 100.00%).
- `active` (BOOLEAN): Operational status flag.

### 3.3 SKU (`skus.csv`, `skus.json`)
- `id` (BIGINT): Unique database primary key.
- `sku_code` (VARCHAR(50), UNIQUE): Canonical item master code (e.g. `SKU-0001`).
- `name` (VARCHAR(150)): Item nomenclature.
- `description` (TEXT): Engineering technical description.
- `category` (VARCHAR(50)): Domain classification (Electronics, Raw Materials, Packaging, etc.).
- `unit_of_measure` (VARCHAR(20)): Inventory quantification unit (`UNITS`, `KG`, `METERS`, `LITERS`).
- `unit_cost` (DECIMAL(10,2)): Base procurement acquisition cost.
- `sellingPrice` (DECIMAL(10,2)): Benchmark list price.
- `reorder_point` (INT): Inventory threshold triggering replenishment.
- `safety_stock` (INT): Buffer threshold guarding against stochastic variance.
- `lead_time_days` (INT): Nominal supplier lead time in days.
- `active` (BOOLEAN): Active catalog flag.

### 3.4 Forecast Demand (`forecast_demands.csv`, `forecast_demands.json`)
- `id` (BIGINT): Unique database primary key.
- `sku_code` (VARCHAR(50)): Target item foreign reference.
- `location_code` (VARCHAR(50)): Target destination facility reference.
- `forecast_date` (DATE): Monday start date of the demand week (ISO 8601).
- `period_type` (VARCHAR(20)): Bucket periodicity (`WEEKLY`).
- `forecast_quantity` (DECIMAL(10,2)): Projected gross customer demand.
- `confidence_score` (DECIMAL(4,2)): Statistical certainty index (0.00 to 1.00).
- `demand_pattern` (VARCHAR(30)): Demand profile classification (`STABLE`, `SEASONAL`, `TRENDING`).

### 3.5 Inbound Purchase Order (`purchase_orders.csv`, `purchase_orders.json`)
- `id` (BIGINT): Unique database primary key.
- `po_number` (VARCHAR(50), UNIQUE): Canonical purchase order number (e.g. `PO-00001`).
- `supplier_code` (VARCHAR(50)): Issuing supplier code.
- `sku_code` (VARCHAR(50)): Procured SKU identifier.
- `destination_location_code` (VARCHAR(50)): Receiving facility code.
- `order_date` (DATE): Purchase order commitment date.
- `expected_delivery_date` (DATE): Scheduled dock arrival date.
- `actual_delivery_date` (DATE): Realized receiving date (null for open orders).
- `quantity_ordered` (DECIMAL(10,2)): Total contracted procurement quantity.
- `quantity_received` (DECIMAL(10,2)): Quantity received into inventory.
- `unit_price` (DECIMAL(10,2)): Negotiated purchase unit price.
- `status` (VARCHAR(30)): Lifecycle state (`PLACED`, `IN_TRANSIT`, `PARTIALLY_DELIVERED`, `DELIVERED`, `CANCELLED`).
- `notes` (TEXT): Transactional procurement remarks.

### 3.6 Inventory (`inventories.csv`, `inventories.json`)
- `id` (BIGINT): Unique database primary key.
- `sku_code` (VARCHAR(50)): Associated SKU.
- `location_code` (VARCHAR(50)): Associated facility.
- `quantity_on_hand` (DECIMAL(10,2)): Physical stock on hand.
- `quantity_reserved` (DECIMAL(10,2)): Allocated/locked stock.
- `quantity_available` (DECIMAL(10,2)): Free uncommitted stock (`quantityOnHand - quantityReserved`).
- `safety_stock_level` (DECIMAL(10,2)): Safety buffer threshold.
- `reorder_point` (DECIMAL(10,2)): Replenishment trigger level.
- `status` (VARCHAR(30)): Health status (`HEALTHY`, `NORMAL`, `LOW_STOCK`, `STOCKOUT`).
- `last_audit_date` (DATE): Last physical cycle count audit.

### 3.7 Supplier Capacity (`supplier_capacities.csv`, `supplier_capacities.json`)
- `id` (BIGINT): Unique database primary key.
- `supplier_code` (VARCHAR(50)): Vendor code.
- `sku_code` (VARCHAR(50)): Component code.
- `effective_start_date` (DATE): Active capacity window start.
- `effective_end_date` (DATE): Active capacity window end.
- `daily_capacity` (DECIMAL(10,2)): Maximum daily production output.
- `monthly_capacity` (DECIMAL(10,2)): Maximum monthly production quota.
- `allocated_capacity` (DECIMAL(10,2)): Committed volume to ongoing orders.
- `available_capacity` (DECIMAL(10,2)): Remaining uncommitted production headroom.

### 3.8 Lead Time (`lead_times.csv`, `lead_times.json`)
- `id` (BIGINT): Unique database primary key.
- `supplier_code` (VARCHAR(50)): Origin vendor.
- `sku_code` (VARCHAR(50)): Item.
- `destination_location_code` (VARCHAR(50)): Destination receiving facility.
- `standard_days` (INT): Baseline economy transit duration.
- `expedited_days` (INT): Accelerated express transit duration.
- `minimum_order_quantity` (INT): Minimum lot size batch constraint.
- `expedited_cost_per_unit` (DECIMAL(10,2)): Express freight surcharge per unit.
- `transport_mode` (VARCHAR(30)): Logistics freight modality (`ROAD`, `AIR`, `OCEAN`, `RAIL`).

### 3.9 Production Plan (`production_plans.csv`, `production_plans.json`)
- `id` (BIGINT): Unique database primary key.
- `plan_code` (VARCHAR(50), UNIQUE): Canonical manufacturing schedule code.
- `sku_code` (VARCHAR(50)): Manufactured end-item SKU.
- `location_code` (VARCHAR(50)): Facility code (strictly validated to `LocationType.PLANT`).
- `start_date` (DATE): Scheduled shop-floor start date.
- `end_date` (DATE): Target completion date.
- `planned_quantity` (DECIMAL(10,2)): Target manufacturing batch quantity.
- `produced_quantity` (DECIMAL(10,2)): Realized manufacturing output.
- `capacity_utilization` (DECIMAL(5,2)): Facility production load utilization percentage.
- `status` (VARCHAR(30)): Schedule status (`DRAFT`, `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`).
- `notes` (TEXT): Engineering shift notes.

### 3.10 Shortage (`shortages.csv`, `shortages.json`)
- `id` (BIGINT): Unique database primary key.
- `shortage_code` (VARCHAR(50), UNIQUE): Canonical deficit identifier (e.g. `SH-0001`).
- `sku_code` (VARCHAR(50)): Deficit item.
- `location_code` (VARCHAR(50)): Deficit fulfillment site.
- `shortage_date` (DATE): Projected date of inventory breach.
- `shortage_quantity` (DECIMAL(10,2)): Unfulfilled deficit quantity below safety threshold.
- `severity` (VARCHAR(30)): Criticality rating (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
- `root_cause` (VARCHAR(50)): Root cause classification (`DEMAND_SURGE`, `SUPPLIER_DELAY`, `CAPACITY_BOTTLENECK`, `INSUFFICIENT_INITIAL_INVENTORY`, `UNEXPECTED_SCRAP`).
- `resolved` (BOOLEAN): Deficit resolution indicator.
- `resolution_date` (DATE): Date resolution confirmed.
- `notes` (TEXT): Operational diagnostic details.

### 3.11 Mitigation Option (`mitigation_options.csv`, `mitigation_options.json`)
- `id` (BIGINT): Unique database primary key.
- `option_code` (VARCHAR(50), UNIQUE): Proposal identifier (e.g. `MIT-0001`).
- `shortage_code` (VARCHAR(50)): Associated shortage reference.
- `strategy` (VARCHAR(50)): Tactical countermeasure:
  - `REALLOCATE_INVENTORY`
  - `EXPEDITE_PURCHASE_ORDER`
  - `ADJUST_PRODUCTION_SCHEDULE`
  - `SPLIT_SHIPMENT`
  - `ALTERNATIVE_SUPPLIER`
- `alternate_supplier_code` (VARCHAR(50)): Secondary supplier reference.
- `source_location_code` (VARCHAR(50)): Donor facility for stock transfers.
- `purchase_order_number` (VARCHAR(50)): Target open purchase order.
- `production_plan_code` (VARCHAR(50)): Target production schedule.
- `cost_impact` (DECIMAL(10,2)): Incremental financial surcharge.
- `time_impact_days` (INT): Lead time delta or turnaround duration.
- `status` (VARCHAR(30)): Lifecycle state (`PROPOSED`, `APPROVED`, `REJECTED`, `EXECUTED`).
- `recommendation_score` (DECIMAL(5,2)): Normalized multi-criteria score (0.00 to 100.00).
- `mitigated_quantity` (DECIMAL(10,2)): Exact volume resolved by this recommendation.
- `estimated_arrival_date` (DATE): Projected arrival date at shortage site.
- `freight_mode` (VARCHAR(30)): Selected freight mode (`AIR`, `ROAD`, `OCEAN`, `RAIL`).
- `split_remaining_quantity` (DECIMAL(10,2)): Remaining un-expedited volume for split orders.
- `details` (TEXT): Complete execution-ready rationale and operational guidelines.

