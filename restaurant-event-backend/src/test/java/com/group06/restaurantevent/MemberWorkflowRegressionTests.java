package com.group06.restaurantevent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group06.restaurantevent.billing.repository.InvoiceRepository;
import com.group06.restaurantevent.common.enums.*;
import com.group06.restaurantevent.events.entity.*;
import com.group06.restaurantevent.events.repository.*;
import com.group06.restaurantevent.inventory.repository.*;
import com.group06.restaurantevent.menu.entity.*;
import com.group06.restaurantevent.menu.repository.*;
import com.group06.restaurantevent.notifications.entity.Notification;
import com.group06.restaurantevent.notifications.repository.NotificationRepository;
import com.group06.restaurantevent.orders.entity.FoodOrder;
import com.group06.restaurantevent.orders.repository.FoodOrderRepository;
import com.group06.restaurantevent.reservations.entity.*;
import com.group06.restaurantevent.reservations.repository.*;
import com.group06.restaurantevent.staff.entity.*;
import com.group06.restaurantevent.staff.repository.*;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:memberworkflows;MODE=MySQL;DB_CLOSE_DELAY=-1",
 "spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=",
 "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect","spring.jpa.open-in-view=false",
 "spring.jpa.show-sql=false","app.seed.demo-users=false","app.auth.mail-enabled=false"})
