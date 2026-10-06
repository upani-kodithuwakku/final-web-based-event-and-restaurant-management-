package com.group06.restaurantevent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group06.restaurantevent.auth.entity.PasswordResetToken;
import com.group06.restaurantevent.auth.repository.PasswordResetTokenRepository;
import com.group06.restaurantevent.common.audit.AuditLogRepository;
import com.group06.restaurantevent.common.enums.OrderStatus;
import com.group06.restaurantevent.common.enums.OrderType;
import com.group06.restaurantevent.notifications.entity.Notification;
import com.group06.restaurantevent.notifications.repository.NotificationRepository;
import com.group06.restaurantevent.orders.entity.FoodOrder;
import com.group06.restaurantevent.orders.entity.FoodOrderItem;
import com.group06.restaurantevent.orders.repository.FoodOrderRepository;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.RoleRepository;
import com.group06.restaurantevent.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:customermgmt;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.open-in-view=false", "app.seed.demo-users=false",
    "app.jwt.secret=test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm-testing",
    "app.auth.expose-reset-link=true"
})
class CustomerManagementTests {
    private static final String PASSWORD = "Original@123";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder encoder;
    @Autowired PasswordResetTokenRepository resetTokens;
    @Autowired NotificationRepository notifications;
    @Autowired AuditLogRepository auditLogs;
    @Autowired FoodOrderRepository orders;
    private final Map<String, User> saved = new HashMap<>();

    /** A real user in the database with the given role (roles are seeded on startup). */
    private User user(String name, String role) {
        return saved.computeIfAbsent(name, n -> users.save(User.builder().fullName("User " + n).email(n + "@cm.test")
            .passwordHash(encoder.encode(PASSWORD)).isActive(true)
            .roles(new HashSet<>(Set.of(roles.findByName(role).orElseThrow()))).build()));
    }

