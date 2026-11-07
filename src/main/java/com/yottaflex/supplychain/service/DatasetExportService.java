package com.yottaflex.supplychain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yottaflex.supplychain.entity.*;
import com.yottaflex.supplychain.entity.enums.*;
import com.yottaflex.supplychain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Service responsible for:
 * 1. Deterministic generation of challenge-scale synthetic datasets:
 *    - 50 Locations, 500 Suppliers, 3,000 SKUs, 52 weeks Forecast Demand, 10,000 Inbound Purchase Orders.
 * 2. Multi-format exports: CSV, JSON, and SQL.
 * 3. Exporting existing MySQL database records.
 * 4. Manifest generation with SHA-256 cryptographic checksums.
 * 5. Ground-truth and edge-case dataset provisioning.
 */
@Service
public class DatasetExportService {

    private static final Logger log = LoggerFactory.getLogger(DatasetExportService.class);

    public static final long DEFAULT_CHALLENGE_SEED = 20261022L;

    private final LocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final SKURepository skuRepository;
    private final InventoryRepository inventoryRepository;
    private final ForecastDemandRepository forecastDemandRepository;
    private final SupplierCapacityRepository supplierCapacityRepository;
    private final LeadTimeRepository leadTimeRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ShortageRepository shortageRepository;
    private final MitigationOptionRepository mitigationOptionRepository;

    private final ObjectMapper objectMapper;

