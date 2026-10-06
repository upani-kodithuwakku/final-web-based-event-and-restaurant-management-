package com.group06.restaurantevent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group06.restaurantevent.common.audit.AuditLogRepository;
import com.group06.restaurantevent.common.enums.EventBookingStatus;
import com.group06.restaurantevent.common.enums.OrderStatus;
import com.group06.restaurantevent.common.enums.OrderType;
import com.group06.restaurantevent.events.entity.EventBooking;
import com.group06.restaurantevent.events.entity.EventHall;
import com.group06.restaurantevent.events.entity.EventPackage;
import com.group06.restaurantevent.events.repository.EventBookingRepository;
import com.group06.restaurantevent.events.repository.EventHallRepository;
import com.group06.restaurantevent.events.repository.EventPackageRepository;
import com.group06.restaurantevent.orders.entity.FoodOrder;
import com.group06.restaurantevent.orders.entity.FoodOrderItem;
import com.group06.restaurantevent.orders.repository.FoodOrderRepository;
import com.group06.restaurantevent.payment.repository.CustomerPaymentRepository;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.UserRepository;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:payments;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.open-in-view=false", "app.seed.demo-users=false",
    "app.jwt.secret=test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm-testing",
})
class CustomerPaymentTests {
    // Simulated gateway: any 12-digit number is accepted; no real card is involved.
    private static final String VALID_CARD = "4242 4242 4242";
    private static final String TOO_SHORT_CARD = "4242 4242 42";

    @Autowired com.group06.restaurantevent.users.repository.RoleRepository roleRepository;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired FoodOrderRepository orders;
    @Autowired EventHallRepository halls;
    @Autowired EventPackageRepository packages;
    @Autowired EventBookingRepository bookings;
    @Autowired CustomerPaymentRepository payments;
    @Autowired AuditLogRepository auditLogs;
    @Autowired com.group06.restaurantevent.notifications.repository.NotificationRepository notifications;
    private final Map<Long, User> savedUsers = new HashMap<>();

    // Same principal shape as JwtAuthFilter: Spring Security UserDetails keyed by email.
    private RequestPostProcessor as(long key, String role) {
        User user = savedUsers.computeIfAbsent(key, k -> users.save(User.builder().fullName("User " + k)
            .email(k + "@pay.test").passwordHash("not-used").isActive(true).build()));
        var principal = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
            .password("not-used").roles(role).build();
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
    private long idOf(long key) { return savedUsers.get(key).getId(); }

    private FoodOrder order(long customerKey, OrderStatus status, String subtotal) {
        as(customerKey, "CUSTOMER");
        FoodOrder order = FoodOrder.builder().orderReference("ORD-T-" + System.nanoTime() % 100000)
            .customerId(idOf(customerKey)).orderType(OrderType.DINE_IN).status(status)
            .subtotal(new BigDecimal(subtotal)).build();
        order.getItems().add(FoodOrderItem.builder().order(order).menuItemId(1L).itemNameSnapshot("Test curry")
            .unitPriceSnapshot(new BigDecimal(subtotal)).quantity(1).lineTotal(new BigDecimal(subtotal)).build());
        return orders.save(order);
    }

    private EventBooking booking(long customerKey, EventBookingStatus status) {
        as(customerKey, "CUSTOMER");
        EventHall hall = halls.save(EventHall.builder().name("Hall " + System.nanoTime()).capacity(100).isActive(true).build());
        EventPackage pkg = packages.save(EventPackage.builder().name("Birthday package").eventType("BIRTHDAY")
            .basePrice(new BigDecimal("45000.00")).minimumGuests(10).maximumGuests(50).isActive(true).build());
        return bookings.save(EventBooking.builder().bookingReference("EVT-T-" + System.nanoTime() % 100000)
            .customerId(idOf(customerKey)).hall(hall).eventPackage(pkg).eventDate(LocalDate.now().plusDays(10))
            .startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(22, 0)).guestCount(20).status(status)
            .depositAmount(new BigDecimal("13500.00")).build());
    }

    private String cardBody(String target, long id, String number, String expiry) throws Exception {
        return mapper.writeValueAsString(Map.of(target, id, "method", "CARD",
            "card", Map.of("holderName", "Test Customer", "number", number, "expiry", expiry, "cvv", "123")));
    }
    private String outletBody(String target, long id) throws Exception {
        return mapper.writeValueAsString(Map.of(target, id, "method", "PAY_AT_OUTLET"));
    }

