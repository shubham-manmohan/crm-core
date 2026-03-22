/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.master.entity;
import com.mini2more.crm.modules.master.repository.MasterDataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mini2more.crm.modules.core.entity.Branch;
import com.mini2more.crm.modules.core.repository.BranchRepository;
import com.mini2more.crm.modules.core.entity.UserEntity;
import com.mini2more.crm.modules.core.repository.UserRepository;
import com.mini2more.crm.common.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final MasterDataRepository masterDataRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final com.mini2more.crm.modules.core.repository.CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedBranches();
        seedAdminUser();
        seedStates();
        seedIndustries();
        seedApplications();
        seedUnits();
        seedPaymentTerms();
        seedLeadSources();
        seedProductCategories();
        seedBrands();
        log.info("✅ Master data seeding completed");
    }

    private void seedBranches() {
        if (branchRepository.count() == 0) {
            branchRepository.save(Branch.builder()
                    .name("Delhi Branch 1 - Head Office")
                    .address("Industrial Area, Delhi")
                    .city("New Delhi")
                    .state("Delhi")
                    .pincode("110001")
                    .phone("9999999901")
                    .email("delhi1@metaautomation.in")
                    .isActive(true)
                    .build());
            branchRepository.save(Branch.builder()
                    .name("Delhi Branch 2")
                    .address("Okhla Industrial Estate, Delhi")
                    .city("New Delhi")
                    .state("Delhi")
                    .pincode("110020")
                    .phone("9999999902")
                    .email("delhi2@metaautomation.in")
                    .isActive(true)
                    .build());
            log.info("Seeded 2 branches");
        }
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@metaautomation.in")) {
            com.mini2more.crm.modules.core.entity.Company adminCompany = com.mini2more.crm.modules.core.entity.Company.builder()
                .name("Meta Automation Solutions")
                .companyType(com.mini2more.crm.common.enums.CompanyType.BOTH)
                .build();
            companyRepository.save(adminCompany);

            UserEntity admin = UserEntity.builder()
                    .email("admin@metaautomation.in")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .fullName("System Administrator")
                    .company(adminCompany)
                    .phone("9999999900")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("Seeded admin user: admin@metaautomation.in / Admin@123");
        }
    }

    private void seedStates() {
        if (masterDataRepository.countByType("STATE") == 0) {
            String[][] states = {
                    { "DL", "Delhi" }, { "MH", "Maharashtra" }, { "KA", "Karnataka" },
                    { "TN", "Tamil Nadu" }, { "GJ", "Gujarat" }, { "RJ", "Rajasthan" },
                    { "UP", "Uttar Pradesh" }, { "MP", "Madhya Pradesh" }, { "WB", "West Bengal" },
                    { "AP", "Andhra Pradesh" }, { "TS", "Telangana" }, { "KL", "Kerala" },
                    { "PB", "Punjab" }, { "HR", "Haryana" }, { "BR", "Bihar" },
                    { "OR", "Odisha" }, { "CG", "Chhattisgarh" }, { "JH", "Jharkhand" },
                    { "UK", "Uttarakhand" }, { "GA", "Goa" }, { "HP", "Himachal Pradesh" },
                    { "JK", "Jammu & Kashmir" }, { "AS", "Assam" }, { "CH", "Chandigarh" }
            };
            int order = 1;
            for (String[] s : states) {
                masterDataRepository.save(MasterData.builder()
                        .type("STATE").code(s[0]).name(s[1])
                        .displayOrder(order++).isActive(true).build());
            }
            // Seed some cities for Delhi
            String[][] delhiCities = {
                    { "NEW_DELHI", "New Delhi" }, { "SOUTH_DELHI", "South Delhi" },
                    { "NORTH_DELHI", "North Delhi" }, { "EAST_DELHI", "East Delhi" },
                    { "WEST_DELHI", "West Delhi" }, { "OKHLA", "Okhla" },
                    { "DWARKA", "Dwarka" }, { "NOIDA", "Noida" }, { "GURGAON", "Gurgaon" },
                    { "FARIDABAD", "Faridabad" }, { "GHAZIABAD", "Ghaziabad" }
            };
            order = 1;
            for (String[] c : delhiCities) {
                masterDataRepository.save(MasterData.builder()
                        .type("CITY").code(c[0]).name(c[1]).parentCode("DL")
                        .displayOrder(order++).isActive(true).build());
            }
            // Mumbai cities
            String[][] mumbaiCities = {
                    { "MUMBAI_CENTRAL", "Mumbai Central" }, { "ANDHERI", "Andheri" },
                    { "THANE", "Thane" }, { "NAVI_MUMBAI", "Navi Mumbai" }, { "PUNE", "Pune" }
            };
            order = 1;
            for (String[] c : mumbaiCities) {
                masterDataRepository.save(MasterData.builder()
                        .type("CITY").code(c[0]).name(c[1]).parentCode("MH")
                        .displayOrder(order++).isActive(true).build());
            }
            log.info("Seeded states and cities");
        }
    }

    private void seedIndustries() {
        if (masterDataRepository.countByType("INDUSTRY") == 0) {
            String[][] industries = {
                    { "POWER", "Power & Energy" }, { "OIL_GAS", "Oil & Gas" },
                    { "WATER", "Water & Wastewater" }, { "METALS", "Metals & Mining" },
                    { "CEMENT", "Cement" }, { "FOOD", "Food & Beverage" },
                    { "PHARMA", "Pharmaceutical" }, { "AUTOMOTIVE", "Automotive" },
                    { "TEXTILE", "Textile" }, { "INFRA", "Infrastructure" },
                    { "SOLAR", "Solar & Renewable" }, { "CHEMICAL", "Chemical" },
                    { "BUILDING", "Building Automation" }, { "MARINE", "Marine" },
                    { "PAPER", "Paper & Pulp" }, { "SUGAR", "Sugar" }
            };
            int order = 1;
            for (String[] i : industries) {
                masterDataRepository.save(MasterData.builder()
                        .type("INDUSTRY").code(i[0]).name(i[1])
                        .displayOrder(order++).isActive(true).build());
            }
            log.info("Seeded industries");
        }
    }

    private void seedApplications() {
        if (masterDataRepository.countByType("APPLICATION") == 0) {
            String[][] apps = {
                    { "MOTOR_CONTROL", "Motor Control" }, { "SWITCHGEAR", "Switchgear Panel" },
                    { "PLC_SCADA", "PLC & SCADA" }, { "DRIVES_VFD", "Drives & VFD" },
                    { "POWER_DIST", "Power Distribution" }, { "METERING", "Metering & Monitoring" },
                    { "PROTECTION", "Protection Relays" }, { "ENCLOSURE", "Enclosures & Panels" },
                    { "CABLE_MGMT", "Cable Management" }, { "SENSOR", "Sensors & Instrumentation" },
                    { "SOFT_STARTER", "Soft Starters" }, { "UPS_POWER", "UPS & Power Supply" },
                    { "LIGHTING", "Industrial Lighting" }, { "SAFETY", "Safety Systems" }
            };
            int order = 1;
            for (String[] a : apps) {
                masterDataRepository.save(MasterData.builder()
                        .type("APPLICATION").code(a[0]).name(a[1])
                        .displayOrder(order++).isActive(true).build());
            }
            log.info("Seeded applications");
        }
    }

    private void seedUnits() {
        if (masterDataRepository.countByType("UNIT") == 0) {
            String[][] units = {
                    { "NOS", "Numbers" }, { "MTR", "Meters" }, { "KG", "Kilograms" },
                    { "SET", "Sets" }, { "BOX", "Box" }, { "ROLL", "Roll" },
                    { "LOT", "Lot" }, { "PCS", "Pieces" }, { "PKT", "Packet" },
                    { "LTR", "Litres" }
            };
            int order = 1;
            for (String[] u : units) {
                masterDataRepository.save(MasterData.builder()
                        .type("UNIT").code(u[0]).name(u[1])
                        .displayOrder(order++).isActive(true).build());
            }
            log.info("Seeded units");
        }
    }

    private void seedPaymentTerms() {
        if (masterDataRepository.countByType("PAYMENT_TERM") == 0) {
            String[][] terms = {
                    { "ADVANCE", "100% Advance" }, { "PDC", "PDC (Post Dated Cheque)" },
                    { "NET15", "Net 15 Days" }, { "NET30", "Net 30 Days" },
                    { "NET45", "Net 45 Days" }, { "NET60", "Net 60 Days" },
                    { "LC", "Letter of Credit" }, { "CASH", "Cash on Delivery" },
                    { "PARTIAL", "50% Advance, 50% on Delivery" }
            };
            int order = 1;
            for (String[] t : terms) {
                masterDataRepository.save(MasterData.builder()
                        .type("PAYMENT_TERM").code(t[0]).name(t[1])
                        .displayOrder(order++).isActive(true).build());
            }
            log.info("Seeded payment terms");
        }
    }

    private void seedLeadSources() {
        if (masterDataRepository.countByType("LEAD_SOURCE") == 0) {
            String[][] sources = {
                    { "WEBSITE", "Website" }, { "WHATSAPP", "WhatsApp" },
                    { "PHONE", "Phone Call" }, { "EMAIL", "Email" },
                    { "TRADE_FAIR", "Trade Fair/Exhibition" }, { "REFERRAL", "Referral" },
                    { "INDIAMART", "IndiaMART" }, { "JUSTDIAL", "JustDial" },
                    { "WALK_IN", "Walk-In" }, { "LINKEDIN", "LinkedIn" },
                    { "GOOGLE", "Google Search" }, { "REPEAT", "Repeat Customer" }
            };
            int order = 1;
            for (String[] s : sources) {
                masterDataRepository.save(MasterData.builder()
                        .type("LEAD_SOURCE").code(s[0]).name(s[1])
                        .displayOrder(order++).isActive(true).build());
            }
            log.info("Seeded lead sources");
        }
    }

    private void seedProductCategories() {
        if (masterDataRepository.countByType("PRODUCT_CATEGORY") == 0) {
            String[][] cats = {
                    { "MCB", "Miniature Circuit Breaker" }, { "MCCB", "Moulded Case Circuit Breaker" },
                    { "ACB", "Air Circuit Breaker" }, { "CONTACTOR", "Contactors & Relays" },
                    { "VFD", "Variable Frequency Drives" }, { "PLC", "Programmable Logic Controller" },
                    { "HMI", "Human Machine Interface" }, { "SERVO", "Servo Drives & Motors" },
                    { "SOFT_STARTER", "Soft Starters" }, { "SENSOR", "Sensors" },
                    { "SWITCH", "Switches & Push Buttons" }, { "CABLE", "Cables & Wires" },
                    { "PANEL", "Panel Accessories" }, { "METER", "Meters & Instruments" },
                    { "RELAY", "Protection Relays" }, { "TRANSFORMER", "Transformers" },
                    { "ENCLOSURE", "Enclosures" }, { "BUSBAR", "Busbar Systems" },
                    { "TIMER", "Timers & Counters" }, { "POWER_SUPPLY", "Power Supplies" }
            };
            int order = 1;
            for (String[] c : cats) {
                masterDataRepository.save(MasterData.builder()
                        .type("PRODUCT_CATEGORY").code(c[0]).name(c[1])
                        .displayOrder(order++).isActive(true).build());
            }
            log.info("Seeded product categories");
        }
    }

    private void seedBrands() {
        if (masterDataRepository.countByType("BRAND") == 0) {
            String[][] brands = {
                    { "SCHNEIDER", "Schneider Electric" }, { "SIEMENS", "Siemens" },
                    { "ABB", "ABB" }, { "LS", "LS Electric" }, { "DELTA", "Delta Electronics" },
                    { "HAVELLS", "Havells" }, { "LEGRAND", "Legrand" }, { "PHOENIX", "Phoenix Contact" },
                    { "OMRON", "Omron" }, { "MITSUBISHI", "Mitsubishi Electric" },
                    { "ROCKWELL", "Rockwell Automation" }, { "HONEYWELL", "Honeywell" },
                    { "EATON", "Eaton" }, { "GE", "GE Industrial" }, { "LANDT", "L&T" },
                    { "BHEL", "BHEL" }, { "CG", "CG Power" }, { "POLYCAB", "Polycab" },
                    { "FINOLEX", "Finolex" }, { "CROMPTON", "Crompton Greaves" }
            };
            int order = 1;
            for (String[] b : brands) {
                masterDataRepository.save(MasterData.builder()
                        .type("BRAND").code(b[0]).name(b[1])
                        .displayOrder(order++).isActive(true).build());
            }
            log.info("Seeded brands");
        }
    }
}
