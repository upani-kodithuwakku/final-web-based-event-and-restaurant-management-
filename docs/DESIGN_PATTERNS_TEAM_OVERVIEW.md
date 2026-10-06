# Design Patterns Used in Gather

A beginner-friendly overview for our project team.

## What is a design pattern?

A design pattern is a reusable approach to solving a common software design problem. It describes how classes work together. It is not a library or a feature that users install.

Our project uses two patterns from the lectures:

| Pattern | Where we use it | Purpose |
| --- | --- | --- |
| Strategy | Customer payments for table reservations, event bookings and food orders | Choose between different payment behaviours |
| Observer | Table reservations | Notify other components when a reservation changes |

## 1. Strategy Pattern: Payments

### Why did we use it?

Customers can choose **Card payment** or **Pay at the outlet**. These methods handle payment differently:

- **Card:** validate card details and mark a successful simulated payment as **PAID**.
- **Outlet:** mark the payment as **PENDING** until staff collect the money and mark it paid.

Putting both behaviours inside one large payment method would mix different rules together. Adding more payment methods would make that method harder to maintain.

Strategy separates each payment behaviour into its own class. Both classes follow the same interface, so the payment service can use whichever method the customer chooses.

### Which classes are involved?

All paths below are under `restaurant-event-backend/src/main/java/com/group06/restaurantevent/`.

| Lecture role | Our class | What it does |
| --- | --- | --- |
| Context | `payment/service/CustomerPaymentService.java` | Selects a strategy and delegates payment handling |
| Strategy interface | `payment/strategy/PaymentMethodStrategy.java` | Defines the common `apply()` operation |
| Concrete strategy | `payment/strategy/CardPaymentStrategy.java` | Handles simulated card payment |
| Concrete strategy | `payment/strategy/PayAtOutletPaymentStrategy.java` | Handles payment at the restaurant |

### How does it work?

```text
Customer chooses a payment method
                 ↓
CustomerPaymentService selects the strategy
                 ↓
CardPaymentStrategy OR PayAtOutletPaymentStrategy
                 ↓
The service saves the resulting payment
```

The service delegates using this line:

```java
strategyFor(req.getMethod()).apply(payment, req.getCard());
```

`apply()` has the same name in both strategies, but each implementation performs its own behaviour.

**Benefits:** payment rules are separated, easier to test and easier to extend. A future payment method can have its own strategy instead of adding its settlement logic to the main service. Its method configuration and checkout UI may also need updates.

**Tradeoff:** we manage a few extra classes and an interface.

**Simple demo:** use Card for one booking and show **PAID**. Use Pay at the outlet for another booking and show **PENDING**. The card gateway is simulated; no real money is charged.

**One sentence to remember:**

> Strategy lets us choose how a payment is handled without putting every payment method's rules in the main payment service.

## 2. Observer Pattern: Table Reservations

When a reservation is created or changed, two components need to react:

1. The customer notification component creates an in-app message.
2. The audit component records the reservation change and the acting user.

`ReservationService` sends the change to `ReservationEventPublisher`. The publisher notifies `CustomerNotificationObserver` and `ReservationAuditObserver`, which implement the common `ReservationObserver` interface.

These classes are in the `reservations/observer/` folder. The publisher implements `ReservationSubject`, with the lecture's `addObserver()`, `removeObserver()` and `notifyObservers()` operations.

**Why use it?** It separates notification and history reactions from reservation business rules. Another reaction can be added as an observer without putting its implementation inside the reservation service.

**One sentence to remember:**

> Observer lets multiple components react to the same reservation change.

## The difference between the two

**Strategy chooses one behaviour:** use the selected payment method.

**Observer informs multiple subscribers:** notify both components about a reservation change.

We chose these patterns because they solve actual problems in our system and match the lecture examples. The important presentation evidence is the working behaviour, the class structure and our understanding—not simply the number of patterns used.
