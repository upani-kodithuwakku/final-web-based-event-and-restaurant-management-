package com.group06.restaurantevent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group06.restaurantevent.menu.entity.*;
import com.group06.restaurantevent.menu.repository.*;
import com.group06.restaurantevent.orders.repository.FoodOrderRepository;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:menuorder;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.open-in-view=false", "app.seed.demo-users=false",
    "app.jwt.secret=test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm-testing"
})
class MenuOrderingTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired MenuCategoryRepository categories;
    @Autowired MenuItemRepository items;
    @Autowired FoodOrderRepository orders;
    @Autowired UserRepository users;
    private final Map<Long, User> savedUsers = new HashMap<>();
    private MenuItem dish;

    @BeforeEach void setup() {
        var category = categories.save(MenuCategory.builder().name("Test dishes").isActive(true).build());
        dish = items.save(MenuItem.builder().category(category).name("Test curry")
            .description("Fresh curry").price(new BigDecimal("1250.00")).isActive(true).isAvailable(true).build());
    }

    // Same principal shape as JwtAuthFilter: Spring Security UserDetails keyed by email.
    private RequestPostProcessor as(long key, String role) {
        User user = savedUsers.computeIfAbsent(key, k -> users.save(User.builder().fullName("Customer " + k)
            .email(k + "@example.test").passwordHash("not-used").isActive(true).build()));
        var principal = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
            .password("not-used").roles(role).build();
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
    private long idOf(long key) { return savedUsers.get(key).getId(); }
    private String order(int quantity) {
        return "{\"orderType\":\"TAKEAWAY\",\"specialNote\":\"No chilli\",\"subtotal\":1,\"items\":[{\"menuItemId\":" + dish.getId() + ",\"quantity\":" + quantity + ",\"price\":1}]}";
    }

    @Test void guestsCanBrowseButCannotOrderOrRequestFood() throws Exception {
        mvc.perform(get("/api/menu/items")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Test curry"))
            .andExpect(jsonPath("$[0].isAvailable").value(true))
            .andExpect(jsonPath("$[0].isActive").value(true));
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(order(1)))
            .andExpect(status().is4xxClientError());
        mvc.perform(post("/api/food-requests").contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"Help\"}"))
            .andExpect(status().is4xxClientError());
        assertThat(orders.count()).isZero();
    }

    @Test void ordersUseDatabasePricesAndBelongToTheSignedInCustomer() throws Exception {
        String response = mvc.perform(post("/api/orders").with(as(101, "CUSTOMER"))
            .contentType(MediaType.APPLICATION_JSON).content(order(2)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.subtotal").value(2500))
            .andExpect(jsonPath("$.customerId").value(idOf(101)))
            .andExpect(jsonPath("$.specialNote").value("No chilli"))
            .andExpect(jsonPath("$.items[0].quantity").value(2)).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(response).get("id").asLong();
        mvc.perform(get("/api/orders/my").with(as(101, "CUSTOMER"))).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/orders/my").with(as(202, "CUSTOMER"))).andExpect(content().json("[]"));
        mvc.perform(get("/api/orders/" + id).with(as(202, "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/kitchen/orders").with(as(303, "KITCHEN_STAFF")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].specialNote").value("No chilli"));
    }

    @Test void onlyCustomersCanPlaceOrders() throws Exception {
        for (String role : List.of("WAITER", "KITCHEN_STAFF", "CASHIER")) {
            mvc.perform(post("/api/orders").with(as(303, role))
                .contentType(MediaType.APPLICATION_JSON).content(order(1))).andExpect(status().isForbidden());
        }
        assertThat(orders.count()).isZero();
    }

    @Test void invalidQuantitiesAndUnavailableDishesCannotBeOrdered() throws Exception {
        for (int quantity : List.of(0, -1, 100)) {
            mvc.perform(post("/api/orders").with(as(101, "CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content(order(quantity))).andExpect(status().isBadRequest());
        }
        dish.setAvailable(false); items.saveAndFlush(dish);
        mvc.perform(get("/api/menu/items")).andExpect(jsonPath("$[0].isAvailable").value(false));
        mvc.perform(post("/api/orders").with(as(101, "CUSTOMER"))
            .contentType(MediaType.APPLICATION_JSON).content(order(1))).andExpect(status().isConflict());
        dish.setAvailable(true); dish.setActive(false); items.saveAndFlush(dish);
        mvc.perform(get("/api/menu/items")).andExpect(content().json("[]"));
        mvc.perform(post("/api/orders").with(as(101, "CUSTOMER"))
            .contentType(MediaType.APPLICATION_JSON).content(order(1))).andExpect(status().isConflict());
        assertThat(orders.count()).isZero();
    }

    @Test void staffCanResolveRequestsButCustomersCannotSeeOthersOrResolveThem() throws Exception {
        var response = mvc.perform(post("/api/food-requests").with(as(101, "CUSTOMER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"menuItemId\":" + dish.getId() + ",\"message\":\"Can this be dairy free? Table 5.\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.itemName").value("Test curry")).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(response).get("id").asLong();
        mvc.perform(get("/api/food-requests/my").with(as(202, "CUSTOMER"))).andExpect(content().json("[]"));
        mvc.perform(get("/api/food-requests").with(as(101, "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(patch("/api/food-requests/" + id + "/resolve").with(as(101, "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/food-requests").with(as(303, "WAITER"))).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(patch("/api/food-requests/" + id + "/resolve").with(as(303, "WAITER")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resolvedBy").value(idOf(303)));
        mvc.perform(get("/api/food-requests/my").with(as(101, "CUSTOMER")))
            .andExpect(jsonPath("$[0].status").value("RESOLVED"));
        mvc.perform(get("/api/food-requests").with(as(303, "WAITER"))).andExpect(content().json("[]"));
    }

    @Test void blankAndOversizedFoodRequestsAreRejected() throws Exception {
        for (String message : List.of("   ", "x".repeat(501))) {
            mvc.perform(post("/api/food-requests").with(as(101, "CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("message", message))))
                .andExpect(status().isBadRequest());
        }
    }

    @Test void managersCanCreateEditAndRemoveMenuItems() throws Exception {
        var body = new HashMap<String, Object>();
        body.put("categoryId", dish.getCategory().getId()); body.put("name", "New soup");
        body.put("price", 900); body.put("preparationMinutes", 15); body.put("available", true);
        String result = mvc.perform(post("/api/admin/menu/items").with(as(404, "MANAGER"))
            .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(result).get("id").asLong();
        body.put("name", "Updated soup"); body.put("price", 950);
        mvc.perform(put("/api/admin/menu/items/" + id).with(as(405, "ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.price").value(950));
        mvc.perform(delete("/api/admin/menu/items/" + id).with(as(404, "MANAGER"))).andExpect(status().isNoContent());
        assertThat(items.findById(id).orElseThrow().isActive()).isFalse();
    }

    @Test void kitchenAndWaitersCanUpdateAvailabilityButCannotChangeMenuPricesOrDelete() throws Exception {
        for (String role : List.of("WAITER", "KITCHEN_STAFF")) {
            mvc.perform(get("/api/admin/menu/items").with(as(303, role))).andExpect(status().isOk());
            mvc.perform(patch("/api/admin/menu/items/" + dish.getId() + "/availability").with(as(303, role))
                .contentType(MediaType.APPLICATION_JSON).content("{\"available\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.isAvailable").value(false));
            mvc.perform(delete("/api/admin/menu/items/" + dish.getId()).with(as(303, role))).andExpect(status().isForbidden());
            String body = mapper.writeValueAsString(Map.of("categoryId", dish.getCategory().getId(), "name", "Changed", "price", 10));
            mvc.perform(put("/api/admin/menu/items/" + dish.getId()).with(as(303, role))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
            mvc.perform(post("/api/admin/menu/items").with(as(303, role))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        }
        mvc.perform(patch("/api/admin/menu/items/" + dish.getId() + "/availability").with(as(101, "CUSTOMER"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"available\":true}")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/admin/menu/items/" + dish.getId() + "/availability").with(as(303, "KITCHEN_STAFF"))
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        assertThat(dish.getPrice()).isEqualByComparingTo("1250.00");
    }

    private String supplierBody(String phone, String date) throws Exception {
        return mapper.writeValueAsString(Map.of("name", "Fresh Supply", "contactPerson", "Sam", "phone", phone,
            "email", "sam@example.test", "address", "Colombo", "suppliedProducts", "Vegetables", "joinedDate", date, "active", true));
    }
    @Test void supplierCrudAndRolePermissions() throws Exception {
        String body = supplierBody("0771234567", "2020-01-01");
        String result = mvc.perform(post("/api/suppliers").with(as(404, "MANAGER"))
            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated())
            .andExpect(jsonPath("$.suppliedProducts").value("Vegetables")).andExpect(jsonPath("$.active").value(true))
            .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(result).get("id").asLong();
        for (String role : List.of("WAITER", "KITCHEN_STAFF", "INVENTORY_MANAGER")) {
            mvc.perform(get("/api/suppliers").with(as(303, role))).andExpect(status().isOk());
            mvc.perform(get("/api/suppliers/" + id).with(as(303, role))).andExpect(status().isOk());
            mvc.perform(post("/api/suppliers").with(as(303, role)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
            mvc.perform(put("/api/suppliers/" + id).with(as(303, role)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
            mvc.perform(delete("/api/suppliers/" + id).with(as(303, role))).andExpect(status().isForbidden());
        }
        mvc.perform(put("/api/suppliers/" + id).with(as(405, "ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content(body.replace("Vegetables", "Fruit")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.suppliedProducts").value("Fruit"));
        mvc.perform(get("/api/suppliers").with(as(101, "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/suppliers/" + id).with(as(404, "MANAGER"))).andExpect(status().isNoContent());
        mvc.perform(get("/api/suppliers/" + id).with(as(405, "ADMIN"))).andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
    }
    @Test void supplierInvalidPhoneEmailDateAndBlankProductsRejected() throws Exception {
        for (String body : List.of(supplierBody("123", "2020-01-01"), supplierBody("0771234567", "2999-01-01"),
            supplierBody("0771234567", "2020-01-01").replace("sam@example.test", "bad-email"),
            supplierBody("0771234567", "2020-01-01").replace("Vegetables", "   "))) {
            mvc.perform(post("/api/suppliers").with(as(405, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
    }
}