    // Same principal shape as JwtAuthFilter: Spring Security UserDetails keyed by email.
    private RequestPostProcessor as(String name, String role) {
        User u = user(name, role);
        var principal = org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
            .password("not-used").roles(role).build();
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private String json(Object body) throws Exception { return mapper.writeValueAsString(body); }

    // ── Profile ───────────────────────────────────────────────

    @Test void profileIsReturnedInsideTheApiResponseWrapperAndCanBeUpdated() throws Exception {
        mvc.perform(get("/api/users/me").with(as("ana", "CUSTOMER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("ana@cm.test"))
            .andExpect(jsonPath("$.data.roles[0]").value("CUSTOMER"));
        mvc.perform(put("/api/users/me").with(as("ana", "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("fullName", "  Ana Perera ", "phone", "0771234567"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.fullName").value("Ana Perera"))
            .andExpect(jsonPath("$.data.phone").value("0771234567"));
        mvc.perform(put("/api/users/me").with(as("ana", "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("fullName", "", "phone", "x")))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/users/me")).andExpect(status().is4xxClientError());
    }

    @Test void changePasswordNeedsTheCurrentPassword() throws Exception {
        mvc.perform(put("/api/users/me/password").with(as("ben", "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("currentPassword", "wrong-password", "newPassword", "Brand@New123"))))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Current password is incorrect"));
        mvc.perform(put("/api/users/me/password").with(as("ben", "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("currentPassword", PASSWORD, "newPassword", "short")))).andExpect(status().isBadRequest());
        mvc.perform(put("/api/users/me/password").with(as("ben", "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("currentPassword", PASSWORD, "newPassword", "Brand@New123")))).andExpect(status().isOk());
        assertThat(encoder.matches("Brand@New123", users.findByEmail("ben@cm.test").orElseThrow().getPasswordHash())).isTrue();
    }

    // ── Forgot / reset password ───────────────────────────────

    @Test void forgotPasswordGivesTheSameAnswerForUnknownEmails() throws Exception {
        mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", "nobody@cm.test"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(containsString("If an account exists")))
            .andExpect(jsonPath("$.data.resetLink").doesNotExist());
        assertThat(resetTokens.count()).isZero();
    }

    @Test void resetLinkSetsANewPasswordOnlyOnce() throws Exception {
        user("cara", "CUSTOMER");
        String body = mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", "cara@cm.test"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.resetLink").value(containsString("/reset-password?token=")))
            .andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(body).at("/data/resetLink").asText().replaceAll(".*token=", "");

        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("token", token, "newPassword", "Reset@Pass123")))).andExpect(status().isOk());
        assertThat(encoder.matches("Reset@Pass123", users.findByEmail("cara@cm.test").orElseThrow().getPasswordHash())).isTrue();

        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("token", token, "newPassword", "Another@Pass1")))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("token", "not-a-real-token", "newPassword", "Another@Pass1")))).andExpect(status().isBadRequest());
    }

    @Test void expiredAndReplacedResetLinksDoNotWork() throws Exception {
        User dan = user("dan", "CUSTOMER");
        PasswordResetToken expired = resetTokens.save(PasswordResetToken.builder().user(dan).token("expired-token")
            .expiresAt(LocalDateTime.now().minusMinutes(1)).used(false).build());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("token", expired.getToken(), "newPassword", "Reset@Pass123")))).andExpect(status().isBadRequest());

        PasswordResetToken older = resetTokens.save(PasswordResetToken.builder().user(dan).token("older-token")
            .expiresAt(LocalDateTime.now().plusMinutes(30)).used(false).build());
        mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("email", "dan@cm.test")))).andExpect(status().isOk());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("token", older.getToken(), "newPassword", "Reset@Pass123")))).andExpect(status().isBadRequest());
    }

    // ── Admin users ───────────────────────────────────────────

    @Test void adminUserListKeepsItsJsonShapeAndIsStaffOnly() throws Exception {
        user("eve", "CUSTOMER");
        mvc.perform(get("/api/admin/users").with(as("boss", "ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.email == 'eve@cm.test')].isActive").value(true))
            .andExpect(jsonPath("$[?(@.email == 'eve@cm.test')].roles[0]").value("CUSTOMER"));
        mvc.perform(get("/api/admin/users").with(as("eve", "CUSTOMER"))).andExpect(status().isForbidden());
    }

    @Test void adminCanSuspendOthersButNotThemselvesAndActionsAreAudited() throws Exception {
        User eve = user("eve", "CUSTOMER");
        User boss = user("boss", "ADMIN");
        mvc.perform(patch("/api/admin/users/" + boss.getId() + "/suspend").with(as("boss", "ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}")).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/admin/users/" + eve.getId() + "/suspend").with(as("boss", "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.isActive").value(false));
        mvc.perform(post("/api/admin/users/" + eve.getId() + "/reset-password").with(as("boss", "ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"short\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/users/" + eve.getId() + "/reset-password").with(as("boss", "ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"Admin@Set123\"}")).andExpect(status().isNoContent());
        assertThat(auditLogs.findAll()).extracting("action").contains("USER_SUSPENDED", "USER_PASSWORD_RESET");
    }

    // ── Notifications ─────────────────────────────────────────

    @Test void customersOnlySeeAndChangeTheirOwnNotifications() throws Exception {
        User fay = user("fay", "CUSTOMER");
        User gus = user("gus", "CUSTOMER");
        Notification mine = notifications.save(Notification.builder().user(fay).title("Hi").message("For Fay").type("INFO").build());
        notifications.save(Notification.builder().user(fay).title("Hi again").message("For Fay").type("INFO").build());
        Notification theirs = notifications.save(Notification.builder().user(gus).title("Hi").message("For Gus").type("INFO").build());

        mvc.perform(get("/api/notifications").with(as("fay", "CUSTOMER")))
            .andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].isRead").value(false));
        mvc.perform(get("/api/notifications/unread-count").with(as("fay", "CUSTOMER"))).andExpect(jsonPath("$.count").value(2));
        mvc.perform(patch("/api/notifications/" + theirs.getId() + "/read").with(as("fay", "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(patch("/api/notifications/" + mine.getId() + "/read").with(as("fay", "CUSTOMER")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.isRead").value(true));
        mvc.perform(patch("/api/notifications/read-all").with(as("fay", "CUSTOMER"))).andExpect(status().isNoContent());
        mvc.perform(get("/api/notifications/unread-count").with(as("fay", "CUSTOMER"))).andExpect(jsonPath("$.count").value(0));
        mvc.perform(get("/api/notifications/unread-count").with(as("gus", "CUSTOMER"))).andExpect(jsonPath("$.count").value(1));
    }

    // ── Reports ───────────────────────────────────────────────

    @Test void reportsAreForAdminsAndManagersOnly() throws Exception {
        mvc.perform(get("/api/admin/reports/dashboard").with(as("hal", "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/reports/dashboard").with(as("mia", "MANAGER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
            .andExpect(jsonPath("$.totalTables").exists())
            .andExpect(jsonPath("$.lowStockItems").exists());
        mvc.perform(get("/api/admin/reports/inventory").with(as("mia", "MANAGER"))).andExpect(jsonPath("$.lowStockItems").isArray());
        mvc.perform(get("/api/admin/reports/events").with(as("mia", "MANAGER"))).andExpect(jsonPath("$.byStatus.CONFIRMED").value(0));
        mvc.perform(get("/api/admin/reports/reservations").with(as("mia", "MANAGER")))
            .andExpect(jsonPath("$.byStatus.NO_SHOW").value(0)).andExpect(jsonPath("$.noShowRate").value(0.0));
    }

    @Test void reportDateRangesAreValidated() throws Exception {
        mvc.perform(get("/api/admin/reports/reservations?from=2026-10-10&to=2026-10-01").with(as("boss", "ADMIN")))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/reports/sales?from=2024-01-01&to=2026-01-01").with(as("boss", "ADMIN")))
            .andExpect(status().isBadRequest());
    }

    @Test void salesReportCountsTodaysOrdersAndTopItems() throws Exception {
        User ivy = user("ivy", "CUSTOMER");
        saveOrder(ivy, OrderStatus.PREPARING, "Curry", 2, "1000.00");
        saveOrder(ivy, OrderStatus.COMPLETED, "Curry", 1, "1000.00");
        saveOrder(ivy, OrderStatus.READY, "Cake", 1, "500.00");
        saveOrder(ivy, OrderStatus.CANCELLED, "Cake", 4, "500.00");
        mvc.perform(get("/api/admin/reports/sales").with(as("boss", "ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderCount").value(3))
            .andExpect(jsonPath("$.cancelledOrders").value(1))
            .andExpect(jsonPath("$.foodRevenue").value(3500.00))
            .andExpect(jsonPath("$.averageOrderValue").value(1166.67))
            .andExpect(jsonPath("$.topItems[0].name").value("Curry"))
            .andExpect(jsonPath("$.topItems[0].quantity").value(3));
    }

    private void saveOrder(User customer, OrderStatus status, String item, int qty, String unitPrice) {
        BigDecimal line = new BigDecimal(unitPrice).multiply(BigDecimal.valueOf(qty));
        FoodOrder order = FoodOrder.builder().orderReference("ORD-CM-" + System.nanoTime() % 1000000)
            .customerId(customer.getId()).orderType(OrderType.DINE_IN).status(status).subtotal(line).build();
        order.getItems().add(FoodOrderItem.builder().order(order).menuItemId(1L).itemNameSnapshot(item)
            .unitPriceSnapshot(new BigDecimal(unitPrice)).quantity(qty).lineTotal(line).build());
        orders.save(order);
    }
}
