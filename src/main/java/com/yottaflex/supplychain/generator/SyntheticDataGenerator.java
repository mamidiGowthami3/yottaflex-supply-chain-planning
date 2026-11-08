package com.yottaflex.supplychain.generator;

import com.yottaflex.supplychain.dto.generator.DependentDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.DependentDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.DependentDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.DependentDataPreviewResponse.ForecastDemandPreviewItem;
import com.yottaflex.supplychain.dto.generator.DependentDataPreviewResponse.InventoryPreviewItem;
import com.yottaflex.supplychain.dto.generator.DependentDataPreviewResponse.LeadTimePreviewItem;
import com.yottaflex.supplychain.dto.generator.DependentDataPreviewResponse.SupplierCapacityPreviewItem;
import com.yottaflex.supplychain.dto.generator.DependentDataStatisticsResponse;
import com.yottaflex.supplychain.dto.generator.MasterDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.MasterDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.MasterDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.MasterDataStatisticsResponse;
import com.yottaflex.supplychain.dto.generator.PlanningDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.PlanningDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.PlanningDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.PlanningDataPreviewResponse.ProductionPlanPreviewItem;
import com.yottaflex.supplychain.dto.generator.PlanningDataPreviewResponse.PurchaseOrderPreviewItem;
import com.yottaflex.supplychain.dto.generator.PlanningDataStatisticsResponse;
import com.yottaflex.supplychain.entity.ForecastDemand;
import com.yottaflex.supplychain.entity.Inventory;
import com.yottaflex.supplychain.entity.LeadTime;
import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.entity.ProductionPlan;
import com.yottaflex.supplychain.entity.PurchaseOrder;
import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.entity.Supplier;
import com.yottaflex.supplychain.entity.SupplierCapacity;
import com.yottaflex.supplychain.entity.enums.DemandPattern;
import com.yottaflex.supplychain.entity.enums.DemandPeriodType;
import com.yottaflex.supplychain.entity.enums.LocationType;
import com.yottaflex.supplychain.entity.enums.ProductionPlanStatus;
import com.yottaflex.supplychain.entity.enums.PurchaseOrderStatus;
import com.yottaflex.supplychain.entity.enums.TransportMode;
import com.yottaflex.supplychain.exception.BusinessValidationException;
import com.yottaflex.supplychain.repository.ForecastDemandRepository;
import com.yottaflex.supplychain.repository.InventoryRepository;
import com.yottaflex.supplychain.repository.LeadTimeRepository;
import com.yottaflex.supplychain.repository.LocationRepository;
import com.yottaflex.supplychain.repository.ProductionPlanRepository;
import com.yottaflex.supplychain.repository.PurchaseOrderRepository;
import com.yottaflex.supplychain.repository.SKURepository;
import com.yottaflex.supplychain.repository.SupplierCapacityRepository;
import com.yottaflex.supplychain.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Service responsible for deterministic synthetic data generation for:
 * 1. Master Data entities (Location, Supplier, SKU).
 * 2. Dependent Transactional entities (Inventory, ForecastDemand, SupplierCapacity, LeadTime).
 * 3. Planning entities (PurchaseOrder, ProductionPlan).
 *
 * Adheres to:
 * - Deterministic random generation using configurable seeds.
 * - Strict constraints and business rules (pricing markup, stock buffers, positive capacities, lead times).
 * - Non-destructive execution: zero overwrites or deletions of existing records.
 * - Collision prevention with dynamic sequence resolution and uniqueness checks.
 * - Clear separation of in-memory preview (read-only) and persistent generation.
 */
