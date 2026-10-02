# Status Enums — Menu and Orders

## OrderStatus
Located in: `com.group06.restaurantevent.common.enums.OrderStatus`
Values:
- PENDING — order placed, awaiting kitchen
- PREPARING — kitchen is cooking
- READY — ready to serve
- SERVED — delivered to customer
- CANCELLED — order cancelled before preparation

## OrderType (VARCHAR in DB)
- DINE_IN
- TAKEAWAY

Transitions are forward-only: PENDING -> PREPARING -> READY -> SERVED.
KITCHEN_STAFF can move status forward. Cancellation requires ADMIN or MANAGER.
