# YottaFlex Supply Chain Planning System - AI Usage & Governance Document

This document outlines how Artificial Intelligence was utilized as an agentic pair-programming architect in the conceptualization, architecture design, code generation, testing, and deployment verification of the **YottaFlex Supply Chain Planning and Synthetic Data Generation System**.

---

## 1. Overview & Architectural Philosophy

The YottaFlex system is designed to simulate, plan, and analyze enterprise supply chains. To ensure industrial-grade software engineering, AI was used not simply as a text generator, but as an **Agentic Software Architect and Senior Java Developer** following strict software engineering principles:
- **Clean Layered Architecture:** Strict decoupling between presentation (`controller`), domain logic (`service`), data access (`repository`), and persistence models (`entity`).
- **Domain-Driven Design (DDD):** 11 cohesive domain models reflecting real-world supply chain entities (SKUs, Locations, Demands, Inventories, Capacities, Lead Times, Purchase Orders, Production Plans, Shortages, and Tactical Mitigations).
- **Test-Driven Verification:** No functionality is accepted without automated test validation, live execution, and database verification.

---

## 2. AI Platforms & Models Employed

| Dimension | Specification |
|---|---|
| **AI Environment** | Google Antigravity (Advanced Agentic Coding Platform) |
| **Foundation Model** | Gemini 3.8 Flash (High Reasoning Profile) |
| **Autonomous Capabilities** | Workspace inspection, process management, shell execution, file system management, reactive background tasks |
| **Developer Role** | Senior Java Spring Boot Developer, Security Engineer & Enterprise Software Architect |

---

## 3. How AI Was Utilized Across Project Phases

### Phase A: Environment Discovery & Toolchain Provisioning
1. **Automated Host Inspection:** AI inspected the host Windows environment, identified the active `MySQL80` service running on port 3306, and detected missing Java 21 and Maven toolchains.
2. **Toolchain Installation:** AI automatically downloaded and extracted Eclipse Adoptium OpenJDK 21 LTS and Apache Maven 3.9.9 into `D:\tools`, verified binaries, and configured persistent user environment variables (`JAVA_HOME`, `M2_HOME`, and `PATH`).

### Phase B: Enterprise Domain Modeling (11 Entities)
AI designed 11 production-grade JPA entities with comprehensive Bean Validation (`jakarta.validation.constraints`), indexes, and relational mapping:
- **`SKU`**: Unique SKU codes, categorizations, unit economics, reorder thresholds, safety stocks, and lead time baselines.
- **`Location`**: Physical multi-node network (plants, warehouses, distribution centers) with capacities.
- **`Supplier`**: Vendor profiles with reliability scores, ratings, and contact points.
- **`ForecastDemand`**: Time-series demand projections with confidence scores and pattern classifications (`STABLE`, `SEASONAL`, `TRENDING`, `VOLATILE`).
- **`Inventory`**: On-hand, reserved, and available stock levels, automatically computing inventory health status (`IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`).
- **`SupplierCapacity`**: Time-bounded capacity constraints per vendor and SKU.
- **`LeadTime`**: Multi-modal transit durations (road, air, ocean, rail) with expedited alternatives and MOQ.
- **`PurchaseOrder`**: Replenishment orders with lifecycle states and financial totals.
- **`ProductionPlan`**: Manufacturing runs, capacity utilization percentages, and planned vs produced quantities.
- **`Shortage`**: Incident detection with root-cause categorization and severity matrix.
- **`MitigationOption`**: Prescriptive resolution actions (expedite PO, reallocate inventory, alternate suppliers) with cost impact and recommendation scoring.

### Phase C: Data Access & Service Layer Implementation
- Designed 11 Spring Data JPA repositories with specialized lookup queries.
- Built initial transaction-managed service classes (`SkuService`, `LocationService`, `SupplierService`, `InventoryService`, `HealthCheckService`).
- Created a centralized `@RestControllerAdvice` (`GlobalExceptionHandler`) to translate domain exceptions into standardized RFC-compliant error structures.

### Phase D: Interactive UI & Telemetry
- Created an interactive web dashboard in `src/main/resources/static/index.html` using Bootstrap 5.3.3 and JavaScript ES6+ that polls `/api/v1/health` to visualize real-time table counts, JVM memory usage, runtime info, and database status.