@Service
public class SyntheticDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(SyntheticDataGenerator.class);

    public static final String LOCATION_CODE_PREFIX = "LOC-SYN-";
    public static final String SUPPLIER_CODE_PREFIX = "SUP-SYN-";
    public static final String SKU_CODE_PREFIX = "SKU-SYN-";
    public static final String PO_CODE_PREFIX = "PO-SYN-";
    public static final String PLAN_CODE_PREFIX = "PP-SYN-";

    private final LocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final SKURepository skuRepository;
    private final InventoryRepository inventoryRepository;
    private final ForecastDemandRepository forecastDemandRepository;
    private final SupplierCapacityRepository supplierCapacityRepository;
    private final LeadTimeRepository leadTimeRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductionPlanRepository productionPlanRepository;

    public SyntheticDataGenerator(LocationRepository locationRepository,
                                  SupplierRepository supplierRepository,
                                  SKURepository skuRepository,
                                  InventoryRepository inventoryRepository,
                                  ForecastDemandRepository forecastDemandRepository,
                                  SupplierCapacityRepository supplierCapacityRepository,
                                  LeadTimeRepository leadTimeRepository,
                                  PurchaseOrderRepository purchaseOrderRepository,
                                  ProductionPlanRepository productionPlanRepository) {
        this.locationRepository = locationRepository;
        this.supplierRepository = supplierRepository;
        this.skuRepository = skuRepository;
        this.inventoryRepository = inventoryRepository;
        this.forecastDemandRepository = forecastDemandRepository;
        this.supplierCapacityRepository = supplierCapacityRepository;
        this.leadTimeRepository = leadTimeRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productionPlanRepository = productionPlanRepository;
        log.info("SyntheticDataGenerator initialized for Master, Dependent, and Planning Data entities.");
    }

    // =========================================================================
    // Master Data Realistic Template Definitions
    // =========================================================================

    private static final String[][] LOCATION_TEMPLATES = {
        {"North Distribution Center", "Chicago", "IL", "USA", "60601", "100 Logistics Blvd"},
        {"Central Fulfillment Hub", "Dallas", "TX", "USA", "75201", "450 Supply Chain Way"},
        {"Pacific Assembly Plant", "Seattle", "WA", "USA", "98101", "789 Industrial Pkwy"},
        {"Southeast Cargo Terminal", "Atlanta", "GA", "USA", "30301", "300 Freight Corridor"},
        {"Midwest Logistics Depot", "Columbus", "OH", "USA", "43201", "550 Gateway Circle"},
        {"Southwest High-Tech Plant", "Phoenix", "AZ", "USA", "85001", "120 Silicon Ave"},
        {"Northeast Regional Terminal", "Philadelphia", "PA", "USA", "19101", "840 Port Access Rd"},
        {"Inland Distribution Hub", "Memphis", "TN", "USA", "38101", "900 Express Way"},
        {"Rocky Mountain Logistics Park", "Denver", "CO", "USA", "80201", "210 Alpine Blvd"},
        {"Advanced Technology Facility", "Austin", "TX", "USA", "78701", "670 Innovation Drive"},
        {"Great Lakes Assembly Works", "Detroit", "MI", "USA", "48201", "500 Automotive Pkwy"},
        {"Gateway Cross-Dock Terminal", "Miami", "FL", "USA", "33101", "320 Maritime Ave"},
        {"Piedmont Logistics Center", "Charlotte", "NC", "USA", "28201", "410 Interstate Way"},
        {"Heartland Cargo Terminal", "Kansas City", "MO", "USA", "64101", "730 Railhead Blvd"},
        {"Crossroads Logistics Depot", "Indianapolis", "IN", "USA", "46201", "620 Distribution Dr"},
        {"Intermountain Storage Depot", "Salt Lake City", "UT", "USA", "84101", "180 Wasatch Rd"},
        {"Music City Fulfillment Hub", "Nashville", "TN", "USA", "37201", "290 Commerce Way"},
        {"Columbia River Assembly Plant", "Portland", "OR", "USA", "97201", "880 Rivergate Pkwy"},
        {"Upper Midwest Distribution Center", "Minneapolis", "MN", "USA", "55401", "350 North Star Ave"},
        {"Gulf Coast Petro-Chemical Depot", "Houston", "TX", "USA", "77001", "990 Navigation Blvd"}
    };

    private static final String[][] SUPPLIER_TEMPLATES = {
        {"Apex Precision Components Ltd", "Sarah Jenkins", "USA"},
        {"Vanguard Microelectronics Inc", "Kenji Takahashi", "Japan"},
        {"Titan Industrial Fasteners", "Marcus Vance", "USA"},
        {"Global Polymer Technologies GmbH", "Lukas Weber", "Germany"},
        {"Nexus Semiconductor Corp", "Elena Rostova", "Canada"},
        {"Pinnacle Alloys & Materials", "David Chen", "Taiwan"},
        {"AeroTech Composites Group", "Claire Dubois", "France"},
        {"Quantum Optic Sensors Ltd", "Min-Jun Kim", "South Korea"},
        {"Omni Packaging Solutions", "Carlos Mendez", "Mexico"},
        {"Sterling Metal Fabrication", "Arthur Pendelton", "UK"},
        {"Echo Power Electronics", "Sunita Sharma", "India"},
        {"Cascade Hydraulics & Valves", "Robert Lindqvist", "Sweden"},
        {"Summit Precision Tooling", "Paolo Rossi", "Italy"},
        {"Frontier Rubber & Gaskets", "Hans Zimmer", "Germany"},
        {"Horizon Circuit Technologies", "Wei Zhang", "Taiwan"},
        {"Matrix Advanced Materials", "Rachel Adams", "USA"},
        {"Dynamic Sensor Systems", "Hiroshi Tanaka", "Japan"},
        {"Kinetics Fluid Solutions", "Mateo Silva", "Brazil"},
        {"Spectra Thermal Dynamics", "Oliver Smith", "UK"},
        {"Vertex Industrial Supply", "Anja Mueller", "Germany"},
        {"Delta Automation Works", "Liam O'Connor", "Ireland"},
        {"Precision Motion Controls", "Victor Hugo", "France"},
        {"Pioneer Micro Devices", "Jin-Woo Park", "South Korea"},
        {"Nordic Alloy Foundries", "Erik Lindgren", "Sweden"},
        {"Pacific Cable & Harness", "Thuy Nguyen", "Vietnam"},
        {"Alpine Mechatronics AG", "Beat Keller", "Switzerland"},
        {"Orion Photonics Inc", "Alice Cooper", "USA"},
        {"Atlas Heavy Forging", "Sven Hansen", "Denmark"},
        {"Zenith Fluid Power", "Mohamed Al-Fassi", "UAE"},
        {"Novus Smart Sensors", "Jan Novak", "Czech Republic"},
        {"Prime Magnetics Corp", "Klaus Fischer", "Austria"},
        {"Solaria High-Temp Glass", "Sofia Gomez", "Spain"},
        {"Axiom Industrial Bearings", "Dmitri Volkov", "Poland"},
        {"BlueShift Microcircuits", "Grace Hopper", "USA"},
        {"Titanium Tech Dynamics", "Taro Yamada", "Japan"},
        {"Centaur Heavy Pumps", "Bruce Wayne", "USA"},
        {"Valence Battery Systems", "Marie Curie", "France"},
        {"Beacon Signal Electronics", "Niels Bohr", "Denmark"},
        {"Cobalt Chemical Reagents", "Alan Turing", "UK"},
        {"Hyperion Optoelectronic", "Ada Lovelace", "UK"},
        {"Venture Polymer Extrusions", "Gabriel Santos", "Brazil"},
        {"IronClad Fastener Works", "Jack Dempsey", "USA"},
        {"Terra Thermal Insulation", "Frederik De Smet", "Belgium"},
        {"Strata Geological Minerals", "Chloe Bennett", "Australia"},
        {"Aether Microwave Systems", "Sean MacLeod", "Ireland"},
        {"Polaris Precision Gears", "Bjorn Borg", "Sweden"},
        {"Aura Luminescent Displays", "Hanna Arendt", "Germany"},
        {"Pulse Actuation Robotics", "Isaac Asimov", "USA"},
        {"Crestview Industrial Seals", "Leonard Euler", "Switzerland"},
        {"Keystone Structural Steel", "Andrew Carnegie", "USA"}
    };

    private record SkuTemplateDef(String category, String name, String description, String uom, double baseCost, double basePrice) {}

    private static final SkuTemplateDef[] SKU_TEMPLATES = {
        new SkuTemplateDef("Electronics", "Microcontroller Board v4", "High-performance dual-core industrial microcontroller", "UNITS", 24.50, 48.00),
        new SkuTemplateDef("Electronics", "Power MOSFET Module 100A", "High-efficiency solid-state power switching module", "UNITS", 12.00, 26.50),
        new SkuTemplateDef("Electronics", "Optocoupler SMD High-Speed", "Digital optical isolator 25Mbps SMD-8", "UNITS", 3.20, 7.80),
        new SkuTemplateDef("Electronics", "Voltage Regulator LDO 5V", "Ultra low-dropout linear voltage regulator", "UNITS", 1.80, 4.50),
        new SkuTemplateDef("Electronics", "Industrial Pressure Sensor CAN", "Precision pressure transmitter with CAN-bus interface", "UNITS", 65.00, 135.00),
        new SkuTemplateDef("Industrial Hardware", "Hex Bolt M8 Grade 8.8", "High-tensile zinc-plated metric hex bolts", "BOXES", 14.00, 28.00),
        new SkuTemplateDef("Industrial Hardware", "Stainless Ball Bearing 608RS", "Sealed deep-groove precision miniature ball bearing", "UNITS", 4.50, 9.90),
        new SkuTemplateDef("Industrial Hardware", "Flange Nut M10 Zinc-Plated", "Serrated self-locking metric flange nuts", "BOXES", 18.00, 34.00),
        new SkuTemplateDef("Industrial Hardware", "Precision Dowel Pin 6x30mm", "Hardened alloy ground cylindrical dowel pins", "BOXES", 11.50, 22.00),
        new SkuTemplateDef("Industrial Hardware", "Steel Compression Spring Heavy", "Heat-treated high-durability coil compression spring", "UNITS", 6.80, 14.50),
        new SkuTemplateDef("Raw Materials", "Aluminum Sheet 6061-T6 4x8ft", "Aerospace-grade high-strength aluminum alloy sheet", "SHEETS", 85.00, 160.00),
        new SkuTemplateDef("Raw Materials", "Copper Wire Spool 14AWG 500ft", "Oxygen-free solid bare conductor copper spool", "ROLLS", 95.00, 175.00),
        new SkuTemplateDef("Raw Materials", "Silicone Rubber Gasket Sheet 3mm", "High-temperature resistant vulcanized silicone sheet", "SHEETS", 32.00, 68.00),
        new SkuTemplateDef("Raw Materials", "Carbon Fiber Rod 10mm x 1m", "Pultruded high-modulus structural carbon fiber tube", "METERS", 28.00, 58.00),
        new SkuTemplateDef("Raw Materials", "Polycarbonate Pellets Resin 25kg", "Virgin injection-molding optical grade granules", "BAGS", 55.00, 110.00),
        new SkuTemplateDef("Automotive Parts", "Brake Rotor Disc 280mm Vented", "Cast-iron precision dynamically balanced brake rotor", "UNITS", 42.00, 89.00),
        new SkuTemplateDef("Automotive Parts", "Timing Belt 120T High-Torque", "Fiberglass reinforced chloroprene automotive belt", "UNITS", 16.50, 36.00),
        new SkuTemplateDef("Automotive Parts", "Fuel Injector Nozzle Piezo", "High-precision common-rail diesel injection nozzle", "UNITS", 75.00, 155.00),
        new SkuTemplateDef("Automotive Parts", "Oxygen Sensor Bosch Type", "Broadband planar lambda sensor with heater", "UNITS", 38.00, 82.00),
        new SkuTemplateDef("Automotive Parts", "Cabin Air Filter HEPA Activated", "Multi-stage electrostatically charged vehicle air filter", "UNITS", 8.50, 22.00),
        new SkuTemplateDef("Packaging", "Corrugated Box 12x12x12 Bundle", "Heavy-duty 200lb test kraft corrugated cartons", "BUNDLES", 22.00, 44.00),
        new SkuTemplateDef("Packaging", "Anti-Static Bubble Wrap 250ft", "Pink amine-free ESD protective air cushion roll", "ROLLS", 35.00, 72.00),
        new SkuTemplateDef("Packaging", "Thermal Transfer Label Roll 4x6", "Ultra-adhesive direct thermal shipping labels", "ROLLS", 12.50, 26.00),
        new SkuTemplateDef("Packaging", "Heavy-Duty Packing Tape 6pk", "Acrylic adhesive water-resistant box packaging tape", "PACKS", 15.00, 31.00),
        new SkuTemplateDef("Packaging", "Pallet Stretch Film 80 Gauge", "High-clarity cast hand wrap pallet stretch film", "ROLLS", 19.00, 41.00),
        new SkuTemplateDef("Hydraulics", "Directional Control Valve 24V", "Pilot-operated hydraulic 4-way spool valve", "UNITS", 145.00, 290.00),
        new SkuTemplateDef("Hydraulics", "Hydraulic Cylinder 50x200mm", "Double-acting tie-rod hydraulic industrial cylinder", "UNITS", 110.00, 235.00),
        new SkuTemplateDef("Hydraulics", "High-Pressure Hose SAE 100R2", "Two-wire braided synthetic rubber hydraulic hose", "METERS", 18.00, 42.00),
        new SkuTemplateDef("Optoelectronics", "Infrared Phototransistor Array", "High-sensitivity NPN silicon phototransistor 940nm", "UNITS", 5.50, 13.00),
        new SkuTemplateDef("Optoelectronics", "Laser Diode Module 650nm 5mW", "Focusable red point industrial laser generator", "UNITS", 14.00, 32.00)
    };

    // =========================================================================
    // Master Data Public API Methods (Milestone 2 Step 2)
    // =========================================================================

    public MasterDataPreviewResponse previewMasterData(MasterDataGenerationRequest request) {
        validateMasterRequest(request);

        long seed = request.getSeed();
        Random locRandom = new Random(seed);
        Random supRandom = new Random(seed + 1000L);
        Random skuRandom = new Random(seed + 2000L);

        List<Location> locations = generateLocations(request.getLocationCount(), locRandom, true);
        List<Supplier> suppliers = generateSuppliers(request.getSupplierCount(), supRandom, true);
        List<SKU> skus = generateSkus(request.getSkuCount(), skuRandom, true);

        log.info("Generated master data preview: {} locations, {} suppliers, {} SKUs (seed: {})",
                locations.size(), suppliers.size(), skus.size(), seed);

        return new MasterDataPreviewResponse(seed, locations, suppliers, skus);
    }

    @Transactional
    public MasterDataGenerationResult generateMasterData(MasterDataGenerationRequest request) {
        validateMasterRequest(request);

        long startTime = System.currentTimeMillis();
        long seed = request.getSeed();

        Random locRandom = new Random(seed);
        Random supRandom = new Random(seed + 1000L);
        Random skuRandom = new Random(seed + 2000L);

        List<Location> locations = generateLocations(request.getLocationCount(), locRandom, false);
        List<Supplier> suppliers = generateSuppliers(request.getSupplierCount(), supRandom, false);
        List<SKU> skus = generateSkus(request.getSkuCount(), skuRandom, false);

        List<Location> savedLocations = locationRepository.saveAll(locations);
        List<Supplier> savedSuppliers = supplierRepository.saveAll(suppliers);
        List<SKU> savedSkus = skuRepository.saveAll(skus);

        long durationMs = System.currentTimeMillis() - startTime;

        List<String> locCodes = savedLocations.stream().map(Location::getLocationCode).toList();
        List<String> supCodes = savedSuppliers.stream().map(Supplier::getSupplierCode).toList();
        List<String> skuCodes = savedSkus.stream().map(SKU::getSkuCode).toList();

        log.info("Persisted synthetic master data successfully: {} locations, {} suppliers, {} SKUs in {} ms",
                savedLocations.size(), savedSuppliers.size(), savedSkus.size(), durationMs);

        return new MasterDataGenerationResult(
                seed,
                savedLocations.size(),
                savedSuppliers.size(),
                savedSkus.size(),
                durationMs,
                locCodes,
                supCodes,
                skuCodes
        );
    }

    public MasterDataStatisticsResponse getMasterDataStatistics() {
        long totalLocations = locationRepository.count();
        long totalSuppliers = supplierRepository.count();
        long totalSkus = skuRepository.count();

        long syntheticLocations = locationRepository.countByLocationCodeStartingWith(LOCATION_CODE_PREFIX);
        long syntheticSuppliers = supplierRepository.countBySupplierCodeStartingWith(SUPPLIER_CODE_PREFIX);
        long syntheticSkus = skuRepository.countBySkuCodeStartingWith(SKU_CODE_PREFIX);

        return new MasterDataStatisticsResponse(
                totalLocations,
                totalSuppliers,
                totalSkus,
                syntheticLocations,
                syntheticSuppliers,
                syntheticSkus
        );
    }

    // =========================================================================
    // Dependent Transactional Data Public API Methods (Milestone 2 Step 3)
    // =========================================================================

    @Transactional(readOnly = true)
    public DependentDataPreviewResponse previewDependentData(DependentDataGenerationRequest request) {
        validateDependentRequest(request);

        List<Location> locations = locationRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SKU> skus = skuRepository.findAll();
        validateMasterDataAvailability(locations, suppliers, skus);

        long seed = request.getSeed();
        Random invRandom = new Random(seed + 3000L);
        Random fdRandom = new Random(seed + 4000L);
        Random scRandom = new Random(seed + 5000L);
        Random ltRandom = new Random(seed + 6000L);

        List<Inventory> inventories = generateInventories(request.getInventoryCount(), invRandom, true, locations, skus);
        List<ForecastDemand> forecastDemands = generateForecastDemands(request.getForecastDemandCount(), fdRandom, true, locations, skus);
        List<SupplierCapacity> supplierCapacities = generateSupplierCapacities(request.getSupplierCapacityCount(), scRandom, true, suppliers, skus);
        List<LeadTime> leadTimes = generateLeadTimes(request.getLeadTimeCount(), ltRandom, true, suppliers, skus, locations);

        List<InventoryPreviewItem> previewInv = inventories.stream().map(InventoryPreviewItem::from).toList();
        List<ForecastDemandPreviewItem> previewFd = forecastDemands.stream().map(ForecastDemandPreviewItem::from).toList();
        List<SupplierCapacityPreviewItem> previewSc = supplierCapacities.stream().map(SupplierCapacityPreviewItem::from).toList();
        List<LeadTimePreviewItem> previewLt = leadTimes.stream().map(LeadTimePreviewItem::from).toList();

        log.info("Generated dependent data preview: {} inv, {} demands, {} capacities, {} lead-times (seed: {})",
                previewInv.size(), previewFd.size(), previewSc.size(), previewLt.size(), seed);

        return new DependentDataPreviewResponse(seed, previewInv, previewFd, previewSc, previewLt);
    }

    @Transactional
    public DependentDataGenerationResult generateDependentData(DependentDataGenerationRequest request) {
        validateDependentRequest(request);

        List<Location> locations = locationRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SKU> skus = skuRepository.findAll();
        validateMasterDataAvailability(locations, suppliers, skus);

        long startTime = System.currentTimeMillis();
        long seed = request.getSeed();

        Random invRandom = new Random(seed + 3000L);
        Random fdRandom = new Random(seed + 4000L);
        Random scRandom = new Random(seed + 5000L);
        Random ltRandom = new Random(seed + 6000L);

        List<Inventory> inventories = generateInventories(request.getInventoryCount(), invRandom, false, locations, skus);
        List<ForecastDemand> forecastDemands = generateForecastDemands(request.getForecastDemandCount(), fdRandom, false, locations, skus);
        List<SupplierCapacity> supplierCapacities = generateSupplierCapacities(request.getSupplierCapacityCount(), scRandom, false, suppliers, skus);
        List<LeadTime> leadTimes = generateLeadTimes(request.getLeadTimeCount(), ltRandom, false, suppliers, skus, locations);

        List<Inventory> savedInventories = inventoryRepository.saveAll(inventories);
        List<ForecastDemand> savedForecastDemands = forecastDemandRepository.saveAll(forecastDemands);
        List<SupplierCapacity> savedSupplierCapacities = supplierCapacityRepository.saveAll(supplierCapacities);
        List<LeadTime> savedLeadTimes = leadTimeRepository.saveAll(leadTimes);

        long durationMs = System.currentTimeMillis() - startTime;

        List<String> invSummaries = savedInventories.stream()
                .map(i -> String.format("SKU: %s @ LOC: %s | OnHand: %s | Avail: %s | Status: %s",
                        i.getSku().getSkuCode(), i.getLocation().getLocationCode(),
                        i.getQuantityOnHand(), i.getQuantityAvailable(), i.getStatus()))
                .toList();

        List<String> fdSummaries = savedForecastDemands.stream()
                .map(f -> String.format("SKU: %s @ LOC: %s | Date: %s (%s) | Qty: %s | Conf: %.2f",
                        f.getSku().getSkuCode(), f.getLocation().getLocationCode(),
                        f.getForecastDate(), f.getPeriodType(), f.getForecastQuantity(), f.getConfidenceScore()))
                .toList();

        List<String> scSummaries = savedSupplierCapacities.stream()
                .map(c -> String.format("SUP: %s -> SKU: %s | %s to %s | Daily: %s | Monthly: %s | Avail: %s",
                        c.getSupplier().getSupplierCode(), c.getSku().getSkuCode(),
                        c.getEffectiveStartDate(), c.getEffectiveEndDate(),
                        c.getDailyCapacity(), c.getMonthlyCapacity(), c.getAvailableCapacity()))
                .toList();

        List<String> ltSummaries = savedLeadTimes.stream()
                .map(l -> String.format("SUP: %s -> SKU: %s -> LOC: %s | Std: %dd | Expedited: %dd | Mode: %s",
                        l.getSupplier().getSupplierCode(), l.getSku().getSkuCode(),
                        l.getDestinationLocation().getLocationCode(),
                        l.getStandardDays(), l.getExpeditedDays(), l.getTransportMode()))
                .toList();

        log.info("Persisted synthetic dependent data successfully: {} inv, {} demands, {} capacities, {} lead-times in {} ms",
                savedInventories.size(), savedForecastDemands.size(), savedSupplierCapacities.size(), savedLeadTimes.size(), durationMs);

        return new DependentDataGenerationResult(
                seed,
                savedInventories.size(),
                savedForecastDemands.size(),
                savedSupplierCapacities.size(),
                savedLeadTimes.size(),
                durationMs,
                invSummaries,
                fdSummaries,
                scSummaries,
                ltSummaries
        );
    }

    public DependentDataStatisticsResponse getDependentDataStatistics() {
        long totalInventories = inventoryRepository.count();
        long totalForecastDemands = forecastDemandRepository.count();
        long totalSupplierCapacities = supplierCapacityRepository.count();
        long totalLeadTimes = leadTimeRepository.count();

        long syntheticInventories = inventoryRepository.countBySyntheticSku(SKU_CODE_PREFIX);
        long syntheticForecastDemands = forecastDemandRepository.countBySyntheticSku(SKU_CODE_PREFIX);
        long syntheticSupplierCapacities = supplierCapacityRepository.countBySyntheticSupplier(SUPPLIER_CODE_PREFIX);
        long syntheticLeadTimes = leadTimeRepository.countBySyntheticSupplier(SUPPLIER_CODE_PREFIX);

        return new DependentDataStatisticsResponse(
                totalInventories,
                totalForecastDemands,
                totalSupplierCapacities,
                totalLeadTimes,
                syntheticInventories,
                syntheticForecastDemands,
                syntheticSupplierCapacities,
                syntheticLeadTimes
        );
    }

    // =========================================================================
    // Planning Data Public API Methods (Milestone 2 Step 4)
    // =========================================================================

    @Transactional(readOnly = true)
    public PlanningDataPreviewResponse previewPlanningData(PlanningDataGenerationRequest request) {
        validatePlanningRequest(request);

        List<Location> locations = locationRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SKU> skus = skuRepository.findAll();
        List<LeadTime> leadTimes = leadTimeRepository.findAll();
        List<SupplierCapacity> capacities = supplierCapacityRepository.findAll();
        validateMasterDataAvailability(locations, suppliers, skus);

        long seed = request.getSeed();
        Random poRandom = new Random(seed + 7000L);
        Random planRandom = new Random(seed + 8000L);

        List<PurchaseOrder> purchaseOrders = generatePurchaseOrders(request.getPurchaseOrderCount(), poRandom, true,
                suppliers, skus, locations, leadTimes, capacities);
        List<ProductionPlan> productionPlans = generateProductionPlans(request.getProductionPlanCount(), planRandom, true,
                skus, locations);

        List<PurchaseOrderPreviewItem> previewPOs = purchaseOrders.stream().map(PurchaseOrderPreviewItem::from).toList();
        List<ProductionPlanPreviewItem> previewPlans = productionPlans.stream().map(ProductionPlanPreviewItem::from).toList();

        log.info("Generated planning data preview: {} POs, {} plans (seed: {})",
                previewPOs.size(), previewPlans.size(), seed);

        return new PlanningDataPreviewResponse(seed, previewPOs, previewPlans);
    }

    @Transactional
    public PlanningDataGenerationResult generatePlanningData(PlanningDataGenerationRequest request) {
        validatePlanningRequest(request);

        List<Location> locations = locationRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SKU> skus = skuRepository.findAll();
        List<LeadTime> leadTimes = leadTimeRepository.findAll();
        List<SupplierCapacity> capacities = supplierCapacityRepository.findAll();
        validateMasterDataAvailability(locations, suppliers, skus);

        long startTime = System.currentTimeMillis();
        long seed = request.getSeed();

        Random poRandom = new Random(seed + 7000L);
        Random planRandom = new Random(seed + 8000L);

        List<PurchaseOrder> purchaseOrders = generatePurchaseOrders(request.getPurchaseOrderCount(), poRandom, false,
                suppliers, skus, locations, leadTimes, capacities);
        List<ProductionPlan> productionPlans = generateProductionPlans(request.getProductionPlanCount(), planRandom, false,
                skus, locations);

        List<PurchaseOrder> savedPOs = purchaseOrderRepository.saveAll(purchaseOrders);
        List<ProductionPlan> savedPlans = productionPlanRepository.saveAll(productionPlans);

        long durationMs = System.currentTimeMillis() - startTime;

        List<String> poSummaries = savedPOs.stream()
                .map(p -> String.format("PO: %s | SUP: %s -> SKU: %s -> LOC: %s | Qty: %s | Status: %s | Expected: %s",
                        p.getPoNumber(), p.getSupplier().getSupplierCode(), p.getSku().getSkuCode(),
                        p.getDestinationLocation().getLocationCode(), p.getQuantityOrdered(), p.getStatus(), p.getExpectedDeliveryDate()))
                .toList();

        List<String> planSummaries = savedPlans.stream()
                .map(pl -> String.format("PLAN: %s | SKU: %s @ LOC: %s | Planned: %s | Produced: %s | Status: %s | Dates: %s to %s",
                        pl.getPlanCode(), pl.getSku().getSkuCode(), pl.getLocation().getLocationCode(),
                        pl.getPlannedQuantity(), pl.getProducedQuantity(), pl.getStatus(), pl.getStartDate(), pl.getEndDate()))
                .toList();

        log.info("Persisted synthetic planning data successfully: {} POs, {} plans in {} ms",
                savedPOs.size(), savedPlans.size(), durationMs);

        return new PlanningDataGenerationResult(
                seed,
                savedPOs.size(),
                savedPlans.size(),
                durationMs,
                poSummaries,
                planSummaries
        );
    }

    public PlanningDataStatisticsResponse getPlanningDataStatistics() {
        long totalPOs = purchaseOrderRepository.count();
        long totalPlans = productionPlanRepository.count();

        long syntheticPOs = purchaseOrderRepository.countBySyntheticPoNumber(PO_CODE_PREFIX);
        long syntheticPlans = productionPlanRepository.countBySyntheticPlanCode(PLAN_CODE_PREFIX);

        return new PlanningDataStatisticsResponse(
                totalPOs,
                totalPlans,
                syntheticPOs,
                syntheticPlans
        );
    }

    // =========================================================================
    // Planning Generator Helpers
    // =========================================================================

    private void validatePlanningRequest(PlanningDataGenerationRequest request) {
        if (request == null) {
            throw new BusinessValidationException("Generation request payload cannot be null");
        }
        if (request.getPurchaseOrderCount() == null || request.getPurchaseOrderCount() < 0 ||
                request.getPurchaseOrderCount() > PlanningDataGenerationRequest.MAX_PO_COUNT) {
            throw new BusinessValidationException(String.format("Purchase order count must be between 0 and %d",
                    PlanningDataGenerationRequest.MAX_PO_COUNT));
        }
        if (request.getProductionPlanCount() == null || request.getProductionPlanCount() < 0 ||
                request.getProductionPlanCount() > PlanningDataGenerationRequest.MAX_PLAN_COUNT) {
            throw new BusinessValidationException(String.format("Production plan count must be between 0 and %d",
                    PlanningDataGenerationRequest.MAX_PLAN_COUNT));
        }
        if (request.getSeed() == null) {
            request.setSeed(PlanningDataGenerationRequest.DEFAULT_SEED);
        }
    }

    private List<PurchaseOrder> generatePurchaseOrders(int count, Random random, boolean isPreview,
                                                       List<Supplier> suppliers, List<SKU> skus,
                                                       List<Location> locations, List<LeadTime> leadTimes,
                                                       List<SupplierCapacity> capacities) {
        if (count == 0) return new ArrayList<>();

        List<PurchaseOrder> pos = new ArrayList<>(count);
        Set<String> batchCodes = new HashSet<>();
        int seq = 1;

        PurchaseOrderStatus[] statuses = {
                PurchaseOrderStatus.DELIVERED,
                PurchaseOrderStatus.DELIVERED,
                PurchaseOrderStatus.IN_TRANSIT,
                PurchaseOrderStatus.IN_TRANSIT,
                PurchaseOrderStatus.PLACED,
                PurchaseOrderStatus.PARTIALLY_DELIVERED,
                PurchaseOrderStatus.DRAFT,
                PurchaseOrderStatus.CANCELLED
        };

        for (int i = 0; i < count; i++) {
            String poNumber;
            if (isPreview) {
                poNumber = String.format("%s%04d", PO_CODE_PREFIX, i + 1);
            } else {
                poNumber = resolveNextAvailableCode(PO_CODE_PREFIX, 4, seq, batchCodes, purchaseOrderRepository::existsByPoNumber);
                seq++;
                batchCodes.add(poNumber);
            }

            Supplier supplier;
            SKU sku;
            Location destinationLocation;
            int leadDays;

            if (leadTimes != null && !leadTimes.isEmpty()) {
                LeadTime lt = leadTimes.get(i % leadTimes.size());
                supplier = lt.getSupplier();
                sku = lt.getSku();
                destinationLocation = lt.getDestinationLocation();
                leadDays = lt.getStandardDays() != null ? lt.getStandardDays() : 10;
            } else {
                supplier = suppliers.get(i % suppliers.size());
                sku = skus.get(i % skus.size());
                destinationLocation = locations.get(i % locations.size());
                leadDays = sku.getLeadTimeDays() != null ? sku.getLeadTimeDays() : 10;
            }

            // Check supplier capacity constraint if present
            BigDecimal maxCapacity = BigDecimal.valueOf(500.0);
            if (capacities != null && !capacities.isEmpty()) {
                for (SupplierCapacity cap : capacities) {
                    if (cap.getSupplier().getId() != null && cap.getSupplier().getId().equals(supplier.getId())
                            && cap.getSku().getId() != null && cap.getSku().getId().equals(sku.getId())) {
                        if (cap.getAvailableCapacity() != null && cap.getAvailableCapacity().compareTo(BigDecimal.ZERO) > 0) {
                            maxCapacity = cap.getAvailableCapacity();
                        }
                        break;
                    }
                }
            }

            double maxOrderable = Math.max(50.0, Math.min(1000.0, maxCapacity.doubleValue()));
            double qtyVal = 25.0 + (random.nextDouble() * (maxOrderable - 25.0));
            BigDecimal quantityOrdered = BigDecimal.valueOf(qtyVal).setScale(2, RoundingMode.HALF_UP);

            BigDecimal unitPrice = sku.getUnitCost() != null ? sku.getUnitCost() : BigDecimal.valueOf(25.00);

            PurchaseOrderStatus status = statuses[i % statuses.length];

            int daysAgo = 10 + random.nextInt(50);
            LocalDate orderDate = LocalDate.now().minusDays(daysAgo);
            LocalDate expectedDeliveryDate = orderDate.plusDays(leadDays);

            LocalDate actualDeliveryDate = null;
            BigDecimal quantityReceived = BigDecimal.ZERO;

            if (status == PurchaseOrderStatus.DELIVERED) {
                actualDeliveryDate = expectedDeliveryDate.plusDays(random.nextInt(5) - 2);
                if (actualDeliveryDate.isBefore(orderDate.plusDays(1))) {
                    actualDeliveryDate = orderDate.plusDays(1);
                }
                quantityReceived = quantityOrdered;
            } else if (status == PurchaseOrderStatus.PARTIALLY_DELIVERED) {
                actualDeliveryDate = expectedDeliveryDate.minusDays(1);
                double partialRatio = 0.30 + (random.nextDouble() * 0.40);
                quantityReceived = quantityOrdered.multiply(BigDecimal.valueOf(partialRatio)).setScale(2, RoundingMode.HALF_UP);
            } else {
                actualDeliveryDate = null;
                quantityReceived = BigDecimal.ZERO;
            }

            PurchaseOrder po = new PurchaseOrder(poNumber, supplier, sku, destinationLocation,
                    orderDate, expectedDeliveryDate, quantityOrdered, unitPrice);
            po.setActualDeliveryDate(actualDeliveryDate);
            po.setQuantityReceived(quantityReceived);
            po.setStatus(status);
            po.setNotes("Synthetic purchase order replenishment cycle");

            pos.add(po);
        }

        return pos;
    }

    private List<ProductionPlan> generateProductionPlans(int count, Random random, boolean isPreview,
                                                         List<SKU> skus, List<Location> locations) {
        if (count == 0) return new ArrayList<>();

        List<ProductionPlan> plans = new ArrayList<>(count);
        Set<String> batchCodes = new HashSet<>();
        int seq = 1;

        List<Location> plantLocations = locations.stream()
                .filter(l -> l.getType() == LocationType.PLANT)
                .toList();
        if (plantLocations.isEmpty()) {
            throw new BusinessValidationException("No locations with type PLANT found. Production plans require at least one manufacturing PLANT location.");
        }
        List<Location> eligibleLocations = plantLocations;

        ProductionPlanStatus[] statuses = {
                ProductionPlanStatus.COMPLETED,
                ProductionPlanStatus.IN_PROGRESS,
                ProductionPlanStatus.SCHEDULED,
                ProductionPlanStatus.SCHEDULED,
                ProductionPlanStatus.DRAFT,
                ProductionPlanStatus.HALTED
        };

        for (int i = 0; i < count; i++) {
            String planCode;
            if (isPreview) {
                planCode = String.format("%s%04d", PLAN_CODE_PREFIX, i + 1);
            } else {
                planCode = resolveNextAvailableCode(PLAN_CODE_PREFIX, 4, seq, batchCodes, productionPlanRepository::existsByPlanCode);
                seq++;
                batchCodes.add(planCode);
            }

            SKU sku = skus.get(i % skus.size());
            Location location = eligibleLocations.get(i % eligibleLocations.size());

            ProductionPlanStatus status = statuses[i % statuses.length];

            int startOffset = switch (status) {
                case COMPLETED, HALTED -> -(20 + random.nextInt(20));
                case IN_PROGRESS -> -(3 + random.nextInt(7));
                case SCHEDULED, DRAFT -> 3 + random.nextInt(25);
                case CANCELLED -> -(5 + random.nextInt(15));
            };

            LocalDate startDate = LocalDate.now().plusDays(startOffset);
            int durationDays = 7 + random.nextInt(14);
            LocalDate endDate = startDate.plusDays(durationDays);

            double planQtyVal = 100.0 + (random.nextDouble() * 900.0);
            BigDecimal plannedQuantity = BigDecimal.valueOf(planQtyVal).setScale(2, RoundingMode.HALF_UP);

            BigDecimal producedQuantity;
            if (status == ProductionPlanStatus.COMPLETED) {
                double completedRatio = 0.95 + (random.nextDouble() * 0.05);
                producedQuantity = plannedQuantity.multiply(BigDecimal.valueOf(completedRatio)).setScale(2, RoundingMode.HALF_UP);
            } else if (status == ProductionPlanStatus.IN_PROGRESS) {
                double inProgRatio = 0.20 + (random.nextDouble() * 0.50);
                producedQuantity = plannedQuantity.multiply(BigDecimal.valueOf(inProgRatio)).setScale(2, RoundingMode.HALF_UP);
            } else if (status == ProductionPlanStatus.HALTED) {
                double haltRatio = 0.10 + (random.nextDouble() * 0.25);
                producedQuantity = plannedQuantity.multiply(BigDecimal.valueOf(haltRatio)).setScale(2, RoundingMode.HALF_UP);
            } else {
                producedQuantity = BigDecimal.ZERO;
            }

            double utilVal = 65.0 + (random.nextDouble() * 45.0);
            double capacityUtilization = Math.round(utilVal * 10.0) / 10.0;

            ProductionPlan plan = new ProductionPlan(planCode, sku, location, startDate, endDate, plannedQuantity, capacityUtilization);
            plan.setProducedQuantity(producedQuantity);
            plan.setStatus(status);
            plan.setNotes("Synthetic production scheduling cycle");

            plans.add(plan);
        }

        return plans;
    }

    // =========================================================================
    // Dependent Generator Helpers
    // =========================================================================

    private void validateDependentRequest(DependentDataGenerationRequest request) {
        if (request == null) {
            throw new BusinessValidationException("Generation request payload cannot be null");
        }
        if (request.getInventoryCount() == null || request.getInventoryCount() < 0 ||
                request.getInventoryCount() > DependentDataGenerationRequest.MAX_INVENTORY_COUNT) {
            throw new BusinessValidationException(String.format("Inventory count must be between 0 and %d",
                    DependentDataGenerationRequest.MAX_INVENTORY_COUNT));
        }
        if (request.getForecastDemandCount() == null || request.getForecastDemandCount() < 0 ||
                request.getForecastDemandCount() > DependentDataGenerationRequest.MAX_FORECAST_DEMAND_COUNT) {
            throw new BusinessValidationException(String.format("Forecast demand count must be between 0 and %d",
                    DependentDataGenerationRequest.MAX_FORECAST_DEMAND_COUNT));
        }
        if (request.getSupplierCapacityCount() == null || request.getSupplierCapacityCount() < 0 ||
                request.getSupplierCapacityCount() > DependentDataGenerationRequest.MAX_SUPPLIER_CAPACITY_COUNT) {
            throw new BusinessValidationException(String.format("Supplier capacity count must be between 0 and %d",
                    DependentDataGenerationRequest.MAX_SUPPLIER_CAPACITY_COUNT));
        }
        if (request.getLeadTimeCount() == null || request.getLeadTimeCount() < 0 ||
                request.getLeadTimeCount() > DependentDataGenerationRequest.MAX_LEAD_TIME_COUNT) {
            throw new BusinessValidationException(String.format("Lead time count must be between 0 and %d",
                    DependentDataGenerationRequest.MAX_LEAD_TIME_COUNT));
        }
        if (request.getSeed() == null) {
            request.setSeed(DependentDataGenerationRequest.DEFAULT_SEED);
        }
    }

    private void validateMasterDataAvailability(List<Location> locations, List<Supplier> suppliers, List<SKU> skus) {
        if (locations == null || locations.isEmpty() ||
                suppliers == null || suppliers.isEmpty() ||
                skus == null || skus.isEmpty()) {
            throw new BusinessValidationException(
                    "Parent master data is missing. Please generate Locations, Suppliers, and SKUs first before generating dependent records."
            );
        }
    }

    private record SkuLocationPair(SKU sku, Location location) {}

    private List<SkuLocationPair> buildAllSkuLocationPairs(List<SKU> skus, List<Location> locations) {
        List<SkuLocationPair> pairs = new ArrayList<>(skus.size() * locations.size());
        for (SKU sku : skus) {
            for (Location loc : locations) {
                pairs.add(new SkuLocationPair(sku, loc));
            }
        }
        return pairs;
    }

    private List<Inventory> generateInventories(int count, Random random, boolean isPreview,
                                                List<Location> locations, List<SKU> skus) {
        if (count == 0) return new ArrayList<>();

        List<SkuLocationPair> allPairs = buildAllSkuLocationPairs(skus, locations);
        List<SkuLocationPair> candidatePairs;

        if (isPreview) {
            if (count > allPairs.size()) {
                throw new BusinessValidationException(String.format(
                        "Requested inventory count (%d) exceeds total available unique SKU-Location combinations (%d) from current master data.",
                        count, allPairs.size()));
            }
            candidatePairs = allPairs.subList(0, count);
        } else {
            List<SkuLocationPair> availablePairs = allPairs.stream()
                    .filter(p -> p.sku().getId() == null || p.location().getId() == null ||
                            !inventoryRepository.existsBySkuIdAndLocationId(p.sku().getId(), p.location().getId()))
                    .toList();

            if (count > availablePairs.size()) {
                throw new BusinessValidationException(String.format(
                        "Requested inventory count (%d) exceeds available unused SKU-Location combinations (%d available out of %d total).",
                        count, availablePairs.size(), allPairs.size()));
            }
            candidatePairs = availablePairs.subList(0, count);
        }

        List<Inventory> inventories = new ArrayList<>(count);
        for (SkuLocationPair pair : candidatePairs) {
            double onHandVal = 50.0 + (random.nextDouble() * 950.0);
            BigDecimal quantityOnHand = BigDecimal.valueOf(onHandVal).setScale(2, RoundingMode.HALF_UP);

            double reservedRatio = random.nextDouble() * 0.40;
            BigDecimal quantityReserved = quantityOnHand.multiply(BigDecimal.valueOf(reservedRatio)).setScale(2, RoundingMode.HALF_UP);

            BigDecimal safetyStock = BigDecimal.valueOf(pair.sku().getSafetyStock() != null ? pair.sku().getSafetyStock() : 30)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal reorderPoint = BigDecimal.valueOf(pair.sku().getReorderPoint() != null ? pair.sku().getReorderPoint() : 80)
                    .setScale(2, RoundingMode.HALF_UP);

            LocalDate auditDate = LocalDate.now().minusDays(1 + random.nextInt(45));

            Inventory inv = new Inventory(pair.sku(), pair.location(), quantityOnHand, quantityReserved, safetyStock, reorderPoint);
            inv.setLastAuditDate(auditDate);
            inv.calculateAvailableAndStatus();

            inventories.add(inv);
        }

        return inventories;
    }

    private List<ForecastDemand> generateForecastDemands(int count, Random random, boolean isPreview,
                                                        List<Location> locations, List<SKU> skus) {
        if (count == 0) return new ArrayList<>();

        List<SkuLocationPair> allPairs = buildAllSkuLocationPairs(skus, locations);
        List<ForecastDemand> demands = new ArrayList<>(count);
        Set<String> batchKeys = new HashSet<>();

        LocalDate baseMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        DemandPattern[] patterns = DemandPattern.values();

        for (int i = 0; i < count; i++) {
            SkuLocationPair pair = allPairs.get(i % allPairs.size());
            int weekOffset = i / allPairs.size();

            LocalDate forecastDate = baseMonday.plusWeeks(weekOffset);
            String key = (pair.sku().getId() != null ? pair.sku().getId() : pair.sku().getSkuCode()) + ":"
                    + (pair.location().getId() != null ? pair.location().getId() : pair.location().getLocationCode()) + ":"
                    + forecastDate;

            if (!isPreview && pair.sku().getId() != null && pair.location().getId() != null) {
                while (batchKeys.contains(key) ||
                        forecastDemandRepository.existsBySkuIdAndLocationIdAndForecastDate(
                                pair.sku().getId(), pair.location().getId(), forecastDate)) {
                    weekOffset++;
                    forecastDate = baseMonday.plusWeeks(weekOffset);
                    key = pair.sku().getId() + ":" + pair.location().getId() + ":" + forecastDate;
                }
            } else {
                while (batchKeys.contains(key)) {
                    weekOffset++;
                    forecastDate = baseMonday.plusWeeks(weekOffset);
                    key = (pair.sku().getId() != null ? pair.sku().getId() : pair.sku().getSkuCode()) + ":"
                            + (pair.location().getId() != null ? pair.location().getId() : pair.location().getLocationCode()) + ":"
                            + forecastDate;
                }
            }
            batchKeys.add(key);

            double qtyVal = 25.0 + (random.nextDouble() * 475.0);
            BigDecimal forecastQty = BigDecimal.valueOf(qtyVal).setScale(2, RoundingMode.HALF_UP);

            double confVal = 0.80 + (random.nextDouble() * 0.19);
            double confidence = Math.round(confVal * 100.0) / 100.0;

            DemandPattern pattern = patterns[random.nextInt(patterns.length)];

            ForecastDemand fd = new ForecastDemand(pair.sku(), pair.location(), forecastDate,
                    DemandPeriodType.WEEKLY, forecastQty, confidence, pattern);

            demands.add(fd);
        }

        return demands;
    }

    private record SupSkuPair(Supplier supplier, SKU sku) {}

    private List<SupSkuPair> buildAllSupSkuPairs(List<Supplier> suppliers, List<SKU> skus) {
        List<SupSkuPair> pairs = new ArrayList<>(suppliers.size() * skus.size());
        for (Supplier sup : suppliers) {
            for (SKU sku : skus) {
                pairs.add(new SupSkuPair(sup, sku));
            }
        }
        return pairs;
    }

    private List<SupplierCapacity> generateSupplierCapacities(int count, Random random, boolean isPreview,
                                                              List<Supplier> suppliers, List<SKU> skus) {
        if (count == 0) return new ArrayList<>();

        List<SupSkuPair> allPairs = buildAllSupSkuPairs(suppliers, skus);
        List<SupplierCapacity> capacities = new ArrayList<>(count);
        Set<String> batchKeys = new HashSet<>();

        LocalDate baseDate = LocalDate.now().withDayOfMonth(1);

        for (int i = 0; i < count; i++) {
            SupSkuPair pair = allPairs.get(i % allPairs.size());
            int horizonIndex = i / allPairs.size();

            LocalDate startDate = baseDate.plusMonths(horizonIndex * 3L);
            LocalDate endDate = startDate.plusMonths(3L);
            String key = (pair.supplier().getId() != null ? pair.supplier().getId() : pair.supplier().getSupplierCode()) + ":"
                    + (pair.sku().getId() != null ? pair.sku().getId() : pair.sku().getSkuCode()) + ":"
                    + startDate;

            if (!isPreview && pair.supplier().getId() != null && pair.sku().getId() != null) {
                while (batchKeys.contains(key) ||
                        supplierCapacityRepository.existsBySupplierIdAndSkuIdAndEffectiveStartDate(
                                pair.supplier().getId(), pair.sku().getId(), startDate)) {
                    horizonIndex++;
                    startDate = baseDate.plusMonths(horizonIndex * 3L);
                    endDate = startDate.plusMonths(3L);
                    key = pair.supplier().getId() + ":" + pair.sku().getId() + ":" + startDate;
                }
            } else {
                while (batchKeys.contains(key)) {
                    horizonIndex++;
                    startDate = baseDate.plusMonths(horizonIndex * 3L);
                    endDate = startDate.plusMonths(3L);
                    key = (pair.supplier().getId() != null ? pair.supplier().getId() : pair.supplier().getSupplierCode()) + ":"
                            + (pair.sku().getId() != null ? pair.sku().getId() : pair.sku().getSkuCode()) + ":"
                            + startDate;
                }
            }
            batchKeys.add(key);

            double dailyVal = 30.0 + (random.nextDouble() * 170.0);
            BigDecimal dailyCapacity = BigDecimal.valueOf(dailyVal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal monthlyCapacity = dailyCapacity.multiply(BigDecimal.valueOf(25)).setScale(2, RoundingMode.HALF_UP);

            double allocRatio = 0.10 + (random.nextDouble() * 0.40);
            BigDecimal allocatedCapacity = monthlyCapacity.multiply(BigDecimal.valueOf(allocRatio)).setScale(2, RoundingMode.HALF_UP);

            SupplierCapacity sc = new SupplierCapacity(pair.supplier(), pair.sku(), startDate, endDate, dailyCapacity, monthlyCapacity);
            sc.setAllocatedCapacity(allocatedCapacity);
            sc.updateAvailableCapacity();

            capacities.add(sc);
        }

        return capacities;
    }

    private record SupSkuLocTriplet(Supplier supplier, SKU sku, Location location) {}

    private List<SupSkuLocTriplet> buildAllTriplets(List<Supplier> suppliers, List<SKU> skus, List<Location> locations) {
        List<SupSkuLocTriplet> triplets = new ArrayList<>(suppliers.size() * skus.size() * locations.size());
        for (Supplier sup : suppliers) {
            for (SKU sku : skus) {
                for (Location loc : locations) {
                    triplets.add(new SupSkuLocTriplet(sup, sku, loc));
                }
            }
        }
        return triplets;
    }

    private List<LeadTime> generateLeadTimes(int count, Random random, boolean isPreview,
                                            List<Supplier> suppliers, List<SKU> skus, List<Location> locations) {
        if (count == 0) return new ArrayList<>();

        List<SupSkuLocTriplet> allTriplets = buildAllTriplets(suppliers, skus, locations);
        List<SupSkuLocTriplet> candidateTriplets;

        if (isPreview) {
            if (count > allTriplets.size()) {
                throw new BusinessValidationException(String.format(
                        "Requested lead time count (%d) exceeds total available unique combinations (%d) from current master data.",
                        count, allTriplets.size()));
            }
            candidateTriplets = allTriplets.subList(0, count);
        } else {
            List<SupSkuLocTriplet> availableTriplets = allTriplets.stream()
                    .filter(t -> t.supplier().getId() == null || t.sku().getId() == null || t.location().getId() == null ||
                            !leadTimeRepository.existsBySupplierIdAndSkuIdAndDestinationLocationId(
                                    t.supplier().getId(), t.sku().getId(), t.location().getId()))
                    .toList();

            if (count > availableTriplets.size()) {
                throw new BusinessValidationException(String.format(
                        "Requested lead time count (%d) exceeds available unused combinations (%d available out of %d total).",
                        count, availableTriplets.size(), allTriplets.size()));
            }
            candidateTriplets = availableTriplets.subList(0, count);
        }

        TransportMode[] modes = TransportMode.values();
        List<LeadTime> leadTimes = new ArrayList<>(count);

        for (SupSkuLocTriplet triplet : candidateTriplets) {
            int standardDays = 5 + random.nextInt(35);
            int expeditedDays = Math.max(1, (int) Math.round(standardDays * (0.35 + random.nextDouble() * 0.35)));
            if (expeditedDays >= standardDays) {
                expeditedDays = Math.max(1, standardDays - 2);
            }

            int moq = 10 + random.nextInt(90);
            double expCostVal = 5.00 + (random.nextDouble() * 45.00);
            BigDecimal expeditedCost = BigDecimal.valueOf(expCostVal).setScale(2, RoundingMode.HALF_UP);

            TransportMode transportMode = modes[random.nextInt(modes.length)];

            LeadTime lt = new LeadTime(triplet.supplier(), triplet.sku(), triplet.location(),
                    standardDays, expeditedDays, moq, expeditedCost, transportMode);

            leadTimes.add(lt);
        }

        return leadTimes;
    }

    // =========================================================================
    // Master Data Generator Helpers
    // =========================================================================

    private void validateMasterRequest(MasterDataGenerationRequest request) {
        if (request == null) {
            throw new BusinessValidationException("Generation request payload cannot be null");
        }
        if (request.getLocationCount() == null || request.getLocationCount() < 0 ||
                request.getLocationCount() > MasterDataGenerationRequest.MAX_LOCATION_COUNT) {
            throw new BusinessValidationException(String.format("Location count must be between 0 and %d",
                    MasterDataGenerationRequest.MAX_LOCATION_COUNT));
        }
        if (request.getSupplierCount() == null || request.getSupplierCount() < 0 ||
                request.getSupplierCount() > MasterDataGenerationRequest.MAX_SUPPLIER_COUNT) {
            throw new BusinessValidationException(String.format("Supplier count must be between 0 and %d",
                    MasterDataGenerationRequest.MAX_SUPPLIER_COUNT));
        }
        if (request.getSkuCount() == null || request.getSkuCount() < 0 ||
                request.getSkuCount() > MasterDataGenerationRequest.MAX_SKU_COUNT) {
            throw new BusinessValidationException(String.format("SKU count must be between 0 and %d",
                    MasterDataGenerationRequest.MAX_SKU_COUNT));
        }
        if (request.getSeed() == null) {
            request.setSeed(MasterDataGenerationRequest.DEFAULT_SEED);
        }
    }

    private List<Location> generateLocations(int count, Random random, boolean isPreview) {
        List<Location> locations = new ArrayList<>(count);
        Set<String> batchCodes = new HashSet<>();
        int seq = 1;

        LocationType[] types = LocationType.values();

        for (int i = 0; i < count; i++) {
            String code;
            if (isPreview) {
                code = String.format("%s%03d", LOCATION_CODE_PREFIX, i + 1);
            } else {
                code = resolveNextAvailableCode(LOCATION_CODE_PREFIX, seq, batchCodes, locationRepository::existsByLocationCode);
                seq++;
                batchCodes.add(code);
            }

            int templateIdx = i % LOCATION_TEMPLATES.length;
            String[] tpl = LOCATION_TEMPLATES[templateIdx];
            LocationType type = types[i % types.length];

            String name = (i >= LOCATION_TEMPLATES.length)
                    ? String.format("%s %s %d", tpl[1], tpl[0], (i / LOCATION_TEMPLATES.length) + 1)
                    : String.format("%s %s", tpl[1], tpl[0]);

            double capVal = 15000.0 + (random.nextDouble() * 235000.0);
            BigDecimal capacity = BigDecimal.valueOf(capVal).setScale(2, RoundingMode.HALF_UP);

            Location loc = new Location();
            loc.setLocationCode(code);
            loc.setName(name);
            loc.setType(type);
            loc.setCity(tpl[1]);
            loc.setState(tpl[2]);
            loc.setCountry(tpl[3]);
            loc.setPostalCode(tpl[4]);
            loc.setAddress(tpl[5]);
            loc.setCapacity(capacity);
            loc.setActive(true);

            locations.add(loc);
        }

        return locations;
    }

    private List<Supplier> generateSuppliers(int count, Random random, boolean isPreview) {
        List<Supplier> suppliers = new ArrayList<>(count);
        Set<String> batchCodes = new HashSet<>();
        int seq = 1;

        for (int i = 0; i < count; i++) {
            String code;
            if (isPreview) {
                code = String.format("%s%03d", SUPPLIER_CODE_PREFIX, i + 1);
            } else {
                code = resolveNextAvailableCode(SUPPLIER_CODE_PREFIX, seq, batchCodes, supplierRepository::existsBySupplierCode);
                seq++;
                batchCodes.add(code);
            }

            int templateIdx = i % SUPPLIER_TEMPLATES.length;
            String[] tpl = SUPPLIER_TEMPLATES[templateIdx];

            String companyName = (i >= SUPPLIER_TEMPLATES.length)
                    ? String.format("%s Division %d", tpl[0], (i / SUPPLIER_TEMPLATES.length) + 1)
                    : tpl[0];

            String contact = tpl[1];
            String country = tpl[2];

            double ratingVal = 3.5 + (random.nextDouble() * 1.5);
            double rating = Math.round(ratingVal * 10.0) / 10.0;

            double relVal = 75.0 + (random.nextDouble() * 24.5);
            double reliability = Math.round(relVal * 10.0) / 10.0;

            String emailPrefix = code.toLowerCase().replace("-", ".");
            String email = String.format("contact.%s@example.com", emailPrefix);
            String phone = String.format("+1-555-%04d", 1000 + random.nextInt(9000));

            Supplier sup = new Supplier();
            sup.setSupplierCode(code);
            sup.setName(companyName);
            sup.setContactPerson(contact);
            sup.setEmail(email);
            sup.setPhone(phone);
            sup.setCountry(country);
            sup.setRating(rating);
            sup.setReliabilityScore(reliability);
            sup.setActive(true);

            suppliers.add(sup);
        }

        return suppliers;
    }

    private List<SKU> generateSkus(int count, Random random, boolean isPreview) {
        List<SKU> skus = new ArrayList<>(count);
        Set<String> batchCodes = new HashSet<>();
        int seq = 1;

        for (int i = 0; i < count; i++) {
            String code;
            if (isPreview) {
                code = String.format("%s%03d", SKU_CODE_PREFIX, i + 1);
            } else {
                code = resolveNextAvailableCode(SKU_CODE_PREFIX, seq, batchCodes, skuRepository::existsBySkuCode);
                seq++;
                batchCodes.add(code);
            }

            int templateIdx = i % SKU_TEMPLATES.length;
            SkuTemplateDef tpl = SKU_TEMPLATES[templateIdx];

            String name = (i >= SKU_TEMPLATES.length)
                    ? String.format("%s Spec %d", tpl.name(), (i / SKU_TEMPLATES.length) + 1)
                    : tpl.name();

            double costMultiplier = 0.85 + (random.nextDouble() * 0.30);
            double costVal = Math.max(1.00, tpl.baseCost() * costMultiplier);
            BigDecimal unitCost = BigDecimal.valueOf(costVal).setScale(2, RoundingMode.HALF_UP);

            double markup = 0.25 + (random.nextDouble() * 0.50);
            BigDecimal markupAmount = unitCost.multiply(BigDecimal.valueOf(markup)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal sellingPrice = unitCost.add(markupAmount);
            if (sellingPrice.compareTo(unitCost) <= 0) {
                sellingPrice = unitCost.add(BigDecimal.valueOf(5.00));
            }

            int safetyStock = 20 + random.nextInt(180);
            int additionalBuffer = 15 + random.nextInt(100);
            int reorderPoint = safetyStock + additionalBuffer;

            int leadTimeDays = 3 + random.nextInt(43);

            SKU sku = new SKU();
            sku.setSkuCode(code);
            sku.setName(name);
            sku.setDescription(tpl.description());
            sku.setCategory(tpl.category());
            sku.setUnitOfMeasure(tpl.uom());
            sku.setUnitCost(unitCost);
            sku.setSellingPrice(sellingPrice);
            sku.setReorderPoint(reorderPoint);
            sku.setSafetyStock(safetyStock);
            sku.setLeadTimeDays(leadTimeDays);
            sku.setActive(true);

            skus.add(sku);
        }

        return skus;
    }

    private String resolveNextAvailableCode(String prefix, int digits, int startingSeq, Set<String> batchCodes, ExistsChecker existsChecker) {
        int candidateSeq = startingSeq;
        String formatStr = "%s%0" + digits + "d";
        while (true) {
            String candidateCode = String.format(formatStr, prefix, candidateSeq);
            if (!batchCodes.contains(candidateCode) && !existsChecker.exists(candidateCode)) {
                return candidateCode;
            }
            candidateSeq++;
        }
    }

    private String resolveNextAvailableCode(String prefix, int startingSeq, Set<String> batchCodes, ExistsChecker existsChecker) {
        return resolveNextAvailableCode(prefix, 3, startingSeq, batchCodes, existsChecker);
    }

    @FunctionalInterface
    private interface ExistsChecker {
        boolean exists(String code);
    }
}
