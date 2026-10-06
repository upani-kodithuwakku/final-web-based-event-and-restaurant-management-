package com.group06.restaurantevent.reports.service;

import com.group06.restaurantevent.common.enums.EventBookingStatus;
import com.group06.restaurantevent.common.enums.OrderStatus;
import com.group06.restaurantevent.common.enums.ReservationStatus;
import com.group06.restaurantevent.common.enums.TableStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.events.entity.EventBooking;
import com.group06.restaurantevent.events.repository.EventBookingRepository;
import com.group06.restaurantevent.inventory.repository.InventoryItemRepository;
import com.group06.restaurantevent.orders.entity.FoodOrder;
import com.group06.restaurantevent.orders.entity.FoodOrderItem;
import com.group06.restaurantevent.orders.repository.FoodOrderRepository;
import com.group06.restaurantevent.reports.dto.response.*;
import com.group06.restaurantevent.reservations.entity.RestaurantTable;
import com.group06.restaurantevent.reservations.entity.TableReservation;
import com.group06.restaurantevent.reservations.repository.RestaurantTableRepository;
import com.group06.restaurantevent.reservations.repository.TableReservationRepository;
import com.group06.restaurantevent.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Read-only reports for admins and managers. Each report reads other modules' repositories only. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final long MAX_RANGE_DAYS = 366;
    private static final List<ReservationStatus> ACTIVE_RESERVATION =
            List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);
    private static final List<OrderStatus> ACTIVE_ORDER =
            List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY);

    private final TableReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final FoodOrderRepository orderRepository;
    private final EventBookingRepository bookingRepository;
    private final InventoryItemRepository inventoryRepository;
    private final UserRepository userRepository;

    public DashboardReportResponse dashboard() {
        LocalDate today = LocalDate.now();
        List<RestaurantTable> tables = tableRepository.findAll();
        Map<TableStatus, Long> tablesByStatus = tables.stream()
                .collect(Collectors.groupingBy(RestaurantTable::getCurrentStatus, Collectors.counting()));
        long customers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> "CUSTOMER".equals(r.getName()))).count();

        return DashboardReportResponse.builder()
                .date(today.toString())
                .todayReservations(reservationRepository.findByReservationDateAndStatusIn(today, ACTIVE_RESERVATION).size())
                .totalTables(tables.size())
                .availableTables(tablesByStatus.getOrDefault(TableStatus.AVAILABLE, 0L))
                .occupiedTables(tablesByStatus.getOrDefault(TableStatus.OCCUPIED, 0L))
                .reservedTables(tablesByStatus.getOrDefault(TableStatus.RESERVED, 0L))
                .outOfServiceTables(tablesByStatus.getOrDefault(TableStatus.OUT_OF_SERVICE, 0L))
                .totalCustomers(customers)
                .activeFoodOrders(orderRepository.findByStatusInOrderByCreatedAtAsc(ACTIVE_ORDER).size())
                .pendingEventBookings(bookingRepository.findByStatusOrderByEventDateAsc(EventBookingStatus.PENDING).size())
                .lowStockItems(inventoryRepository.findLowStock().size())
                .build();
    }

    public ReservationReportResponse reservations(LocalDate from, LocalDate to) {
        checkRange(from, to);
        List<TableReservation> list = reservationRepository.findAll().stream()
                .filter(r -> !r.getReservationDate().isBefore(from) && !r.getReservationDate().isAfter(to))
                .toList();

        Map<String, Long> byStatus = countByEnum(ReservationStatus.values(), list, TableReservation::getStatus);
        long completed = byStatus.get(ReservationStatus.COMPLETED.name());
        long noShow = byStatus.get(ReservationStatus.NO_SHOW.name());
        Map.Entry<LocalDate, Long> busiest = list.stream()
                .collect(Collectors.groupingBy(TableReservation::getReservationDate, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);

        return ReservationReportResponse.builder()
                .from(from).to(to)
                .totalReservations(list.size())
                .totalGuests(list.stream().mapToLong(TableReservation::getGuestCount).sum())
                .byStatus(byStatus)
                .noShowRate(percent(noShow, completed + noShow))
                .busiestDay(busiest == null ? null : busiest.getKey())
                .busiestDayReservations(busiest == null ? 0 : busiest.getValue())
                .build();
    }

    public SalesReportResponse sales(LocalDate from, LocalDate to) {
        checkRange(from, to);
        List<FoodOrder> inRange = orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> { LocalDate d = o.getCreatedAt().toLocalDate(); return !d.isBefore(from) && !d.isAfter(to); })
                .toList();
        List<FoodOrder> sold = inRange.stream().filter(o -> o.getStatus() != OrderStatus.CANCELLED).toList();
        BigDecimal revenue = sold.stream().map(FoodOrder::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, List<FoodOrderItem>> byItem = sold.stream().flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(FoodOrderItem::getItemNameSnapshot));
        List<SalesReportResponse.TopItem> top = byItem.entrySet().stream()
                .map(e -> SalesReportResponse.TopItem.builder()
                        .name(e.getKey())
                        .quantity(e.getValue().stream().mapToLong(FoodOrderItem::getQuantity).sum())
                        .revenue(e.getValue().stream().map(FoodOrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add))
                        .build())
                .sorted(Comparator.comparingLong(SalesReportResponse.TopItem::getQuantity).reversed()
                        .thenComparing(SalesReportResponse.TopItem::getName))
                .limit(5).toList();

        return SalesReportResponse.builder()
                .from(from).to(to)
                .orderCount(sold.size())
                .cancelledOrders(inRange.size() - sold.size())
                .foodRevenue(revenue)
                .averageOrderValue(sold.isEmpty() ? BigDecimal.ZERO.setScale(2)
                        : revenue.divide(BigDecimal.valueOf(sold.size()), 2, RoundingMode.HALF_UP))
                .topItems(top)
                .build();
    }

    public InventoryReportResponse inventory() {
        var low = inventoryRepository.findLowStock().stream()
                .map(i -> InventoryReportResponse.LowStockItem.builder()
                        .id(i.getId()).name(i.getName()).unit(i.getUnit())
                        .currentQuantity(i.getCurrentQuantity()).reorderLevel(i.getReorderLevel()).build())
                .sorted(Comparator.comparing(InventoryReportResponse.LowStockItem::getName))
                .toList();
        return InventoryReportResponse.builder()
                .activeItems(inventoryRepository.findByIsActiveTrueOrderByNameAsc().size())
                .lowStockCount(low.size())
                .lowStockItems(low)
                .build();
    }

    public EventReportResponse events(LocalDate from, LocalDate to) {
        checkRange(from, to);
        List<EventBooking> list = bookingRepository.findAllByOrderByEventDateDesc().stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().toLocalDate().isBefore(from) && !b.getCreatedAt().toLocalDate().isAfter(to))
                .toList();
        List<EventBooking> confirmed = list.stream().filter(b -> b.getStatus() == EventBookingStatus.CONFIRMED).toList();

        return EventReportResponse.builder()
                .from(from).to(to)
                .totalBookings(list.size())
                .byStatus(countByEnum(EventBookingStatus.values(), list, EventBooking::getStatus))
                .confirmedGuests(confirmed.stream().mapToLong(EventBooking::getGuestCount).sum())
                .confirmedValue(confirmed.stream().map(b -> b.getEventPackage().getBasePrice())
                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────

    private void checkRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) throw new BadRequestException("'from' date must be on or before 'to' date");
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS)
            throw new BadRequestException("Date range cannot be longer than " + MAX_RANGE_DAYS + " days");
    }

    private static <E extends Enum<E>, T> Map<String, Long> countByEnum(E[] values, List<T> items, Function<T, E> status) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (E v : values) counts.put(v.name(), 0L);
        items.forEach(i -> counts.merge(status.apply(i).name(), 1L, Long::sum));
        return counts;
    }

    private static double percent(long part, long whole) {
        return whole == 0 ? 0 : BigDecimal.valueOf(part * 100.0 / whole).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
