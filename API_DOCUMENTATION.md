# YottaFlex Supply Chain System – REST API Documentation

Base URL: `http://localhost:8080`  
API Prefix: `/api/v1`

All responses follow the standard `ApiResponse<T>` envelope:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-11-07T12:00:00"
}
```

---

## 1. System Health & Diagnostics

### 1.1 Service Health Check
- **Endpoint**: `GET /api/v1/health`
- **Description**: Returns the real-time operational status of the service and database connectivity.
- **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Service is healthy and fully operational",
  "data": {
    "status": "UP",
    "timestamp": "2026-11-07T12:00:00",
    "version": "1.0.0-SNAPSHOT",
    "database": "UP"
  }
}
```

---

## 2. Master Data Management

### 2.1 Generate Master Data
- **Endpoint**: `POST /api/v1/generator/master`
- **Request Parameters**:
  - `locations` (int, default: 20): Number of locations.
  - `suppliers` (int, default: 50): Number of suppliers.
  - `skus` (int, default: 100): Number of SKUs.
  - `seed` (long, default: 20261022): Deterministic pseudo-random seed.
- **Description**: Generates baseline master data records (Locations, Suppliers, SKUs).

### 2.2 Query Locations
- **Endpoint**: `GET /api/v1/locations`
- **Description**: Retrieves all configured facilities (Plants, Warehouses, Distribution Centers).

### 2.3 Query Suppliers
- **Endpoint**: `GET /api/v1/suppliers`
- **Description**: Retrieves all vendor profiles with reliability scores.

### 2.4 Query SKUs
- **Endpoint**: `GET /api/v1/skus`
- **Description**: Retrieves catalog item masters with lead time and safety stock thresholds.

---

## 3. Dependent Data Management

### 3.1 Generate Dependent Data
- **Endpoint**: `POST /api/v1/generator/dependent`
- **Request Parameters**:
  - `inventoryPerSku` (int, default: 2): Inventories per SKU.
  - `demandWeeks` (int, default: 12): Weekly forecast horizons.
  - `seed` (long, default: 20261022): Deterministic seed.
- **Description**: Generates linked Inventory balances, Forecast Demand profiles, Supplier Capacities, and Lead Times.

### 3.2 Dependent Data Generation Status
- **Endpoint**: `GET /api/v1/generator/dependent/status`
- **Description**: Returns count statistics of dependent data records.

---

## 4. Planning Data Management

### 4.1 Generate Planning Data
- **Endpoint**: `POST /api/v1/generator/planning`
- **Request Parameters**:
  - `purchaseOrders` (int, default: 200): Inbound Purchase Orders.
  - `productionPlans` (int, default: 50): Production schedules.
  - `seed` (long, default: 20261022): Deterministic seed.
- **Description**: Generates Purchase Orders and Production Plans linked to existing entities.

### 4.2 Preview Planning Data
- **Endpoint**: `GET /api/v1/generator/planning/preview`
- **Description**: Returns a structured preview sample of recent Purchase Orders and Production Plans.

### 4.3 Planning Data Statistics
- **Endpoint**: `GET /api/v1/generator/planning/statistics`
- **Description**: Returns counts, statuses, and value summaries of current planning records.

---

## 5. Shortage Detection & Mitigation Engine

### 5.1 Run Shortage Detection & Mitigation Engine
- **Endpoint**: `POST /api/v1/generator/shortages`
- **Request Parameters**:
  - `maxShortages` (int, optional): Upper bound for detected shortages.
  - `seed` (long, default: 20261022): Deterministic seed.
- **Description**: Executes the time-phased inventory projection engine across all SKU-location nodes, detects safety stock deficits, evaluates all 5 mitigation strategies, applies feasibility guards, and ranks feasible options by recommendation score.

### 5.2 Retrieve Shortages
- **Endpoint**: `GET /api/v1/shortages`
- **Query Parameters**:
  - `resolved` (boolean, optional): Filter by resolution status (`true` / `false`).
  - `severity` (enum, optional): `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
  - `skuId` (long, optional): Filter by SKU identifier.
  - `locationId` (long, optional): Filter by Location identifier.
- **Description**: Returns detected shortages and their nested, scored mitigation options.

### 5.3 Retrieve Shortage by Code
- **Endpoint**: `GET /api/v1/shortages/{shortageCode}`
- **Description**: Retrieves single shortage record by canonical code (e.g. `SH-0001`).

### 5.4 Resolve Shortage
- **Endpoint**: `PATCH /api/v1/shortages/{shortageCode}/resolve`
- **Description**: Sets the `resolved` flag to true and records `resolutionDate`.

### 5.5 Update Mitigation Option Status
- **Endpoint**: `PATCH /api/v1/mitigation-options/{optionCode}/status`  
  *(Also supported: `PATCH /api/v1/shortages/mitigation-options/{optionCode}/status`)*
- **Query Parameters**:
  - `status` (required, enum): `PROPOSED`, `APPROVED`, `REJECTED`, `EXECUTED`.
- **Description**: Updates the lifecycle status of a proposed tactical mitigation option.

---

## 6. Challenge Export & Verification Subsystem

### 6.1 Generate Complete Challenge Dataset
- **Endpoint**: `POST /api/v1/export/generate`
- **Query Parameters**:
  - `seed` (long, default: `20261022`): Deterministic generator seed.
- **Description**: Generates the complete benchmark dataset (50 Locations, 500 Suppliers, 3,000 SKUs, 52 weeks Forecast Demand, 10,000 Inbound POs, and all supporting entities) and exports to CSV, JSON, and SQL dumps. Emits `manifest.json` with cryptographic SHA-256 hashes.
- **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Challenge benchmark dataset generated and exported successfully",
  "data": {
    "systemName": "YottaFlex Supply Chain Planning and Synthetic Data Generation System",
    "version": "1.0.0-SNAPSHOT",
    "generatedAt": "2026-11-07",
    "seed": 20261022,
    "locationCount": 50,
    "supplierCount": 500,
    "skuCount": 3000,
    "forecastDemandWeeks": 52,
    "forecastDemandCount": 5200,
    "purchaseOrderCount": 10000,
    "inventoryCount": 3000,
    "supplierCapacityCount": 1500,
    "leadTimeCount": 1500,
    "productionPlanCount": 500,
    "shortageCount": 100,
    "mitigationOptionCount": 200,
    "fileChecksums": {
      "csv/locations.csv": "...",
      "csv/purchase_orders.csv": "...",
      "json/skus.json": "...",
      "sql/challenge_dataset_insert.sql": "..."
    }
  }
}
```

### 6.2 Get Export Manifest Summary
- **Endpoint**: `GET /api/v1/export/summary`
- **Description**: Returns the manifest of previously exported files, counts, and SHA-256 hashes.

### 6.3 Download Exported Files
- **Endpoint**: `GET /api/v1/export/download/{format}/{filename}`
- **Path Parameters**:
  - `format`: Format directory (`csv`, `json`, `sql`, `root`).
  - `filename`: Target file name (e.g. `locations.csv`, `purchase_orders.json`, `manifest.json`).
- **Response**: File stream with proper `Content-Disposition: attachment; filename="..."` and MIME type (`text/csv`, `application/json`, `application/sql`).

