package com.group06.restaurantevent.inventory.service;
import com.group06.restaurantevent.inventory.dto.request.SupplierRequest;
import com.group06.restaurantevent.inventory.entity.Supplier;
import com.group06.restaurantevent.inventory.repository.SupplierRepository;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository repository;
    private final com.group06.restaurantevent.inventory.repository.PurchaseOrderRepository purchaseOrders;
    public List<Supplier> list() { return repository.findAll().stream().sorted(java.util.Comparator.comparing(Supplier::getName, String.CASE_INSENSITIVE_ORDER)).toList(); }
    public Supplier get(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier not found")); }
    @Transactional
    public Supplier save(Long id, SupplierRequest req) {
        if (repository.findAll().stream().anyMatch(s -> s.isActive() && !s.getId().equals(id)
                && (s.getName().trim().equalsIgnoreCase(req.name().trim()) || req.email().trim().equalsIgnoreCase(s.getEmail()))))
            throw new com.group06.restaurantevent.common.exception.ConflictException("An active supplier already uses this name or email");
        if (id != null && !req.active() && purchaseOrders.findAll().stream().anyMatch(p -> p.getSupplierId().equals(id) && p.getStatus().equals("PENDING")))
            throw new com.group06.restaurantevent.common.exception.ConflictException("Receive or cancel pending purchase orders before deactivating the supplier");
        Supplier supplier = id == null ? new Supplier() : get(id);
        supplier.setName(req.name().trim()); supplier.setContactPerson(req.contactPerson().trim());
        supplier.setPhone(req.phone()); supplier.setEmail(req.email().trim()); supplier.setAddress(req.address().trim());
        supplier.setSuppliedProducts(req.suppliedProducts().trim()); supplier.setJoinedDate(req.joinedDate()); supplier.setActive(req.active());
        return repository.save(supplier);
    }
    @Transactional
    public void delete(Long id) { Supplier supplier = get(id);
        if (purchaseOrders.findAll().stream().anyMatch(p -> p.getSupplierId().equals(id) && p.getStatus().equals("PENDING")))
            throw new com.group06.restaurantevent.common.exception.ConflictException("Receive or cancel this supplier's pending purchase orders first"); supplier.setActive(false); repository.save(supplier); }
}
