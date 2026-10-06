package com.group06.restaurantevent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group06.restaurantevent.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.open-in-view=false",
    "app.seed.demo-users=false",
    "app.jwt.secret=test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm-testing",
    "app.jwt.expiration-ms=86400000"
})
class RestaurantEventApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;

    @Test
    @WithMockUser(roles = "ADMIN")
    void staffCanBeCreatedAndListedWithoutDemoAccounts() throws Exception {
        assertThat(users.count()).isZero();
        mvc.perform(get("/api/admin/staff"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        String request = """
                {"fullName":"Real Staff", "email":"real.staff@example.com",
                 "password":"Staff-test-123", "roles":["WAITER"],
                 "employmentStatus":"FULL_TIME"}
                """;
        String response = mvc.perform(post("/api/admin/staff/users")
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.active").doesNotExist())
                .andExpect(jsonPath("$.roles[0]").value("WAITER"))
                .andReturn().getResponse().getContentAsString();
        long staffId = mapper.readTree(response).get("id").asLong();

        mvc.perform(get("/api/admin/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(staffId))
                .andExpect(jsonPath("$[0].email").value("real.staff@example.com"))
                .andExpect(jsonPath("$[0].isActive").value(true));
        mvc.perform(get("/api/admin/staff/{id}", staffId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
        mvc.perform(post("/api/admin/staff/users")
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict());
        assertThat(users.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customersCannotManageStaff() throws Exception {
        mvc.perform(get("/api/admin/staff")).andExpect(status().isForbidden());
    }
}