@AutoConfigureMockMvc @Transactional
class MemberWorkflowRegressionTests {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired UserRepository users; @Autowired RoleRepository roles;
 @Autowired FoodOrderRepository orders; @Autowired InvoiceRepository invoices;
 @Autowired StaffProfileRepository profiles; @Autowired ShiftRepository shifts; @Autowired ShiftAssignmentRepository assignments;
 @Autowired InventoryItemRepository inventory; @Autowired StockMovementRepository movements; @Autowired SupplierRepository suppliers;
 @Autowired NotificationRepository notifications; @Autowired AttendanceRepository attendance;
 @Autowired MenuCategoryRepository categories; @Autowired MenuItemRepository menu;
 @Autowired EventHallRepository halls; @Autowired EventPackageRepository packages; @Autowired EventBookingRepository events;
 @Autowired RestaurantTableRepository tables; @Autowired TableReservationRepository reservations;
 final ZoneId zone=ZoneId.of("Asia/Colombo");
 User user(String role) {return users.save(User.builder().fullName("Test "+role).email(UUID.randomUUID()+"@example.test")
   .passwordHash("not-used").isActive(true).roles(new HashSet<>(Set.of(roles.findByName(role).orElseThrow()))).build());}
 RequestPostProcessor as(User u,String role) {UserDetails p=org.springframework.security.core.userdetails.User.withUsername(u.getEmail()).password("not-used").roles(role).build();return authentication(new UsernamePasswordAuthenticationToken(p,null,p.getAuthorities()));}
 String body(Object value)throws Exception{return json.writeValueAsString(value);}
 long id(String response)throws Exception{return json.readTree(response).get("id").asLong();}
 FoodOrder order(User u,String amount){return orders.save(FoodOrder.builder().customerId(u.getId()).orderReference("O-"+UUID.randomUUID().toString().substring(0,20)).orderType(OrderType.TAKEAWAY).status(OrderStatus.PENDING).subtotal(new BigDecimal(amount)).build());}
 MenuItem dish(){var c=categories.save(MenuCategory.builder().name("Test menu").isActive(true).build());return menu.save(MenuItem.builder().category(c).name("Curry").price(new BigDecimal("100.00")).preparationMinutes(15).isActive(true).isAvailable(true).build());}
 Map<String,Object> orderBody(MenuItem d,int quantity){return Map.of("orderType","TAKEAWAY","items",List.of(Map.of("menuItemId",d.getId(),"quantity",quantity)));}
 StaffProfile profile(User u){return profiles.save(StaffProfile.builder().userId(u.getId()).employeeCode("EMP-"+UUID.randomUUID().toString().substring(0,8)).jobTitle("Waiter").employmentStatus(EmploymentStatus.FULL_TIME).isActive(true).build());}
 Shift shift(LocalDate date,int needed){return shifts.save(Shift.builder().shiftDate(date).startTime(LocalTime.of(10,0)).endTime(LocalTime.of(18,0)).roleRequired("WAITER").requiredStaffCount(needed).status(ShiftStatus.SCHEDULED).build());}
 ShiftAssignment assign(Shift s,StaffProfile p){return assignments.save(ShiftAssignment.builder().shift(s).staffId(p.getId()).assignedRole("WAITER").status(AssignmentStatus.ASSIGNED).build());}
 long stock(User admin,String name,int quantity)throws Exception{return id(mvc.perform(post("/api/inventory/items").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("name",name,"unit","kg","currentQuantity",quantity,"reorderLevel",5)))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());}
 long supplier(User admin)throws Exception{return id(mvc.perform(post("/api/suppliers").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("name","Supplier","contactPerson","Contact","phone","0771234567","email","supplier@example.test","address","Colombo","suppliedProducts","Rice","joinedDate",LocalDate.now(zone).minusDays(1).toString(),"active",true)))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());}

 @Test void billingUsesServerAmountsAndProtectsOwnerAndVoids()throws Exception{
  var admin=user("ADMIN");var owner=user("CUSTOMER");var stranger=user("CUSTOMER");var order=order(owner,"200.00");
  String invoice= mvc.perform(post("/api/billing/invoices").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("invoiceType","FOOD_ORDER","foodOrderId",order.getId(),"customerId",owner.getId())))).andExpect(status().isCreated()).andExpect(jsonPath("$.totalAmount").value(240.0)).andReturn().getResponse().getContentAsString();long inv=id(invoice);
  mvc.perform(get("/api/billing/invoices/"+inv).with(as(stranger,"CUSTOMER"))).andExpect(status().isForbidden());
  mvc.perform(get("/api/billing/payments/"+inv).with(as(stranger,"CUSTOMER"))).andExpect(status().isForbidden());
  mvc.perform(get("/api/billing/invoices/"+inv).with(as(owner,"CUSTOMER"))).andExpect(status().isOk());
  mvc.perform(get("/api/billing/invoices/my").with(as(owner,"CUSTOMER"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(inv));
  mvc.perform(post("/api/billing/invoices").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("invoiceType","FOOD_ORDER","foodOrderId",order.getId(),"customerId",owner.getId())))).andExpect(status().isConflict());
  mvc.perform(patch("/api/billing/invoices/"+inv+"/void").with(as(admin,"ADMIN"))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
  mvc.perform(post("/api/billing/payments").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("invoiceId",inv,"method","CASH")))).andExpect(status().isBadRequest());
 }
 @Test void billingRejectsMismatchedMissingAndZeroReferences()throws Exception{
  var admin=user("ADMIN");var owner=user("CUSTOMER");var other=user("CUSTOMER");var o=order(owner,"0.00");
  for(Map<String,Object> request:List.<Map<String,Object>>of(Map.of("invoiceType","FOOD_ORDER","foodOrderId",o.getId(),"customerId",owner.getId()),Map.of("invoiceType","FOOD_ORDER","foodOrderId",o.getId(),"customerId",other.getId()),Map.of("invoiceType","EVENT_BOOKING","foodOrderId",o.getId(),"customerId",owner.getId())))mvc.perform(post("/api/billing/invoices").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(request))).andExpect(status().isBadRequest());
  mvc.perform(post("/api/billing/invoices").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("invoiceType","FOOD_ORDER","foodOrderId",99999,"customerId",owner.getId())))).andExpect(status().isNotFound());assertThat(invoices.count()).isZero();
 }
 @Test void deletingStaffDisablesLoginAndListUsesTheProfileId()throws Exception{
  var admin=user("ADMIN");var worker=user("WAITER");
  String list=mvc.perform(get("/api/admin/staff").with(as(admin,"ADMIN"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  var p=profiles.findByUserId(worker.getId()).orElseThrow();
  assertThat(json.readTree(list)).anyMatch(node->node.get("userId").asLong()==worker.getId() && node.get("id").asLong()==p.getId());
  mvc.perform(delete("/api/admin/staff/"+p.getId()).with(as(admin,"ADMIN"))).andExpect(status().isNoContent());
  assertThat(users.findById(worker.getId()).orElseThrow().isActive()).isFalse();
  mvc.perform(get("/api/admin/staff").with(as(admin,"ADMIN"))).andExpect(status().isOk());
  assertThat(profiles.findById(p.getId()).orElseThrow().isActive()).isFalse();
  mvc.perform(patch("/api/admin/staff/"+p.getId()+"/status").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
 }
 @Test void staffRolesCannotUseLowercaseCustomerOrBlankEntries()throws Exception{
  var admin=user("ADMIN");for(String role:List.of("customer"," CUSTOMER ",""))mvc.perform(post("/api/admin/staff/users").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("fullName","Staff","email","new@example.test","password","Password123","roles",List.of(role))))).andExpect(status().isBadRequest());
  var worker=profile(user("WAITER"));mvc.perform(patch("/api/admin/staff/"+worker.getId()+"/roles").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content("{\"roles\":[null]}")).andExpect(status().isBadRequest());
 }
 @Test void shiftCannotBeOverfilledOrAssignedToInactiveStaff()throws Exception{
  var admin=user("ADMIN");var a=profile(user("WAITER"));var b=profile(user("WAITER"));var s=shift(LocalDate.now(zone).plusDays(2),1);
  mvc.perform(post("/api/admin/staff/shifts/"+s.getId()+"/assign").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("staffId",a.getId())))).andExpect(status().isCreated());
  mvc.perform(post("/api/admin/staff/shifts/"+s.getId()+"/assign").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("staffId",b.getId())))).andExpect(status().isConflict());assertThat(assignments.findByShift_Id(s.getId())).hasSize(1);
 }
 @Test void orderEditCancelAndReferencesStayOwnerScoped()throws Exception{
  var owner=user("CUSTOMER");var other=user("CUSTOMER");var dish=dish();
  long order=id(mvc.perform(post("/api/orders").with(as(owner,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(orderBody(dish,1)))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
  mvc.perform(put("/api/orders/"+order).with(as(other,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(orderBody(dish,2)))).andExpect(status().isForbidden());
  mvc.perform(put("/api/orders/"+order).with(as(owner,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(orderBody(dish,2)))).andExpect(status().isOk()).andExpect(jsonPath("$.subtotal").value(200));assertThat(orders.count()).isEqualTo(1);
  mvc.perform(patch("/api/orders/"+order+"/cancel").with(as(other,"CUSTOMER"))).andExpect(status().isForbidden());
  mvc.perform(patch("/api/orders/"+order+"/cancel").with(as(owner,"CUSTOMER"))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
  mvc.perform(put("/api/orders/"+order).with(as(owner,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(orderBody(dish,2)))).andExpect(status().isConflict());
  var invalid=new HashMap<>(orderBody(dish,1));invalid.put("tableId",999999);invalid.put("orderType","DINE_IN");mvc.perform(post("/api/orders").with(as(owner,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(invalid))).andExpect(status().isNotFound());
 }
 @Test void orderingCannotAttachAnotherCustomersReservation()throws Exception{
  var owner=user("CUSTOMER");var other=user("CUSTOMER");var dish=dish();var t=tables.save(RestaurantTable.builder().tableNumber("T99").capacity(6).isActive(true).currentStatus(TableStatus.AVAILABLE).build());
  var r=reservations.save(TableReservation.builder().bookingReference("R99").customer(other).table(t).reservationDate(LocalDate.now(zone).plusDays(2)).startTime(LocalTime.NOON).endTime(LocalTime.of(14,0)).guestCount(2).contactName("Other").contactPhone("0771234567").status(ReservationStatus.CONFIRMED).build());
  var req=new HashMap<>(orderBody(dish,1));req.put("orderType","DINE_IN");req.put("reservationId",r.getId());mvc.perform(post("/api/orders").with(as(owner,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(req))).andExpect(status().isForbidden());
 }
 @Test void oversizedReservationTextAndBadImagePathsAreRejected()throws Exception{
  var owner=user("CUSTOMER");var admin=user("ADMIN");mvc.perform(patch("/api/reservations/999/cancel").with(as(owner,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("reason","x".repeat(501))))).andExpect(status().isBadRequest());
  mvc.perform(put("/api/reservations/999").with(as(owner,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("seatingPreference","x".repeat(51))))).andExpect(status().isBadRequest());
  var dish=dish();mvc.perform(post("/api/admin/menu/items").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("name","Unsafe","categoryId",dish.getCategory().getId(),"price",10,"imageUrl","javascript:alert(1)","preparationMinutes",15)))).andExpect(status().isBadRequest());
  mvc.perform(delete("/api/admin/menu/categories/"+dish.getCategory().getId()).with(as(admin,"ADMIN"))).andExpect(status().isConflict());
 }
 @Test void stockDeliveryIsAtomicAndCannotBeReceivedTwice()throws Exception{
  var admin=user("ADMIN");long item=stock(admin,"Rice",10);long supplier=supplier(admin);
  var req=Map.of("supplierId",supplier,"items",List.of(Map.of("inventoryItemId",item,"quantity",2,"unitCost",100)));
  long po=id(mvc.perform(post("/api/inventory/purchase-orders").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(req))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
  mvc.perform(get("/api/inventory/purchase-orders/"+po).with(as(admin,"ADMIN"))).andExpect(status().isOk());
  mvc.perform(delete("/api/suppliers/"+supplier).with(as(admin,"ADMIN"))).andExpect(status().isConflict());
  mvc.perform(patch("/api/inventory/purchase-orders/"+po+"/receive").with(as(admin,"ADMIN"))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RECEIVED"));
  assertThat(inventory.findById(item).orElseThrow().getCurrentQuantity()).isEqualByComparingTo("12");
  assertThat(movements.findByInventoryItemIdOrderByCreatedAtDesc(item)).hasSize(2).anyMatch(m->m.getMovementType()==MovementType.PURCHASE && m.getReferenceId().equals(po));
  mvc.perform(patch("/api/inventory/purchase-orders/"+po+"/receive").with(as(admin,"ADMIN"))).andExpect(status().isConflict());assertThat(inventory.findById(item).orElseThrow().getCurrentQuantity()).isEqualByComparingTo("12");
  mvc.perform(delete("/api/inventory/purchase-orders/"+po).with(as(admin,"ADMIN"))).andExpect(status().isConflict());
 }
 @Test void purchasingRejectsDuplicateLinesAndCustomers()throws Exception{
  var admin=user("ADMIN");long item=stock(admin,"Rice",10);long supplier=supplier(admin);var line=Map.of("inventoryItemId",item,"quantity",1,"unitCost",100);
  mvc.perform(post("/api/inventory/purchase-orders").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("supplierId",supplier,"items",List.of(line,line))))).andExpect(status().isBadRequest());
  mvc.perform(get("/api/inventory/purchase-orders").with(as(user("CUSTOMER"),"CUSTOMER"))).andExpect(status().isForbidden());
 }
 @Test void duplicateStockIsRejectedAndLowStockNotifiesOnlyOnCrossing()throws Exception{
  var admin=user("ADMIN");long item=stock(admin,"Rice",6);
  mvc.perform(post("/api/inventory/items").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" rice \",\"unit\":\"kg\",\"currentQuantity\":10,\"reorderLevel\":5}")).andExpect(status().isConflict());
  for(int i=0;i<2;i++)mvc.perform(patch("/api/inventory/items/"+item+"/adjust").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content("{\"delta\":-1}")).andExpect(status().isOk());
  assertThat(notifications.findByUserIdOrderByCreatedAtDesc(admin.getId())).hasSize(1);mvc.perform(get("/api/inventory/items/"+item+"/movements").with(as(admin,"ADMIN"))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
 }
 @Test void attendanceCrudChecksTimeOwnershipOfAssignmentsAndProtectsHistory()throws Exception{
  var admin=user("ADMIN");var worker=profile(user("WAITER"));var day=LocalDate.now(zone).minusDays(1);var shift=shift(day,1);var assignment=assign(shift,worker);
  var req=new HashMap<String,Object>();req.put("assignmentId",assignment.getId());req.put("status","PRESENT");req.put("checkInAt",day+"T10:00:00");req.put("checkOutAt",day+"T18:00:00");
  long record=id(mvc.perform(post("/api/admin/staff/attendance").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(req))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
  mvc.perform(post("/api/admin/staff/attendance").with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(req))).andExpect(status().isConflict());
  req.put("checkOutAt",day+"T09:00:00");mvc.perform(put("/api/admin/staff/attendance/"+record).with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(req))).andExpect(status().isBadRequest());
  req.put("checkOutAt",day+"T17:00:00");req.put("status","HALF_DAY");mvc.perform(put("/api/admin/staff/attendance/"+record).with(as(admin,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body(req))).andExpect(status().isOk());
  mvc.perform(get("/api/admin/staff/attendance").with(as(admin,"ADMIN"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].staffName").value("Test WAITER"));
  mvc.perform(delete("/api/admin/staff/shifts/"+shift.getId()+"/assignments/"+worker.getId()).with(as(admin,"ADMIN"))).andExpect(status().isConflict());
  mvc.perform(delete("/api/admin/staff/attendance/"+record).with(as(admin,"ADMIN"))).andExpect(status().isNoContent());assertThat(attendance.count()).isZero();
 }
 @Test void notificationsCanOnlyBeDeletedByTheirOwner()throws Exception{
  var owner=user("CUSTOMER");var other=user("CUSTOMER");var n=notifications.save(Notification.builder().user(owner).title("Notice").message("Hello").type("TEST").isRead(false).build());
  notifications.save(Notification.builder().user(other).title("Other").message("Hello").type("TEST").isRead(false).build());
  mvc.perform(delete("/api/notifications/"+n.getId()).with(as(other,"CUSTOMER"))).andExpect(status().isForbidden());
  mvc.perform(delete("/api/notifications").with(as(owner,"CUSTOMER"))).andExpect(status().isNoContent());assertThat(notifications.findByUserIdOrderByCreatedAtDesc(owner.getId())).isEmpty();assertThat(notifications.findByUserIdOrderByCreatedAtDesc(other.getId())).hasSize(1);
 }
 @Test void eventEditingPreservesIdAndRevalidatesOwnershipCapacityAndOverlap()throws Exception{
  var owner=user("CUSTOMER");var other=user("CUSTOMER");var hall=halls.save(EventHall.builder().name("Test Hall").capacity(200).location("Colombo").isActive(true).build());
  var pkg=packages.save(EventPackage.builder().name("Test Package").eventType("BIRTHDAY").basePrice(new BigDecimal("1000.00")).minimumGuests(1).maximumGuests(200).isActive(true).build());
  var req=new HashMap<String,Object>();req.put("hallId",hall.getId());req.put("packageId",pkg.getId());req.put("eventDate",LocalDate.now(zone).plusDays(5).toString());req.put("startTime","10:00");req.put("endTime","12:00");req.put("guestCount",50);
  long booking=id(mvc.perform(post("/api/events/bookings").with(as(owner,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(req))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
  mvc.perform(put("/api/events/bookings/"+booking).with(as(other,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(req))).andExpect(status().isForbidden());
  req.put("guestCount",100);mvc.perform(put("/api/events/bookings/"+booking).with(as(owner,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(req))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(booking)).andExpect(jsonPath("$.guestCount").value(100));
  req.put("guestCount",201);mvc.perform(put("/api/events/bookings/"+booking).with(as(owner,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(req))).andExpect(status().isBadRequest());
 }
}