### Phase E: Automated Verification & Milestone Validation
- Built automated test suites utilizing JUnit 5, Mockito, and Spring MockMvc.
- Executed `mvn compile` (57 source files compiled cleanly on Java 21).
- Executed `mvn test` (8 unit & integration tests executed with 0 failures).
- Packaged executable Spring Boot JAR and executed background daemon on port `8080`.
- Verified live HTTP endpoints (`/api/v1/health`, `/api/v1/health/ping`, `/actuator/health`, `/api/v1/skus`).
- Verified MySQL 8 table creation via native SQL queries (`SHOW TABLES; DESCRIBE;`).

### Phase F: Security Hardening & Zero-Credential Enforcement
- Enforced strict environment variable authentication (`${SPRING_DATASOURCE_USERNAME}`, `${SPRING_DATASOURCE_PASSWORD}`) with zero fallback defaults in configuration files.
- Purged sensitive parameter logging (`org.hibernate.SQL=WARN`, `org.hibernate.type.descriptor.sql=WARN`).
- Established secure in-memory PowerShell credential input patterns using `Read-Host -AsSecureString`.
- Verified zero credential exposure in source code, logs, and public templates.

### Phase G: Master Data Synthetic Data Generator (Milestone 2 Step 2)
- Implemented deterministic synthetic master data generator strictly scoped to `Location`, `Supplier`, and `SKU`.
- Implemented deterministic seeded pseudo-random generation with configurable seed (default: `20261022L`).
- Implemented non-destructive collision avoidance using database existence checks and dynamic sequence indexing (`LOC-SYN-`, `SUP-SYN-`, `SKU-SYN-`).
- Developed preview (`POST /api/v1/generator/master-data/preview`), persistent generation (`POST /api/v1/generator/master-data/generate`), and database statistics (`GET /api/v1/generator/master-data/statistics`) endpoints.
- Expanded automated test suite to 20 tests (JUnit 5, Mockito, MockMvc) covering determinism, domain constraints, duplicate avoidance, and boundary validation.

### Phase H: Dependent Transactional Data Generator (Milestone 2 Step 3)
- Implemented deterministic synthetic dependent transactional generator strictly scoped to `Inventory`, `ForecastDemand`, `SupplierCapacity`, and `LeadTime`.
- Integrated existing MySQL master records (`Location`, `Supplier`, `SKU`) as parent references without recreating or modifying master data.
- Enforced domain integrity: `quantityReserved <= quantityOnHand`, `quantityAvailable = onHand - reserved`, weekly Monday forecast dates, quarterly capacity horizons, MOQ and expedited lead time constraints.
- Engineered duplicate avoidance algorithms respecting unique constraints (`uk_inventory_sku_location`, unique forecast dates, capacity horizons, supplier-destination lead times).
- Exposed REST API endpoints: `POST /api/v1/generator/dependent-data/preview`, `POST /api/v1/generator/dependent-data/generate`, `GET /api/v1/generator/dependent-data/statistics`.
- Expanded automated test suite to 34 tests (JUnit 5, Mockito, MockMvc) with zero failures. Verified live against MySQL 8.0 database.

### Phase 2.4: Milestone 2 Step 4 – Purchase Order & Production Plan Synthetic Generator & Recovery
- Synthesized operational planning data strictly scoped to `PurchaseOrder` and `ProductionPlan`.
- Solved `LazyInitializationException` in preview operations by applying `@Transactional(readOnly = true)` to keep the Hibernate session bound while building preview DTO projections.
- Restricted manufacturing `ProductionPlan` assignments strictly to facilities with `LocationType.PLANT`, enforcing a hard validation check if no plants are configured.
- Aligned plan numbering with the domain prefix `PP-SYN-%04d` and purchase orders with `PO-SYN-%04d`.
- Integrated supplier capacity validation so that purchase order batch sizes do not exceed supplier production limits.
- Exposed REST API endpoints: `POST /api/v1/generator/operational-data/preview`, `POST /api/v1/generator/operational-data/generate`, `GET /api/v1/generator/operational-data/statistics` (with `/planning-data` alias support).
- Expanded automated test suite to 52 tests (JUnit 5, Mockito, MockMvc) with zero failures (`BUILD SUCCESS`). Verified live against running MySQL 8.0 database.