    public DatasetExportService(LocationRepository locationRepository,
                                SupplierRepository supplierRepository,
                                SKURepository skuRepository,
                                InventoryRepository inventoryRepository,
                                ForecastDemandRepository forecastDemandRepository,
                                SupplierCapacityRepository supplierCapacityRepository,
                                LeadTimeRepository leadTimeRepository,
                                PurchaseOrderRepository purchaseOrderRepository,
                                ProductionPlanRepository productionPlanRepository,
                                ShortageRepository shortageRepository,
                                MitigationOptionRepository mitigationOptionRepository) {
        this.locationRepository = locationRepository;
        this.supplierRepository = supplierRepository;
        this.skuRepository = skuRepository;
        this.inventoryRepository = inventoryRepository;
        this.forecastDemandRepository = forecastDemandRepository;
        this.supplierCapacityRepository = supplierCapacityRepository;
        this.leadTimeRepository = leadTimeRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productionPlanRepository = productionPlanRepository;
        this.shortageRepository = shortageRepository;
        this.mitigationOptionRepository = mitigationOptionRepository;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // =========================================================================
    // Record Structures for Challenge Export
    // =========================================================================

    public record LocationExport(long id, String locationCode, String name, String type,
                                 String city, String state, String country, String postalCode,
                                 String address, double capacity, boolean active) {}

    public record SupplierExport(long id, String supplierCode, String name, String contactPerson,
                                 String email, String phone, String country, double rating,
                                 double reliabilityScore, boolean active) {}

    public record SkuExport(long id, String skuCode, String name, String description,
                            String category, String unitOfMeasure, double unitCost,
                            double sellingPrice, int reorderPoint, int safetyStock,
                            int leadTimeDays, boolean active) {}

    public record InventoryExport(long id, String skuCode, String locationCode, double quantityOnHand,
                                  double quantityReserved, double quantityAvailable,
                                  double safetyStockLevel, double reorderPoint, String status, String lastAuditDate) {}

    public record ForecastDemandExport(long id, String skuCode, String locationCode, String forecastDate,
                                       String periodType, double forecastQuantity, double confidenceScore, String demandPattern) {}

    public record SupplierCapacityExport(long id, String supplierCode, String skuCode, String effectiveStartDate,
                                         String effectiveEndDate, double dailyCapacity, double monthlyCapacity,
                                         double allocatedCapacity, double availableCapacity) {}

    public record LeadTimeExport(long id, String supplierCode, String skuCode, String destinationLocationCode,
                                 int standardDays, int expeditedDays, int minimumOrderQuantity,
                                 double expeditedCostPerUnit, String transportMode) {}

    public record PurchaseOrderExport(long id, String poNumber, String supplierCode, String skuCode,
                                      String destinationLocationCode, String orderDate, String expectedDeliveryDate,
                                      String actualDeliveryDate, double quantityOrdered, double quantityReceived,
                                      double unitPrice, String status, String notes) {}

    public record ProductionPlanExport(long id, String planCode, String skuCode, String locationCode,
                                       String startDate, String endDate, double plannedQuantity,
                                       double producedQuantity, double capacityUtilization, String status, String notes) {}

    public record ShortageExport(long id, String shortageCode, String skuCode, String locationCode,
                                 String shortageDate, double shortageQuantity, String severity,
                                 String rootCause, boolean resolved, String resolutionDate, String notes) {}

    public record MitigationOptionExport(long id, String optionCode, String shortageCode, String strategy,
                                         String alternateSupplierCode, String sourceLocationCode,
                                         String purchaseOrderNumber, String productionPlanCode,
                                         double costImpact, int timeImpactDays, String status,
                                         double recommendationScore, Double mitigatedQuantity,
                                         String estimatedArrivalDate, String freightMode,
                                         Double splitRemainingQuantity, String details) {}

    public record ExportManifest(
            String systemName,
            String version,
            String generatedAt,
            long seed,
            int locationCount,
            int supplierCount,
            int skuCount,
            int forecastDemandWeeks,
            int forecastDemandCount,
            int purchaseOrderCount,
            int inventoryCount,
            int supplierCapacityCount,
            int leadTimeCount,
            int productionPlanCount,
            int shortageCount,
            int mitigationOptionCount,
            Map<String, String> fileChecksums,
            Map<String, Long> fileSizesBytes,
            Map<String, String> dataDictionary
    ) {}

    // =========================================================================
    // Core Generator & Exporter for Challenge Benchmark Dataset
    // =========================================================================

    public ExportManifest generateAndExportChallengeDataset(long seed, Path exportBaseDir) throws IOException {
        log.info("Generating and exporting complete YottaFlex challenge benchmark dataset (seed: {})...", seed);
        Files.createDirectories(exportBaseDir);
        Path csvDir = exportBaseDir.resolve("csv");
        Path jsonDir = exportBaseDir.resolve("json");
        Path sqlDir = exportBaseDir.resolve("sql");
        Files.createDirectories(csvDir);
        Files.createDirectories(jsonDir);
        Files.createDirectories(sqlDir);

        Random random = new Random(seed);

        // 1. Generate 50 Locations
        List<LocationExport> locations = new ArrayList<>(50);
        String[] cities = {"Chicago", "Dallas", "Seattle", "Atlanta", "Columbus", "Phoenix", "Philadelphia", "Memphis", "Denver", "Austin",
                "Detroit", "Miami", "Charlotte", "Kansas City", "Indianapolis", "Salt Lake City", "Nashville", "Portland", "Minneapolis", "Houston",
                "San Jose", "Boston", "Tampa", "Baltimore", "Raleigh", "Louisville", "Milwaukee", "Albuquerque", "Tucson", "Fresno",
                "Sacramento", "Mesa", "Omaha", "Colorado Springs", "Virginia Beach", "Oakland", "Tulsa", "Arlington", "New Orleans", "Wichita",
                "Cleveland", "Bakersfield", "Aurora", "Anaheim", "Honolulu", "Santa Ana", "Riverside", "Corpus Christi", "Lexington", "Stockton"};
        String[] states = {"IL", "TX", "WA", "GA", "OH", "AZ", "PA", "TN", "CO", "TX",
                "MI", "FL", "NC", "MO", "IN", "UT", "TN", "OR", "MN", "TX",
                "CA", "MA", "FL", "MD", "NC", "KY", "WI", "NM", "AZ", "CA",
                "CA", "AZ", "NE", "CO", "VA", "CA", "OK", "TX", "LA", "KS",
                "OH", "CA", "CO", "CA", "HI", "CA", "CA", "TX", "KY", "CA"};

        LocationType[] locTypes = {LocationType.PLANT, LocationType.WAREHOUSE, LocationType.DISTRIBUTION_CENTER};
        for (int i = 1; i <= 50; i++) {
            String code = String.format("LOC-%04d", i);
            int idx = (i - 1) % cities.length;
            String city = cities[idx];
            String state = states[idx];
            LocationType type = (i % 5 == 1 || i % 5 == 3) ? LocationType.PLANT : locTypes[(i - 1) % locTypes.length];
            String name = String.format("%s %s %s", city, (type == LocationType.PLANT ? "Manufacturing Plant" : (type == LocationType.WAREHOUSE ? "Regional Warehouse" : "Distribution Center")), (i / cities.length > 0 ? (i / cities.length + 1) : ""));
            double cap = Math.round((50000.0 + random.nextDouble() * 200000.0) * 100.0) / 100.0;
            locations.add(new LocationExport(i, code, name.trim(), type.name(), city, state, "USA", String.format("%05d", 10000 + i * 15), String.format("%d Logistics Way", 100 + i * 5), cap, true));
        }

        // 2. Generate 500 Suppliers
        List<SupplierExport> suppliers = new ArrayList<>(500);
        String[] supplierPrefixes = {"Apex", "Vanguard", "Titan", "Global", "Nexus", "Pinnacle", "AeroTech", "Quantum", "Omni", "Sterling",
                "Echo", "Cascade", "Summit", "Frontier", "Horizon", "Matrix", "Dynamic", "Kinetics", "Spectra", "Vertex",
                "Delta", "Precision", "Pioneer", "Nordic", "Pacific", "Alpine", "Orion", "Atlas", "Zenith", "Novus",
                "Prime", "Solaria", "Axiom", "BlueShift", "Titanium", "Centaur", "Valence", "Beacon", "Cobalt", "Hyperion",
                "Venture", "IronClad", "Terra", "Strata", "Aether", "Polaris", "Aura", "Pulse", "Crestview", "Keystone"};
        String[] supplierSuffixes = {"Components Ltd", "Microelectronics Inc", "Industrial Corp", "Technologies GmbH", "Materials Group", "Systems AG", "Manufacturing SA", "Enterprises LLC", "Dynamics Corp", "Works Ltd"};
        String[] countries = {"USA", "Japan", "Germany", "Taiwan", "South Korea", "UK", "Canada", "France", "Italy", "Sweden"};
        for (int i = 1; i <= 500; i++) {
            String code = String.format("SUP-%04d", i);
            String pfx = supplierPrefixes[(i - 1) % supplierPrefixes.length];
            String sfx = supplierSuffixes[(i - 1) % supplierSuffixes.length];
            String company = (i > 50) ? String.format("%s %s Division %d", pfx, sfx, (i / 50)) : String.format("%s %s", pfx, sfx);
            String country = countries[(i - 1) % countries.length];
            double rating = Math.round((3.8 + random.nextDouble() * 1.2) * 10.0) / 10.0;
            double reliability = Math.round((80.0 + random.nextDouble() * 19.5) * 10.0) / 10.0;
            suppliers.add(new SupplierExport(i, code, company, "Operations Director", String.format("contact.%s@example.com", code.toLowerCase()), String.format("+1-555-%04d", 2000 + (i % 8000)), country, rating, reliability, true));
        }

        // 3. Generate 3,000 SKUs
        List<SkuExport> skus = new ArrayList<>(3000);
        String[] categories = {"Electronics", "Industrial Hardware", "Raw Materials", "Automotive Parts", "Packaging", "Hydraulics", "Optoelectronics", "Fasteners", "Robotics", "Sensors"};
        String[] baseNames = {"Microcontroller Board", "Power MOSFET Module", "Linear Voltage Regulator", "Industrial Pressure Sensor", "Hex Bolt Grade 8",
                "Sealed Ball Bearing", "Flange Nut Metric", "Hardened Dowel Pin", "Steel Compression Spring", "Aerospace Aluminum Sheet",
                "Copper Conductor Wire", "Silicone Rubber Gasket", "Carbon Fiber Structural Rod", "Virgin Polymer Pellets", "Vented Brake Rotor",
                "High-Torque Timing Belt", "Common-Rail Fuel Injector", "Broadband Oxygen Sensor", "Cabin Air Filter HEPA", "Heavy-Duty Corrugated Carton",
                "Anti-Static Packaging Film", "Thermal Transfer Shipping Labels", "Directional Spool Valve", "Double-Acting Tie-Rod Cylinder", "Infrared Phototransistor Array"};
        for (int i = 1; i <= 3000; i++) {
            String code = String.format("SKU-%04d", i);
            String cat = categories[(i - 1) % categories.length];
            String base = baseNames[(i - 1) % baseNames.length];
            String name = (i > baseNames.length) ? String.format("%s Spec v%d", base, (i / baseNames.length) + 1) : base;
            double cost = Math.round((5.0 + random.nextDouble() * 145.0) * 100.0) / 100.0;
            double markup = 0.25 + random.nextDouble() * 0.50;
            double price = Math.round((cost * (1.0 + markup)) * 100.0) / 100.0;
            int safety = 15 + (i % 50);
            int reorder = safety + 20 + (i % 80);
            int leadDays = 3 + (i % 25);
            skus.add(new SkuExport(i, code, name, String.format("Industrial grade %s component for supply chain operations", cat.toLowerCase()), cat, "UNITS", cost, price, reorder, safety, leadDays, true));
        }

        // 4. Generate 52 Weeks Forecast Demand
        LocalDate startMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        List<ForecastDemandExport> forecasts = new ArrayList<>();
        long fdId = 1;
        // 52 consecutive calendar weeks across top 100 SKUs at 5 major fulfillment locations
        int sampleSkus = Math.min(100, skus.size());
        for (int w = 0; w < 52; w++) {
            LocalDate weekDate = startMonday.plusWeeks(w);
            for (int s = 0; s < sampleSkus; s++) {
                SkuExport sku = skus.get(s);
                LocationExport loc = locations.get(s % 5);
                double qty = Math.round((50.0 + (s * 3.5) + (Math.sin(w * 0.3) * 25.0) + random.nextDouble() * 30.0) * 100.0) / 100.0;
                double conf = Math.round((0.85 + random.nextDouble() * 0.14) * 100.0) / 100.0;
                DemandPattern pattern = (w % 4 == 0) ? DemandPattern.SEASONAL : (w % 6 == 0 ? DemandPattern.TRENDING : DemandPattern.STABLE);
                forecasts.add(new ForecastDemandExport(fdId++, sku.skuCode(), loc.locationCode(), weekDate.toString(), "WEEKLY", qty, conf, pattern.name()));
            }
        }

        // 5. Generate Inventories
        List<InventoryExport> inventories = new ArrayList<>(skus.size());
        for (int i = 0; i < skus.size(); i++) {
            SkuExport sku = skus.get(i);
            LocationExport loc = locations.get(i % locations.size());
            double onHand = Math.round((100.0 + random.nextDouble() * 900.0) * 100.0) / 100.0;
            double reserved = Math.round((onHand * (random.nextDouble() * 0.30)) * 100.0) / 100.0;
            double avail = Math.round((onHand - reserved) * 100.0) / 100.0;
            String status = avail >= sku.reorderPoint() ? "HEALTHY" : (avail > sku.safetyStock() ? "NORMAL" : (avail > 0 ? "LOW_STOCK" : "STOCKOUT"));
            inventories.add(new InventoryExport(i + 1, sku.skuCode(), loc.locationCode(), onHand, reserved, avail, sku.safetyStock(), sku.reorderPoint(), status, LocalDate.now().minusDays(i % 30).toString()));
        }

        // 6. Generate Supplier Capacities (1,500 records)
        List<SupplierCapacityExport> capacities = new ArrayList<>(1500);
        for (int i = 1; i <= 1500; i++) {
            SupplierExport sup = suppliers.get((i - 1) % suppliers.size());
            SkuExport sku = skus.get((i - 1) % skus.size());
            LocalDate effStart = LocalDate.now().minusMonths(1);
            LocalDate effEnd = LocalDate.now().plusMonths(6);
            double monthly = Math.round((500.0 + random.nextDouble() * 4500.0) * 100.0) / 100.0;
            double allocated = Math.round((monthly * (random.nextDouble() * 0.40)) * 100.0) / 100.0;
            double available = Math.round((monthly - allocated) * 100.0) / 100.0;
            double daily = Math.round((monthly / 30.0) * 100.0) / 100.0;
            capacities.add(new SupplierCapacityExport(i, sup.supplierCode(), sku.skuCode(), effStart.toString(), effEnd.toString(), daily, monthly, allocated, available));
        }

        // 7. Generate Lead Times (1,500 records)
        List<LeadTimeExport> leadTimes = new ArrayList<>(1500);
        TransportMode[] freightModes = {TransportMode.AIR, TransportMode.ROAD, TransportMode.OCEAN, TransportMode.RAIL};
        for (int i = 1; i <= 1500; i++) {
            SupplierExport sup = suppliers.get((i - 1) % suppliers.size());
            SkuExport sku = skus.get((i - 1) % skus.size());
            LocationExport loc = locations.get((i - 1) % locations.size());
            int stdDays = 5 + (i % 25);
            int expDays = Math.max(2, stdDays - 4 - (i % 3));
            int moq = (i % 10 == 0) ? 50 : (i % 5 == 0 ? 20 : 1);
            double expCost = Math.round((3.0 + (i % 12) * 1.5) * 100.0) / 100.0;
            TransportMode mode = freightModes[(i - 1) % freightModes.length];
            leadTimes.add(new LeadTimeExport(i, sup.supplierCode(), sku.skuCode(), loc.locationCode(), stdDays, expDays, moq, expCost, mode.name()));
        }

        // 8. Generate 10,000 Inbound Purchase Orders
        List<PurchaseOrderExport> purchaseOrders = new ArrayList<>(10000);
        PurchaseOrderStatus[] poStatuses = {
                PurchaseOrderStatus.DELIVERED, PurchaseOrderStatus.DELIVERED, PurchaseOrderStatus.IN_TRANSIT,
                PurchaseOrderStatus.IN_TRANSIT, PurchaseOrderStatus.PLACED, PurchaseOrderStatus.PARTIALLY_DELIVERED
        };
        for (int i = 1; i <= 10000; i++) {
            String poNumber = String.format("PO-%05d", i);
            SupplierExport sup = suppliers.get((i - 1) % suppliers.size());
            SkuExport sku = skus.get((i - 1) % skus.size());
            LocationExport loc = locations.get((i - 1) % locations.size());
            PurchaseOrderStatus status = poStatuses[(i - 1) % poStatuses.length];
            int leadDays = sku.leadTimeDays();

            int daysAgo = 5 + (i % 60);
            LocalDate orderDate = LocalDate.now().minusDays(daysAgo);
            LocalDate expectedDeliveryDate = orderDate.plusDays(leadDays);

            double qtyVal = Math.round((20.0 + random.nextDouble() * 280.0) * 100.0) / 100.0;
            LocalDate actualDeliveryDate = null;
            double qtyReceived = 0.0;

            if (status == PurchaseOrderStatus.DELIVERED) {
                actualDeliveryDate = expectedDeliveryDate;
                qtyReceived = qtyVal;
            } else if (status == PurchaseOrderStatus.PARTIALLY_DELIVERED) {
                actualDeliveryDate = expectedDeliveryDate.minusDays(1);
                qtyReceived = Math.round((qtyVal * 0.50) * 100.0) / 100.0;
            }

            purchaseOrders.add(new PurchaseOrderExport(i, poNumber, sup.supplierCode(), sku.skuCode(), loc.locationCode(),
                    orderDate.toString(), expectedDeliveryDate.toString(), actualDeliveryDate != null ? actualDeliveryDate.toString() : null,
                    qtyVal, qtyReceived, sku.unitCost(), status.name(), "Replenishment procurement order"));
        }

        // 9. Generate Production Plans (500 records)
        List<ProductionPlanExport> productionPlans = new ArrayList<>(500);
        List<LocationExport> plants = locations.stream().filter(l -> "PLANT".equals(l.type())).toList();
        for (int i = 1; i <= 500; i++) {
            String code = String.format("PP-%04d", i);
            SkuExport sku = skus.get((i - 1) % skus.size());
            LocationExport plant = plants.get((i - 1) % plants.size());
            LocalDate start = LocalDate.now().minusDays(i % 20);
            LocalDate end = start.plusDays(10 + (i % 15));
            double planned = Math.round((200.0 + random.nextDouble() * 800.0) * 100.0) / 100.0;
            double produced = Math.round((planned * 0.85) * 100.0) / 100.0;
            double util = Math.round((70.0 + random.nextDouble() * 35.0) * 10.0) / 10.0;
            productionPlans.add(new ProductionPlanExport(i, code, sku.skuCode(), plant.locationCode(),
                    start.toString(), end.toString(), planned, produced, util, "SCHEDULED", "Manufacturing shift cycle"));
        }

        // 10. Generate Shortages & Mitigations (100 Shortages, 200 Mitigations)
        List<ShortageExport> shortages = new ArrayList<>(100);
        List<MitigationOptionExport> mitigations = new ArrayList<>(200);
        long optId = 1;
        for (int i = 1; i <= 100; i++) {
            String code = String.format("SH-%04d", i);
            SkuExport sku = skus.get((i - 1) % 50);
            LocationExport loc = locations.get((i - 1) % 10);
            LocalDate date = startMonday.plusWeeks((i - 1) % 8);
            double qty = Math.round((15.0 + random.nextDouble() * 150.0) * 100.0) / 100.0;
            ShortageSeverity sev = qty > 100 ? ShortageSeverity.CRITICAL : (qty > 50 ? ShortageSeverity.HIGH : ShortageSeverity.MEDIUM);
            ShortageRootCause cause = (i % 3 == 0) ? ShortageRootCause.DEMAND_SURGE : (i % 3 == 1 ? ShortageRootCause.SUPPLIER_DELAY : ShortageRootCause.CAPACITY_BOTTLENECK);
            shortages.add(new ShortageExport(i, code, sku.skuCode(), loc.locationCode(), date.toString(), qty, sev.name(), cause.name(), (i == 1), (i == 1 ? LocalDate.now().toString() : null), "Time-phased projected inventory deficit"));

            // Option 1: Expedite PO
            PurchaseOrderExport matchedPo = purchaseOrders.get((i - 1) % purchaseOrders.size());
            double optScore1 = Math.round((85.0 + random.nextDouble() * 10.0) * 10.0) / 10.0;
            double cost1 = Math.round((qty * 8.50) * 100.0) / 100.0;
            mitigations.add(new MitigationOptionExport(optId++, String.format("MIT-%04d", optId - 1), code,
                    "EXPEDITE_PURCHASE_ORDER", null, null, matchedPo.poNumber(), null,
                    cost1, 4, (i == 1 ? "APPROVED" : "PROPOSED"), optScore1, qty, date.minusDays(2).toString(), "AIR", null,
                    "Expedite purchase order delivery transit by 4 days via express air freight"));

            // Option 2: Alternative Supplier
            SupplierExport altSup = suppliers.get((i + 5) % suppliers.size());
            double optScore2 = Math.round((78.0 + random.nextDouble() * 10.0) * 10.0) / 10.0;
            double cost2 = Math.round((qty * 12.00) * 100.0) / 100.0;
            mitigations.add(new MitigationOptionExport(optId++, String.format("MIT-%04d", optId - 1), code,
                    "ALTERNATIVE_SUPPLIER", altSup.supplierCode(), null, null, null,
                    cost2, 5, "PROPOSED", optScore2, qty, date.minusDays(1).toString(), "ROAD", null,
                    "Engage alternative qualified vendor with verified monthly capacity"));
        }

        // =====================================================================
        // Write CSV Files
        // =====================================================================
        writeCsv(csvDir.resolve("locations.csv"), "id,location_code,name,type,city,state,country,postal_code,address,capacity,active", locations,
                l -> String.format("%d,%s,\"%s\",%s,\"%s\",%s,%s,%s,\"%s\",%.2f,%b", l.id(), l.locationCode(), l.name(), l.type(), l.city(), l.state(), l.country(), l.postalCode(), l.address(), l.capacity(), l.active()));

        writeCsv(csvDir.resolve("suppliers.csv"), "id,supplier_code,name,contact_person,email,phone,country,rating,reliability_score,active", suppliers,
                s -> String.format("%d,%s,\"%s\",\"%s\",%s,%s,%s,%.1f,%.1f,%b", s.id(), s.supplierCode(), s.name(), s.contactPerson(), s.email(), s.phone(), s.country(), s.rating(), s.reliabilityScore(), s.active()));

        writeCsv(csvDir.resolve("skus.csv"), "id,sku_code,name,description,category,unit_of_measure,unit_cost,selling_price,reorder_point,safety_stock,lead_time_days,active", skus,
                k -> String.format("%d,%s,\"%s\",\"%s\",%s,%s,%.2f,%.2f,%d,%d,%d,%b", k.id(), k.skuCode(), k.name(), k.description(), k.category(), k.unitOfMeasure(), k.unitCost(), k.sellingPrice(), k.reorderPoint(), k.safetyStock(), k.leadTimeDays(), k.active()));

        writeCsv(csvDir.resolve("inventories.csv"), "id,sku_code,location_code,quantity_on_hand,quantity_reserved,quantity_available,safety_stock_level,reorder_point,status,last_audit_date", inventories,
                inv -> String.format("%d,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%s", inv.id(), inv.skuCode(), inv.locationCode(), inv.quantityOnHand(), inv.quantityReserved(), inv.quantityAvailable(), inv.safetyStockLevel(), inv.reorderPoint(), inv.status(), inv.lastAuditDate()));

        writeCsv(csvDir.resolve("forecast_demands.csv"), "id,sku_code,location_code,forecast_date,period_type,forecast_quantity,confidence_score,demand_pattern", forecasts,
                f -> String.format("%d,%s,%s,%s,%s,%.2f,%.2f,%s", f.id(), f.skuCode(), f.locationCode(), f.forecastDate(), f.periodType(), f.forecastQuantity(), f.confidenceScore(), f.demandPattern()));

        writeCsv(csvDir.resolve("supplier_capacities.csv"), "id,supplier_code,sku_code,effective_start_date,effective_end_date,daily_capacity,monthly_capacity,allocated_capacity,available_capacity", capacities,
                c -> String.format("%d,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f", c.id(), c.supplierCode(), c.skuCode(), c.effectiveStartDate(), c.effectiveEndDate(), c.dailyCapacity(), c.monthlyCapacity(), c.allocatedCapacity(), c.availableCapacity()));

        writeCsv(csvDir.resolve("lead_times.csv"), "id,supplier_code,sku_code,destination_location_code,standard_days,expedited_days,minimum_order_quantity,expedited_cost_per_unit,transport_mode", leadTimes,
                lt -> String.format("%d,%s,%s,%s,%d,%d,%d,%.2f,%s", lt.id(), lt.supplierCode(), lt.skuCode(), lt.destinationLocationCode(), lt.standardDays(), lt.expeditedDays(), lt.minimumOrderQuantity(), lt.expeditedCostPerUnit(), lt.transportMode()));

        writeCsv(csvDir.resolve("purchase_orders.csv"), "id,po_number,supplier_code,sku_code,destination_location_code,order_date,expected_delivery_date,actual_delivery_date,quantity_ordered,quantity_received,unit_price,status,notes", purchaseOrders,
                p -> String.format("%d,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%s,\"%s\"", p.id(), p.poNumber(), p.supplierCode(), p.skuCode(), p.destinationLocationCode(), p.orderDate(), p.expectedDeliveryDate(), p.actualDeliveryDate() != null ? p.actualDeliveryDate() : "", p.quantityOrdered(), p.quantityReceived(), p.unitPrice(), p.status(), p.notes()));

        writeCsv(csvDir.resolve("production_plans.csv"), "id,plan_code,sku_code,location_code,start_date,end_date,planned_quantity,produced_quantity,capacity_utilization,status,notes", productionPlans,
                pl -> String.format("%d,%s,%s,%s,%s,%s,%.2f,%.2f,%.1f,%s,\"%s\"", pl.id(), pl.planCode(), pl.skuCode(), pl.locationCode(), pl.startDate(), pl.endDate(), pl.plannedQuantity(), pl.producedQuantity(), pl.capacityUtilization(), pl.status(), pl.notes()));

        writeCsv(csvDir.resolve("shortages.csv"), "id,shortage_code,sku_code,location_code,shortage_date,shortage_quantity,severity,root_cause,resolved,resolution_date,notes", shortages,
                sh -> String.format("%d,%s,%s,%s,%s,%.2f,%s,%s,%b,%s,\"%s\"", sh.id(), sh.shortageCode(), sh.skuCode(), sh.locationCode(), sh.shortageDate(), sh.shortageQuantity(), sh.severity(), sh.rootCause(), sh.resolved(), sh.resolutionDate() != null ? sh.resolutionDate() : "", sh.notes()));

        writeCsv(csvDir.resolve("mitigation_options.csv"), "id,option_code,shortage_code,strategy,alternate_supplier_code,source_location_code,purchase_order_number,production_plan_code,cost_impact,time_impact_days,status,recommendation_score,mitigated_quantity,estimated_arrival_date,freight_mode,split_remaining_quantity,details", mitigations,
                mo -> String.format("%d,%s,%s,%s,%s,%s,%s,%s,%.2f,%d,%s,%.1f,%s,%s,%s,%s,\"%s\"", mo.id(), mo.optionCode(), mo.shortageCode(), mo.strategy(), mo.alternateSupplierCode() != null ? mo.alternateSupplierCode() : "", mo.sourceLocationCode() != null ? mo.sourceLocationCode() : "", mo.purchaseOrderNumber() != null ? mo.purchaseOrderNumber() : "", mo.productionPlanCode() != null ? mo.productionPlanCode() : "", mo.costImpact(), mo.timeImpactDays(), mo.status(), mo.recommendationScore(), mo.mitigatedQuantity() != null ? String.format("%.2f", mo.mitigatedQuantity()) : "", mo.estimatedArrivalDate() != null ? mo.estimatedArrivalDate() : "", mo.freightMode() != null ? mo.freightMode() : "", mo.splitRemainingQuantity() != null ? String.format("%.2f", mo.splitRemainingQuantity()) : "", mo.details()));

        // =====================================================================
        // Write JSON Files
        // =====================================================================
        objectMapper.writeValue(jsonDir.resolve("locations.json").toFile(), locations);
        objectMapper.writeValue(jsonDir.resolve("suppliers.json").toFile(), suppliers);
        objectMapper.writeValue(jsonDir.resolve("skus.json").toFile(), skus);
        objectMapper.writeValue(jsonDir.resolve("inventories.json").toFile(), inventories);
        objectMapper.writeValue(jsonDir.resolve("forecast_demands.json").toFile(), forecasts);
        objectMapper.writeValue(jsonDir.resolve("supplier_capacities.json").toFile(), capacities);
        objectMapper.writeValue(jsonDir.resolve("lead_times.json").toFile(), leadTimes);
        objectMapper.writeValue(jsonDir.resolve("purchase_orders.json").toFile(), purchaseOrders);
        objectMapper.writeValue(jsonDir.resolve("production_plans.json").toFile(), productionPlans);
        objectMapper.writeValue(jsonDir.resolve("shortages.json").toFile(), shortages);
        objectMapper.writeValue(jsonDir.resolve("mitigation_options.json").toFile(), mitigations);

        // =====================================================================
        // Write SQL Dumps (Challenge insert file)
        // =====================================================================
        writeSqlInserts(sqlDir.resolve("challenge_dataset_insert.sql"), locations, suppliers, skus, inventories, forecasts, capacities, leadTimes, purchaseOrders, productionPlans, shortages, mitigations);

        // =====================================================================
        // Compute SHA-256 Hashes and File Sizes
        // =====================================================================
        Map<String, String> checksums = new TreeMap<>();
        Map<String, Long> sizes = new TreeMap<>();
        List<Path> allExportFiles = new ArrayList<>();
        try (var stream = Files.walk(exportBaseDir)) {
            stream.filter(Files::isRegularFile).forEach(allExportFiles::add);
        }

        for (Path f : allExportFiles) {
            String rel = exportBaseDir.relativize(f).toString().replace('\\', '/');
            checksums.put(rel, calculateSha256(f));
            sizes.put(rel, Files.size(f));
        }

        Map<String, String> dataDict = new LinkedHashMap<>();
        dataDict.put("locations", "50 facilities (PLANT, WAREHOUSE, DISTRIBUTION_CENTER) across US key metropolitan freight nodes");
        dataDict.put("suppliers", "500 global vendor partners with rating (1-5) and reliability score (0-100)");
        dataDict.put("skus", "3,000 components across 10 categories with cost, markup price, reorder points, and lead times");
        dataDict.put("forecast_demands", "5,200 time-phased demand projections spanning 52 consecutive calendar weeks (2026-11-09 to 2027-11-01)");
        dataDict.put("purchase_orders", "10,000 inbound purchase orders with delivery dates, quantities, unit prices, and status");
        dataDict.put("inventories", "3,000 stocking node inventory levels with on-hand, reserved, available, and health status");
        dataDict.put("supplier_capacities", "1,500 monthly recurring vendor capacity quotas with allocated and available balances");
        dataDict.put("lead_times", "1,500 supplier-to-destination transport lead times with expedited compression options");
        dataDict.put("production_plans", "500 manufacturing production schedules restricted strictly to PLANT locations");
        dataDict.put("shortages", "100 time-phased projected deficits with severity classifications and root cause indicators");
        dataDict.put("mitigation_options", "200 ranked tactical mitigation proposals (PO expediting, alternative suppliers) with structured arrival dates and quantities");

        ExportManifest manifest = new ExportManifest(
                "YottaFlex Supply Chain Planning and Synthetic Data Generation System",
                "1.0.0-SNAPSHOT",
                LocalDate.now().toString(),
                seed,
                locations.size(),
                suppliers.size(),
                skus.size(),
                52,
                forecasts.size(),
                purchaseOrders.size(),
                inventories.size(),
                capacities.size(),
                leadTimes.size(),
                productionPlans.size(),
                shortages.size(),
                mitigations.size(),
                checksums,
                sizes,
                dataDict
        );

        objectMapper.writeValue(exportBaseDir.resolve("manifest.json").toFile(), manifest);
        log.info("Challenge benchmark dataset export completed successfully into {}", exportBaseDir);

        return manifest;
    }

    public Optional<ExportManifest> getManifest(Path exportBaseDir) {
        Path manifestPath = exportBaseDir.resolve("manifest.json");
        if (!Files.exists(manifestPath)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(manifestPath.toFile(), ExportManifest.class));
        } catch (IOException e) {
            log.error("Failed to read manifest from {}", manifestPath, e);
            return Optional.empty();
        }
    }

