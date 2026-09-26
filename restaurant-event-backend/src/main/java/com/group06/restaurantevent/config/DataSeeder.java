package com.group06.restaurantevent.config;

import com.group06.restaurantevent.users.entity.Role;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.RoleRepository;
import com.group06.restaurantevent.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.demo-users:false}")
    private boolean seedDemoUsers;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        if (seedDemoUsers) {
            seedAdminUser();
        }
    }

    private void seedRoles() {
        List<String[]> roles = List.of(
                new String[]{"CUSTOMER",           "Restaurant customer"},
                new String[]{"ADMIN",              "System administrator"},
                new String[]{"MANAGER",            "Restaurant manager"},
                new String[]{"WAITER",             "Floor staff - waiter"},
                new String[]{"KITCHEN_STAFF",      "Kitchen preparation staff"},
                new String[]{"EVENT_COORDINATOR",  "Event bookings coordinator"},
                new String[]{"CASHIER",            "Billing and payments staff"},
                new String[]{"INVENTORY_MANAGER",  "Inventory and supplier management"}
        );

        for (String[] r : roles) {
            if (roleRepository.findByName(r[0]).isEmpty()) {
                Role role = new Role();
                role.setName(r[0]);
                role.setDescription(r[1]);
                roleRepository.save(role);
                log.info("Seeded role: {}", r[0]);
            }
        }
    }

    private void seedAdminUser() {
        seedUser("Admin User",        "admin@gather.com",     "0711000001", "Admin@1234",     "ADMIN");
        seedUser("Manager User",      "manager@gather.com",   "0711000002", "Manager@1234",   "MANAGER");
        seedUser("Waiter User",       "waiter@gather.com",    "0711000003", "Waiter@1234",    "WAITER");
        seedUser("Kitchen Staff",     "kitchen@gather.com",   "0711000004", "Kitchen@1234",   "KITCHEN_STAFF");
        seedUser("Events Coordinator","events@gather.com",    "0711000005", "Events@1234",    "EVENT_COORDINATOR");
        seedUser("Cashier User",      "cashier@gather.com",   "0711000006", "Cashier@1234",   "CASHIER");
        seedUser("Inventory Manager", "inventory@gather.com", "0711000007", "Inventory@1234", "INVENTORY_MANAGER");
        seedUser("Customer User",     "customer@gather.com",  "0711000008", "Customer@1234",  "CUSTOMER");
    }

    private void seedUser(String fullName, String email, String phone,
                          String password, String roleName) {
        if (userRepository.existsByEmail(email)) return;
        Role role = roleRepository.findByName(roleName).orElseThrow(
                () -> new IllegalStateException("Role not found: " + roleName));
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(password))
                .isActive(true)
                .roles(Set.of(role))
                .build();
        userRepository.save(user);
        log.info("Seeded user: {} / {}", email, roleName);
    }
}