    @Test void foodCanBePaidAtCheckoutButUnconfirmedEventsAndCancelledOrdersCannot() throws Exception {
        FoodOrder justPlaced = order(1, OrderStatus.PENDING, "1000.00");
        FoodOrder cancelled = order(1, OrderStatus.CANCELLED, "1000.00");
        EventBooking pendingEvent = booking(1, EventBookingStatus.PENDING);
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(outletBody("foodOrderId", justPlaced.getId()))).andExpect(status().isCreated());
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(outletBody("foodOrderId", cancelled.getId()))).andExpect(status().isConflict());
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(outletBody("eventBookingId", pendingEvent.getId()))).andExpect(status().isConflict());
        assertThat(payments.count()).isOne();
    }

    @Test void cardPaymentIsPaidImmediatelyAndStoresOnlyLastFourDigits() throws Exception {
        FoodOrder order = order(1, OrderStatus.PREPARING, "7200.00");
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(cardBody("foodOrderId", order.getId(), VALID_CARD, "12/39")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.method").value("CARD"))
            .andExpect(jsonPath("$.amount").value(7920.00))      // 7200 + 10% service charge
            .andExpect(jsonPath("$.cardLast4").value("4242"))
            .andExpect(jsonPath("$.cardBrand").value("VISA"))
            .andExpect(jsonPath("$.targetReference").value(order.getOrderReference()));
        var saved = payments.findByFoodOrderId(order.getId()).orElseThrow();
        assertThat(saved.toString()).doesNotContain("4242424242424242").doesNotContain("123,");
    }

    @Test void anyTwelveDigitCardIsAccepted() throws Exception {
        FoodOrder order = order(1, OrderStatus.PENDING, "1000.00");
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(cardBody("foodOrderId", order.getId(), "1234 5678 9012", "12/39")))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.cardLast4").value("9012"));
    }

    @Test void shortOrExpiredCardsAndMissingDetailsAreRejected() throws Exception {
        FoodOrder order = order(1, OrderStatus.READY, "1000.00");
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(cardBody("foodOrderId", order.getId(), TOO_SHORT_CARD, "12/39"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(cardBody("foodOrderId", order.getId(), VALID_CARD, "01/20"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("foodOrderId", order.getId(), "method", "CARD"))))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("method", "PAY_AT_OUTLET")))).andExpect(status().isBadRequest());
        assertThat(payments.count()).isZero();
    }

    @Test void payAtOutletStaysPendingUntilStaffCollectOrCustomerSwitchesToCard() throws Exception {
        FoodOrder order = order(1, OrderStatus.SERVED, "1000.00");
        String body = mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(outletBody("foodOrderId", order.getId())))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.paidAt").value(nullValue()))
            .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(body).get("id").asLong();

        // Only one payment per order.
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(outletBody("foodOrderId", order.getId()))).andExpect(status().isConflict());

        mvc.perform(put("/api/payments/" + id).with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("method", "CARD", "card",
                    Map.of("holderName", "Test Customer", "number", VALID_CARD, "expiry", "12/39", "cvv", "123")))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAID"));

        // A completed payment cannot be changed by the customer.
        mvc.perform(put("/api/payments/" + id).with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"method\":\"PAY_AT_OUTLET\"}")).andExpect(status().isConflict());
    }

    @Test void cashierMarksOutletPaymentAsPaidAndChangeIsAudited() throws Exception {
        FoodOrder order = order(1, OrderStatus.COMPLETED, "1000.00");
        String body = mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(outletBody("foodOrderId", order.getId()))).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(body).get("id").asLong();

        mvc.perform(patch("/api/payments/" + id + "/status").with(as(1, "CUSTOMER"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/payments/" + id + "/status").with(as(9, "CASHIER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAID"));
        mvc.perform(patch("/api/payments/" + id + "/status").with(as(9, "CASHIER"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PENDING\"}")).andExpect(status().isBadRequest());
        assertThat(auditLogs.findAll()).anyMatch(a -> "PAYMENT_STATUS_UPDATED".equals(a.getAction()));
    }

    @Test void customersCannotPayForOrSeeOtherCustomersPayments() throws Exception {
        FoodOrder order = order(1, OrderStatus.PREPARING, "1000.00");
        mvc.perform(post("/api/payments").with(as(2, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(outletBody("foodOrderId", order.getId()))).andExpect(status().isForbidden());
        String body = mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(outletBody("foodOrderId", order.getId()))).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(body).get("id").asLong();
        mvc.perform(get("/api/payments/" + id).with(as(2, "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/payments/my").with(as(2, "CUSTOMER"))).andExpect(content().json("[]"));
        mvc.perform(get("/api/payments").with(as(2, "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/payments/" + id).with(as(9, "CASHIER"))).andExpect(status().isOk());
        mvc.perform(get("/api/payments").with(as(9, "MANAGER"))).andExpect(jsonPath("$.length()").value(1));
    }

    @Test void onlyAdminCanDeletePayments() throws Exception {
        FoodOrder order = order(1, OrderStatus.PREPARING, "1000.00");
        String body = mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(outletBody("foodOrderId", order.getId()))).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(body).get("id").asLong();
        for (String role : List.of("CUSTOMER", "CASHIER", "MANAGER")) {
            mvc.perform(delete("/api/payments/" + id).with(as(1, role))).andExpect(status().isForbidden());
        }
        mvc.perform(delete("/api/payments/" + id).with(as(8, "ADMIN"))).andExpect(status().isNoContent());
        assertThat(payments.findById(id)).isEmpty();
        assertThat(auditLogs.findAll()).anyMatch(a -> "PAYMENT_DELETED".equals(a.getAction()));
        mvc.perform(delete("/api/payments/" + id).with(as(8, "ADMIN"))).andExpect(status().isNotFound());
    }

    @Test void summaryShowsOnlyFoodTotalWhenCustomerOnlyOrderedFood() throws Exception {
        order(1, OrderStatus.PREPARING, "1000.00");
        order(1, OrderStatus.PENDING, "500.00");   // just placed: payable at checkout, so counted
        order(1, OrderStatus.CANCELLED, "800.00"); // cancelled without payment: hidden
        booking(1, EventBookingStatus.PENDING);    // unconfirmed event: listed but not counted
        mvc.perform(get("/api/payments/summary").with(as(1, "CUSTOMER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.foodOrders.length()").value(2))
            .andExpect(jsonPath("$.foodTotal").value(1650.00))
            .andExpect(jsonPath("$.eventTotal").value(nullValue()))
            .andExpect(jsonPath("$.grandTotal").value(1650.00))
            .andExpect(jsonPath("$.amountDue").value(1650.00));
    }

    @Test void summaryShowsOnlyEventTotalWhenCustomerOnlyBookedAnEvent() throws Exception {
        booking(1, EventBookingStatus.CONFIRMED);
        mvc.perform(get("/api/payments/summary").with(as(1, "CUSTOMER")))
            .andExpect(jsonPath("$.foodTotal").value(nullValue()))
            .andExpect(jsonPath("$.eventTotal").value(45000.00))
            .andExpect(jsonPath("$.grandTotal").value(45000.00));
    }

    @Test void summaryKeepsFoodAndEventTotalsSeparateAndAddsThemUp() throws Exception {
        FoodOrder order = order(1, OrderStatus.READY, "2000.00");
        EventBooking event = booking(1, EventBookingStatus.CONFIRMED);
        // Food paid by card, event to be paid at the outlet: both still appear in the totals.
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(cardBody("foodOrderId", order.getId(), VALID_CARD, "12/39"))).andExpect(status().isCreated());
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(outletBody("eventBookingId", event.getId()))).andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(45000.00));
        mvc.perform(get("/api/payments/summary").with(as(1, "CUSTOMER")))
            .andExpect(jsonPath("$.foodTotal").value(2200.00))
            .andExpect(jsonPath("$.eventTotal").value(45000.00))
            .andExpect(jsonPath("$.grandTotal").value(47200.00))
            .andExpect(jsonPath("$.amountPaid").value(2200.00))
            .andExpect(jsonPath("$.amountDue").value(45000.00))
            .andExpect(jsonPath("$.foodOrders[0].paymentStatus").value("PAID"))
            .andExpect(jsonPath("$.foodOrders[0].items[0].name").value("Test curry"))
            .andExpect(jsonPath("$.foodOrders[0].items[0].quantity").value(1))
            .andExpect(jsonPath("$.eventBookings[0].items[0].lineTotal").value(45000.00))
            .andExpect(jsonPath("$.eventBookings[0].paymentMethod").value("PAY_AT_OUTLET"));
        // Another customer's bill is empty.
        mvc.perform(get("/api/payments/summary").with(as(2, "CUSTOMER")))
            .andExpect(jsonPath("$.grandTotal").value(nullValue()));
    }



    @Autowired com.group06.restaurantevent.reservations.repository.TableReservationRepository reservations;
    @Autowired com.group06.restaurantevent.reservations.repository.RestaurantTableRepository tables;

    private com.group06.restaurantevent.reservations.entity.TableReservation reservation(long customerKey, String status, int days) {
        as(customerKey, "CUSTOMER");
        var table = tables.save(com.group06.restaurantevent.reservations.entity.RestaurantTable.builder()
                .tableNumber("P" + System.nanoTime() % 100000000).capacity(8).location("INDOOR")
                .currentStatus(com.group06.restaurantevent.common.enums.TableStatus.AVAILABLE).isActive(true).build());
        return reservations.save(com.group06.restaurantevent.reservations.entity.TableReservation.builder()
                .bookingReference("RES-T-" + System.nanoTime()).customer(savedUsers.get(customerKey))
                .table(table).reservationDate(LocalDate.now(java.time.ZoneId.of("Asia/Colombo")).plusDays(days))
                .startTime(LocalTime.of(19, 0)).endTime(LocalTime.of(21, 0)).guestCount(4)
                .status(com.group06.restaurantevent.common.enums.ReservationStatus.valueOf(status))
                .contactName("Test Customer").contactPhone("0771234567").build());
    }

    @Test void reservationDepositUsesGuestsAndAllowsOnlyOnePayment() throws Exception {
        var reservation = reservation(1, "CONFIRMED", 2);
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(outletBody("tableReservationId", reservation.getId())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.amount").value(2000.00))
                .andExpect(jsonPath("$.purpose").value("TABLE_RESERVATION"));
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(outletBody("tableReservationId", reservation.getId()))).andExpect(status().isConflict());
    }

    @Test void cancelledPastAndOtherCustomersReservationsCannotBePaid() throws Exception {
        for (String state : List.of("CANCELLED", "NO_SHOW", "COMPLETED", "CHECKED_IN")) {
            var reservation = reservation(1, state, 2);
            mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                    .content(outletBody("tableReservationId", reservation.getId()))).andExpect(status().isConflict());
        }
        var past = reservation(1, "CONFIRMED", -1);
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(outletBody("tableReservationId", past.getId()))).andExpect(status().isConflict());
        var owned = reservation(1, "PENDING", 2);
        mvc.perform(post("/api/payments").with(as(2, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(outletBody("tableReservationId", owned.getId()))).andExpect(status().isForbidden());
    }

    @Test void cancellationRefundsCardDepositAndDeletesPendingOutletDeposit() throws Exception {
        var paid = reservation(1, "CONFIRMED", 2);
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(cardBody("tableReservationId", paid.getId(), VALID_CARD, "12/39"))).andExpect(status().isCreated());
        mvc.perform(patch("/api/reservations/" + paid.getId() + "/cancel").with(as(1, "CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Changed plans\"}")).andExpect(status().isOk());
        assertThat(payments.findByTableReservationId(paid.getId()).orElseThrow().getStatus())
                .isEqualTo(com.group06.restaurantevent.common.enums.PaymentStatus.REFUNDED);
        var outlet = reservation(1, "CONFIRMED", 3);
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(outletBody("tableReservationId", outlet.getId()))).andExpect(status().isCreated());
        mvc.perform(patch("/api/reservations/" + outlet.getId() + "/cancel").with(as(1, "CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Changed plans\"}")).andExpect(status().isOk());
        assertThat(payments.findByTableReservationId(outlet.getId())).isEmpty();
        assertThat(auditLogs.findAll()).anyMatch(a -> "RESERVATION_DEPOSIT_REFUNDED".equals(a.getAction()))
                .anyMatch(a -> "RESERVATION_OUTLET_PAYMENT_CANCELLED".equals(a.getAction()));
    }

    @Test void summaryKeepsThreeTotalsAndIncludesOutletDeposits() throws Exception {
        order(1, OrderStatus.READY, "1000.00");
        booking(1, EventBookingStatus.CONFIRMED);
        var reservation = reservation(1, "CONFIRMED", 2);
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(outletBody("tableReservationId", reservation.getId()))).andExpect(status().isCreated());
        mvc.perform(get("/api/payments/summary").with(as(1, "CUSTOMER")))
                .andExpect(jsonPath("$.foodTotal").value(1100.00))
                .andExpect(jsonPath("$.eventTotal").value(45000.00))
                .andExpect(jsonPath("$.reservationTotal").value(2000.00))
                .andExpect(jsonPath("$.grandTotal").value(48100.00))
                .andExpect(jsonPath("$.amountDue").value(48100.00))
                .andExpect(jsonPath("$.tableReservations[0].paymentMethod").value("PAY_AT_OUTLET"));
    }

    @Test void confirmationNamesEachPaymentTypeAndOutletCollectionUsesSameText() throws Exception {
        var food = order(1, OrderStatus.PENDING, "1800.00");
        var event = booking(1, EventBookingStatus.CONFIRMED);
        var reservation = reservation(1, "CONFIRMED", 2);
        String[] targets = {"foodOrderId", "eventBookingId", "tableReservationId"};
        Long[] ids = {food.getId(), event.getId(), reservation.getId()};
        String[] labels = {"food order " + food.getOrderReference(), "your event booking " + event.getBookingReference(), "your table reservation " + reservation.getBookingReference()};
        for (int j = 0; j < targets.length; j++) {
            var body = mapper.readTree(cardBody(targets[j], ids[j], VALID_CARD, "12/39"));
            if (j == 2) ((com.fasterxml.jackson.databind.node.ObjectNode) body).put("method", "PAY_AT_OUTLET");
            String response = mvc.perform(post("/api/payments").with(as(1, "CUSTOMER"))
                    .contentType(MediaType.APPLICATION_JSON).content(body.toString())).andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            if (j == 2) {
                long paymentId = mapper.readTree(response).get("id").asLong();
                response = mvc.perform(patch("/api/payments/" + paymentId + "/status").with(as(9, "CASHIER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}"))
                        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            }
            var receipt = mapper.readTree(response);
            assertThat(receipt.get("confirmationMessage").asText()).contains(labels[j]).contains("(ref PMT-");
        }
    }

    @Test void customerSavedCatalogDoesNotRequireAvailabilitySearchAndCannotMutateTables() throws Exception {
        var reservation = reservation(1, "CONFIRMED", 2);
        mvc.perform(get("/api/reservations/tables").with(as(1, "CUSTOMER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[?(@.id == " + reservation.getTable().getId() + ")]").isNotEmpty());
        mvc.perform(post("/api/admin/tables").with(as(1, "CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"tableNumber\":\"NOPE\",\"capacity\":4,\"location\":\"INDOOR\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/cashier/summary").with(as(1, "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/cashier/summary").with(as(9, "CASHIER"))).andExpect(status().isOk());
    }

    @Autowired com.group06.restaurantevent.config.EventCatalogSeeder eventCatalogSeeder;

    @Test void eventEnquiryConfirmationAndCardPaymentWorkWithEmailPrincipal() throws Exception {
        var template = booking(1, EventBookingStatus.PENDING);
        var request = Map.of("hallId", template.getHall().getId(), "packageId", template.getEventPackage().getId(),
                "eventDate", LocalDate.now().plusDays(20).toString(), "startTime", "18:00", "endTime", "22:00", "guestCount", 20);
        String response = mvc.perform(post("/api/events/bookings").with(as(1, "CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(response).get("id").asLong();
        mvc.perform(get("/api/events/bookings/my").with(as(1, "CUSTOMER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == " + id + ")]").isNotEmpty());
        mvc.perform(get("/api/events/bookings/" + id).with(as(2, "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(outletBody("eventBookingId", id))).andExpect(status().isConflict());
        mvc.perform(patch("/api/event-coordinator/bookings/" + id + "/approve").with(as(9, "EVENT_COORDINATOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        var body = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(cardBody("eventBookingId", id, VALID_CARD, "12/39"));
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.amount").value(45000))
                .andExpect(jsonPath("$.confirmationMessage").value(org.hamcrest.Matchers.containsString("your event booking EVT-")));
    }

    @Test void cancelledEventOutletPaymentCannotBeCollectedOrChangedToCard() throws Exception {
        var event = booking(1, EventBookingStatus.CONFIRMED);
        String response = mvc.perform(post("/api/payments").with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(outletBody("eventBookingId", event.getId()))).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long paymentId = mapper.readTree(response).get("id").asLong();
        mvc.perform(patch("/api/events/bookings/" + event.getId() + "/cancel").with(as(2, "CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(patch("/api/events/bookings/" + event.getId() + "/cancel").with(as(1, "CUSTOMER"))).andExpect(status().isOk());
        mvc.perform(patch("/api/payments/" + paymentId + "/status").with(as(9, "CASHIER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}")).andExpect(status().isConflict());
        mvc.perform(put("/api/payments/" + paymentId).with(as(1, "CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
                .content(cardBody("eventBookingId", event.getId(), VALID_CARD, "12/39"))).andExpect(status().isConflict());
    }

    @Test void celebrationCatalogIsPublicAndSeedingPreservesExistingPackages() throws Exception {
        mvc.perform(get("/api/events/packages")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.eventType == 'BABY_SHOWER')]").isNotEmpty());
        mvc.perform(get("/api/events/halls")).andExpect(status().isOk());
        var pkg = packages.findAll().stream().filter(p -> "Birthday Garden Party".equals(p.getName())).findFirst().orElseThrow();
        pkg.setBasePrice(new BigDecimal("47000.00")); pkg.setActive(false); packages.save(pkg);
        long count = packages.count(); long hallCount = halls.count();
        eventCatalogSeeder.run(); eventCatalogSeeder.run();
        assertThat(packages.count()).isEqualTo(count); assertThat(halls.count()).isEqualTo(hallCount);
        assertThat(packages.findById(pkg.getId()).orElseThrow().getBasePrice()).isEqualByComparingTo("47000.00");
        assertThat(packages.findById(pkg.getId()).orElseThrow().isActive()).isFalse();
    }

    @Test void eventEnquiriesValidateHallCapacityAndTimeOrder() throws Exception {
        var event = booking(1, EventBookingStatus.PENDING);
        var hall = event.getHall(); hall.setCapacity(10); halls.save(hall);
        var request = new HashMap<String, Object>(Map.of("hallId", hall.getId(), "packageId", event.getEventPackage().getId(),
                "eventDate", LocalDate.now().plusDays(20).toString(), "startTime", "18:00", "endTime", "22:00", "guestCount", 20));
        mvc.perform(post("/api/events/bookings").with(as(1, "CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request))).andExpect(status().isBadRequest());
        hall.setCapacity(100); halls.save(hall); request.put("endTime", "17:00");
        mvc.perform(post("/api/events/bookings").with(as(1, "CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }


    @Test void reservationObserversWriteNotificationsAndHistoryOnlyForSuccessfulChanges() throws Exception {
        var r = reservation(1, "CONFIRMED", 2);
        mvc.perform(patch("/api/admin/reservations/"+r.getId()+"/check-in").with(as(9,"ADMIN")))
            .andExpect(status().isOk());
        var history = auditLogs.findByEntityNameAndEntityIdOrderByCreatedAtDescIdDesc("TableReservation",r.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getOldValue()).isEqualTo("CONFIRMED");
        assertThat(history.get(0).getNewValue()).isEqualTo("CHECKED_IN");
        assertThat(history.get(0).getUserId()).isEqualTo(idOf(9));
        assertThat(notifications.findByUserIdOrderByCreatedAtDesc(idOf(1)))
            .anyMatch(n -> n.getMessage().contains(r.getBookingReference()) && n.getMessage().contains("checked in"));
        mvc.perform(patch("/api/admin/reservations/"+r.getId()+"/check-in").with(as(9,"ADMIN")))
            .andExpect(status().isBadRequest());
        assertThat(auditLogs.findByEntityNameAndEntityIdOrderByCreatedAtDescIdDesc("TableReservation",r.getId())).hasSize(1);
        mvc.perform(patch("/api/admin/reservations/"+r.getId()+"/complete").with(as(9,"ADMIN")))
            .andExpect(status().isOk());
        assertThat(auditLogs.findByEntityNameAndEntityIdOrderByCreatedAtDescIdDesc("TableReservation",r.getId())).hasSize(2);
        mvc.perform(get("/api/admin/reservations/"+r.getId()+"/history").with(as(9,"ADMIN")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2));
        mvc.perform(get("/api/admin/reservations/"+r.getId()+"/history").with(as(1,"CUSTOMER")))
            .andExpect(status().isForbidden());
    }

    @Test void adminCanCreateCustomerReservationButCustomerCannotUseAdminEndpoint() throws Exception {
        var original = reservation(1, "CONFIRMED", 2);
        var customer = savedUsers.get(1L);
        var role = new com.group06.restaurantevent.users.entity.Role();
        role.setName("CUSTOMER"); role.setDescription("Customer");
        role = roleRepository.findByName("CUSTOMER").orElseGet(() -> { var r = new com.group06.restaurantevent.users.entity.Role(); r.setName("CUSTOMER"); return roleRepository.save(r); });
        customer.getRoles().add(role); users.save(customer);
        String body = mapper.writeValueAsString(Map.of("customerEmail",customer.getEmail(),"tableId",original.getTable().getId(),
            "reservationDate",LocalDate.now().plusDays(30).toString(), "startTime","18:00",
            "guestCount",2,"contactName","Phone booking","contactPhone","0771234567"));
        mvc.perform(post("/api/admin/reservations")
            .with(as(9,"ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data.contactName").value("Phone booking"));
        mvc.perform(post("/api/admin/reservations")
            .with(as(1,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
    }

    @Test void waiterCanReassignReservationsWithoutBypassingCapacityOrOverlapRules() throws Exception {
        var r = reservation(1, "CONFIRMED", 2);
        var small = tables.save(com.group06.restaurantevent.reservations.entity.RestaurantTable.builder()
                .tableNumber("SMALL").capacity(1).location("INDOOR")
                .currentStatus(com.group06.restaurantevent.common.enums.TableStatus.AVAILABLE).isActive(true).build());
        mvc.perform(put("/api/admin/reservations/" + r.getId()).with(as(9, "WAITER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("tableId", small.getId(), "guestCount", 4))))
                .andExpect(status().isBadRequest());

        var conflict = reservation(2, "CONFIRMED", 2);
        mvc.perform(put("/api/admin/reservations/" + r.getId()).with(as(9, "WAITER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("tableId", conflict.getTable().getId()))))
                .andExpect(status().isConflict());

        mvc.perform(put("/api/admin/reservations/" + r.getId()).with(as(9, "WAITER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("tableId", conflict.getTable().getId(), "startTime", "17:00"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.table.id").value(conflict.getTable().getId()));
        assertThat(auditLogs.findAll()).anyMatch(log -> "RESERVATION_UPDATED_BY_STAFF".equals(log.getAction())
                && savedUsers.get(9L).getId().equals(log.getUserId()));
    }

    @Test void staffCancellationClearsPendingDepositAndKeepsNoShowProtection() throws Exception {
        var r = reservation(1, "CONFIRMED", 2);
        mvc.perform(post("/api/payments").with(as(1, "CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content(outletBody("tableReservationId", r.getId())))
                .andExpect(status().isCreated());
        mvc.perform(patch("/api/admin/reservations/" + r.getId() + "/cancel").with(as(9, "WAITER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Customer called the restaurant\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("CANCELLED"));
        assertThat(payments.findByTableReservationId(r.getId())).isEmpty();
        assertThat(auditLogs.findAll()).anyMatch(log -> "RESERVATION_CANCELLED_BY_STAFF".equals(log.getAction())
                && savedUsers.get(9L).getId().equals(log.getUserId()));
        var noShow = reservation(1, "NO_SHOW", 3);
        mvc.perform(patch("/api/admin/reservations/" + noShow.getId() + "/cancel").with(as(9, "WAITER")))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/reservations/" + noShow.getId() + "/cancel").with(as(1, "CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test void sixteenDigitCardIsRejected() throws Exception {
        var o = order(1,OrderStatus.PENDING,"1000.00");
        mvc.perform(post("/api/payments").with(as(1,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON)
            .content(cardBody("foodOrderId",o.getId(),"4242 4242 4242 4242","12/39")))
            .andExpect(status().isBadRequest());
    }

    @Test void reservationRejectsBadPhonePastDateAndOutsideServiceHours() throws Exception {
        var r = reservation(1, "CONFIRMED", 2);
        for (String phone : List.of("077123456", "07712345678", "+94771234567", "abcdefghij")) {
            mvc.perform(put("/api/reservations/"+r.getId()).with(as(1,"CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("contactPhone",phone))))
                .andExpect(status().isBadRequest());
        }
        mvc.perform(put("/api/reservations/"+r.getId()).with(as(1,"CUSTOMER"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"reservationDate\":\"2000-01-01\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(put("/api/reservations/"+r.getId()).with(as(1,"CUSTOMER"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"startTime\":\"23:00\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void reportsCountNewBookingsForFutureCelebrations() throws Exception {
        var b = booking(1,EventBookingStatus.CONFIRMED);
        String today=LocalDate.now().toString();
        mvc.perform(get("/api/admin/reports/events").param("from",today).param("to",today).with(as(9,"ADMIN")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalBookings").value(1))
            .andExpect(jsonPath("$.confirmedGuests").value(b.getGuestCount()))
            .andExpect(jsonPath("$.confirmedValue").value(45000));
    }
    @Test void eventCatalogCrudValidatesGuestLimitsAndPreservesHistory() throws Exception {
        String response=mvc.perform(post("/api/event-coordinator/catalog/halls").with(as(9,"EVENT_COORDINATOR"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Test Hall CRUD\",\"capacity\":30,\"location\":\"Ground Floor\"}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id=mapper.readTree(response).get("id").asLong();
        mvc.perform(put("/api/event-coordinator/catalog/halls/"+id).with(as(9,"EVENT_COORDINATOR"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Updated Hall\",\"capacity\":40,\"location\":\"First Floor\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.capacity").value(40));
        mvc.perform(post("/api/event-coordinator/catalog/packages").with(as(9,"EVENT_COORDINATOR"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Bad limits\",\"eventType\":\"BIRTHDAY\",\"basePrice\":1000,\"minimumGuests\":20,\"maximumGuests\":10}"))
            .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/event-coordinator/catalog/halls/"+id).with(as(9,"EVENT_COORDINATOR"))).andExpect(status().isNoContent());
        assertThat(halls.findById(id).orElseThrow().isActive()).isFalse();
        mvc.perform(post("/api/event-coordinator/catalog/halls").with(as(1,"CUSTOMER"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Forbidden Hall\",\"capacity\":30,\"location\":\"Ground floor\"}")) .andExpect(status().isForbidden());
    }
    @Test void eachMemberRejectsInvalidInput() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fullName\":\"User\",\"email\":\"bad-email\",\"password\":\"short\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/menu/items").with(as(9,"ADMIN")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"categoryId\":1,\"name\":\"Bad price\",\"price\":-10,\"preparationMinutes\":-1}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/inventory/items/1/adjust").with(as(9,"INVENTORY_MANAGER"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":\"not a number\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/staff/shifts").with(as(9,"ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("shiftDate",LocalDate.now().plusDays(2).toString(),"startTime","18:00","endTime","10:00","roleRequired","WAITER","requiredStaffCount",2))))
            .andExpect(status().isBadRequest());
    }

    @Test void scheduledShiftsSupportCreateReadUpdateAndCancel() throws Exception {
        String date=LocalDate.now().plusDays(2).toString();
        String role="WAITER";
        // DataSeeder provides the restaurant roles; create one if this test profile has none.
        var roles = testRoleRepository;
        if(roles.findByName(role).isEmpty()) {var r = new com.group06.restaurantevent.users.entity.Role();r.setName(role);roles.save(r);}
        String response=mvc.perform(post("/api/admin/staff/shifts").with(as(9,"ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("shiftDate",date,"startTime","10:00","endTime","18:00","roleRequired",role,"requiredStaffCount",2))))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id=mapper.readTree(response).get("id").asLong();
        mvc.perform(get("/api/admin/staff/shifts").param("date",date).with(as(9,"ADMIN"))).andExpect(status().isOk());
        mvc.perform(put("/api/admin/staff/shifts/"+id).with(as(9,"ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("shiftDate",date,"startTime","11:00","endTime","19:00","roleRequired",role,"requiredStaffCount",3))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.requiredStaffCount").value(3));
        mvc.perform(delete("/api/admin/staff/shifts/"+id).with(as(9,"ADMIN"))).andExpect(status().isNoContent());
    }
    @Autowired com.group06.restaurantevent.staff.service.StaffService staffTestService;
    @Autowired com.group06.restaurantevent.users.repository.RoleRepository testRoleRepository;
}