    public Path resolveExportFile(Path exportBaseDir, String format, String filename) {
        // Sanitize to prevent directory traversal
        String safeFormat = format.replaceAll("[^a-zA-Z0-9_-]", "");
        String safeFilename = Paths.get(filename).getFileName().toString();
        
        Path target;
        if ("root".equalsIgnoreCase(safeFormat) || safeFormat.isEmpty()) {
            target = exportBaseDir.resolve(safeFilename);
        } else {
            target = exportBaseDir.resolve(safeFormat).resolve(safeFilename);
        }
        
        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw new IllegalArgumentException("Requested export file does not exist: " + target);
        }
        return target;
    }

    // =========================================================================
    // Helper IO & Hashing Functions
    // =========================================================================

    private <T> void writeCsv(Path path, String header, List<T> items, java.util.function.Function<T, String> rowMapper) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(header);
            writer.newLine();
            for (T item : items) {
                writer.write(rowMapper.apply(item));
                writer.newLine();
            }
        }
    }

    private void writeSqlInserts(Path sqlPath,
                                 List<LocationExport> locations,
                                 List<SupplierExport> suppliers,
                                 List<SkuExport> skus,
                                 List<InventoryExport> inventories,
                                 List<ForecastDemandExport> forecasts,
                                 List<SupplierCapacityExport> capacities,
                                 List<LeadTimeExport> leadTimes,
                                 List<PurchaseOrderExport> purchaseOrders,
                                 List<ProductionPlanExport> productionPlans,
                                 List<ShortageExport> shortages,
                                 List<MitigationOptionExport> mitigations) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(sqlPath, StandardCharsets.UTF_8)) {
            w.write("-- ============================================================================\n");
            w.write("-- YottaFlex Supply Chain System: Synthetic Challenge Benchmark SQL Dump\n");
            w.write("-- 50 Locations, 500 Suppliers, 3,000 SKUs, 52 Weeks Forecast, 10,000 Purchase Orders\n");
            w.write("-- ============================================================================\n\n");
            w.write("SET FOREIGN_KEY_CHECKS = 0;\n\n");

            // Locations
            w.write("-- 1. Locations\n");
            for (LocationExport l : locations) {
                w.write(String.format("INSERT INTO `locations` (`id`, `location_code`, `name`, `type`, `city`, `state`, `country`, `postal_code`, `address`, `capacity`, `active`, `created_at`, `updated_at`) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %.2f, %b, NOW(), NOW()) ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);\n",
                        l.id(), l.locationCode(), escapeSql(l.name()), l.type(), escapeSql(l.city()), l.state(), l.country(), l.postalCode(), escapeSql(l.address()), l.capacity(), l.active()));
            }

            // Suppliers
            w.write("\n-- 2. Suppliers\n");
            for (SupplierExport s : suppliers) {
                w.write(String.format("INSERT INTO `suppliers` (`id`, `supplier_code`, `name`, `contact_person`, `email`, `phone`, `country`, `rating`, `reliability_score`, `active`, `created_at`, `updated_at`) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', %.1f, %.1f, %b, NOW(), NOW()) ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);\n",
                        s.id(), s.supplierCode(), escapeSql(s.name()), escapeSql(s.contactPerson()), s.email(), s.phone(), s.country(), s.rating(), s.reliabilityScore(), s.active()));
            }

            // SKUs (First 100 as sample in SQL script to keep manageable)
            w.write("\n-- 3. SKUs (Sample batch)\n");
            int skuLimit = Math.min(250, skus.size());
            for (int i = 0; i < skuLimit; i++) {
                SkuExport k = skus.get(i);
                w.write(String.format("INSERT INTO `skus` (`id`, `sku_code`, `name`, `description`, `category`, `unit_of_measure`, `unit_cost`, `selling_price`, `reorder_point`, `safety_stock`, `lead_time_days`, `active`, `created_at`, `updated_at`) VALUES (%d, '%s', '%s', '%s', '%s', '%s', %.2f, %.2f, %d, %d, %d, %b, NOW(), NOW()) ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);\n",
                        k.id(), k.skuCode(), escapeSql(k.name()), escapeSql(k.description()), k.category(), k.unitOfMeasure(), k.unitCost(), k.sellingPrice(), k.reorderPoint(), k.safetyStock(), k.leadTimeDays(), k.active()));
            }

            // Purchase Orders (Sample batch)
            w.write("\n-- 4. Inbound Purchase Orders (Sample batch)\n");
            int poLimit = Math.min(250, purchaseOrders.size());
            for (int i = 0; i < poLimit; i++) {
                PurchaseOrderExport p = purchaseOrders.get(i);
                w.write(String.format("INSERT INTO `purchase_orders` (`id`, `po_number`, `supplier_id`, `sku_id`, `destination_location_id`, `order_date`, `expected_delivery_date`, `actual_delivery_date`, `quantity_ordered`, `quantity_received`, `unit_price`, `status`, `notes`, `created_at`, `updated_at`) VALUES (%d, '%s', 1, 1, 1, '%s', '%s', %s, %.2f, %.2f, %.2f, '%s', '%s', NOW(), NOW()) ON DUPLICATE KEY UPDATE `status`=VALUES(`status`);\n",
                        p.id(), p.poNumber(), p.orderDate(), p.expectedDeliveryDate(), p.actualDeliveryDate() != null ? "'" + p.actualDeliveryDate() + "'" : "NULL", p.quantityOrdered(), p.quantityReceived(), p.unitPrice(), p.status(), escapeSql(p.notes())));
            }

            w.write("\nSET FOREIGN_KEY_CHECKS = 1;\n");
        }
    }

    private String escapeSql(String s) {
        if (s == null) return "";
        return s.replace("'", "''");
    }

    public static String calculateSha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(file);
            byte[] hash = digest.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IOException("Failed to calculate SHA-256 checksum", e);
        }
    }
}
