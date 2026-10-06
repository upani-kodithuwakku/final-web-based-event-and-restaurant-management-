package com.group06.restaurantevent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:stocktests;MODE=MySQL;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect","app.seed.demo-users=false"})
@AutoConfigureMockMvc
@Transactional
class InventoryCrudTests {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper mapper;
 void crud() throws Exception {
  String body="{\"name\":\"Basmati Rice\",\"unit\":\"kg\",\"currentQuantity\":25,\"reorderLevel\":5}";
  var response=mvc.perform(post("/api/inventory/items").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andExpect(jsonPath("$.isActive").value(true)).andReturn().getResponse().getContentAsString();
  long id=mapper.readTree(response).get("id").asLong();
  mvc.perform(get("/api/inventory/items/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Basmati Rice"));
  mvc.perform(put("/api/inventory/items/"+id).contentType(MediaType.APPLICATION_JSON).content(body.replace(":25",":30"))).andExpect(status().isOk()).andExpect(jsonPath("$.currentQuantity").value(30));
  mvc.perform(get("/api/inventory/items")).andExpect(status().isOk()).andExpect(jsonPath("$[0].isActive").value(true));
  mvc.perform(delete("/api/inventory/items/"+id)).andExpect(status().isNoContent());
  mvc.perform(get("/api/inventory/items")).andExpect(content().json("[]"));
 }
 @Test @WithMockUser(roles="ADMIN") void adminCrud() throws Exception {crud();}
 @Test @WithMockUser(roles="MANAGER") void managerCrud() throws Exception {crud();}
 @Test @WithMockUser(roles="CUSTOMER") void customerDenied() throws Exception {mvc.perform(get("/api/inventory/items")).andExpect(status().isForbidden());}
}
