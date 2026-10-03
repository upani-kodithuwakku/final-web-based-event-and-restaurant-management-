# Entities — Menu and Orders

## MenuCategory
Package: `com.group06.restaurantevent.menu.entity`
Fields: id, name, description, displayOrder, isActive

## MenuItem
Package: `com.group06.restaurantevent.menu.entity`
Fields: id, category (ManyToOne MenuCategory), name, description, price, imageUrl, preparationMinutes, isAvailable, isActive

## FoodOrder
Package: `com.group06.restaurantevent.orders.entity`
Fields: id, orderReference, customer (ManyToOne User), table (ManyToOne RestaurantTable, nullable), reservation (ManyToOne TableReservation, nullable), orderType, status, specialNote, subtotal, createdAt, updatedAt, items (OneToMany FoodOrderItem)

## FoodOrderItem
Package: `com.group06.restaurantevent.orders.entity`
Fields: id, order (ManyToOne FoodOrder), menuItem (ManyToOne MenuItem), itemNameSnapshot, unitPriceSnapshot, quantity, specialNote, lineTotal

## MenuItemIngredient
Package: `com.group06.restaurantevent.menu.entity` (or inventory)
Fields: id, menuItem (ManyToOne MenuItem), inventoryItem (ManyToOne InventoryItem), quantityRequired