### Phase 2.5: Milestone 2 Step 5 – Shortage Detection & Tactical Mitigation Implementation
- Implemented time-phased shortage detection engine `SupplyChainPlanningEngine` evaluating cumulative inventory balance week-by-week using forecast periods.
- Enforced Rule 5: Production at a PLANT node is strictly isolated and never credited to warehouse or distribution center nodes without explicit transfer.
- Enforced Rule 6: Rigorous feasibility verification before recommending any tactical mitigation:
  1. `REALLOCATE_INVENTORY`: Requires origin location to possess surplus stock above its safety stock level.
  2. `EXPEDITE_PURCHASE_ORDER`: Requires an open PO with viable lead time compression via `LeadTime.expeditedDays`.
  3. `ADJUST_PRODUCTION_SCHEDULE`: Requires the target facility to be a designated `PLANT`.
  4. `ALTERNATIVE_SUPPLIER`: Requires qualified alternate vendor to possess positive `availableCapacity`.
  5. `SPLIT_SHIPMENT`: Requires open PO order quantity exceeding threshold for priority partial shipment.
- Implemented multi-criteria recommendation scoring and strict descending sort order (`recommendationScore`).
- Eliminated Hibernate proxy serialization errors in web layer by applying `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` and dedicated JOIN FETCH queries.
- Engineered non-destructive deduplication to prevent duplicate shortage creation across repeated engine runs.
- Expanded automated test suite to 74 tests (JUnit 5, Mockito, MockMvc) with zero failures (`BUILD SUCCESS`).
- Verified live against MySQL 8.0:
  - In-memory preview confirmed 0 database mutations (`persisted: false`).
  - Persistent generation saved 11 shortages (`SH-SYN-0001` to `SH-SYN-0011`) and 29 mitigations (`MIT-SYN-0001` to `MIT-SYN-0029`).
  - Re-run confirmed duplicate prevention (`shortagesSaved: 0`).
  - Operational lifecycle endpoints verified (`PATCH /resolve` and `PATCH /status`).

### Phase 2.6: Milestone 2 Final Remediation – Shortage Mitigation Execution Readiness
- Extended `MitigationOption.java` entity, repository, and DTOs with structured execution fields (`mitigatedQuantity`, `purchaseOrder`, `productionPlan`, `estimatedArrivalDate`, `freightMode`, `splitRemainingQuantity`) and `asOfDate`.
- Implemented versioned migration `database/migration_v2_mitigation_options.sql` and updated `database/schema.sql` while preserving existing 29 mitigation option records intact.
- Engineered in-memory `ResourceAllocationLedger`:
  - Tracks allocated pending quantity per PO, locking POs against simultaneous expediting and splitting.
  - Ingests previously `APPROVED` and `EXECUTED` mitigations from MySQL to reserve allocated resources.
  - Manages monthly recurrent supplier capacity quotas per `(supplier, SKU, YearMonth)` within active date windows.
- Enforced Critical Date Invariant: delivery arriving after the shortage date cannot resolve the shortage (`estimatedArrivalDate <= shortageDate`).
- Implemented dynamic weight renormalization scoring (Coverage 35%, Speed 25%, Economics 20%, Reliability 20%) bounded in [0.0, 100.0] with zero arbitrary constants.
- Explicitly handled domain limitations: omitted unverified inter-facility reallocation and undefined plant capacity adjustments.
- Expanded automated test suite to 77 tests (JUnit 5, Mockito, MockMvc) with zero failures (`BUILD SUCCESS`). Verified live against running MySQL 8.0 instance and Spring Boot REST API.

---

## 4. Security, Secrets Management & Open Source Readiness

To comply with enterprise security practices and open-source public repository standards:
1. **Zero Hardcoded Credentials:** Application configuration strictly requires dynamic environment variable resolution (`${SPRING_DATASOURCE_USERNAME}`, `${SPRING_DATASOURCE_PASSWORD}`, `${SPRING_DATASOURCE_URL}`).
2. **Environment Template:** Created [`.env.example`](file:///d:/supply_chain/.env.example) to document required keys without committing actual secrets.
3. **Hardened `.gitignore`:** Strict rules preventing accidental commits of `.env`, `.env.*`, certificates (`.pem`, `.key`, `.p12`, `.jks`), `application-local.properties`, IDE configurations, build outputs (`target/`), and temporary logs.
4. **Sanitized Database Scripts:** Created [`database/schema.sql`](file:///d:/supply_chain/database/schema.sql) and [`database/sample_data.sql`](file:///d:/supply_chain/database/sample_data.sql) containing purely synthetic mock data.

---

## 5. Human-in-the-Loop Governance

In accordance with user guidelines:
- **No Remote Repository Creation or Push:** Git is initialized locally only. All remote GitHub repository creation and pushing actions are strictly deferred until explicit confirmation is provided by the user.
- **Transparent Audit Trail:** All prompts and corresponding architectural executions are documented in [`PROMPT_HISTORY.md`](file:///d:/supply_chain/PROMPT_HISTORY.md).
