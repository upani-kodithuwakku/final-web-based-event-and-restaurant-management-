package com.group06.restaurantevent.billing.service;

import com.group06.restaurantevent.billing.dto.request.CreateInvoiceRequest;
import com.group06.restaurantevent.billing.dto.request.CreatePaymentRequest;
import com.group06.restaurantevent.billing.dto.response.InvoiceResponse;
import com.group06.restaurantevent.billing.dto.response.PaymentResponse;
import com.group06.restaurantevent.billing.entity.Invoice;
import com.group06.restaurantevent.billing.entity.InvoiceItem;
import com.group06.restaurantevent.billing.entity.Payment;
import com.group06.restaurantevent.billing.repository.InvoiceRepository;
import com.group06.restaurantevent.billing.repository.PaymentRepository;
import com.group06.restaurantevent.common.enums.InvoiceStatus;
import com.group06.restaurantevent.common.enums.InvoiceType;
import com.group06.restaurantevent.common.enums.PaymentMethod;
import com.group06.restaurantevent.common.enums.PaymentStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BillingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final BigDecimal SERVICE_RATE = new BigDecimal("0.10");

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest req) {
        InvoiceType type = parseType(req.getInvoiceType());

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .customerId(req.getCustomerId())
                .foodOrderId(req.getFoodOrderId())
                .eventBookingId(req.getEventBookingId())
                .invoiceType(type)
                .subtotal(BigDecimal.ZERO)
                .serviceCharge(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.ISSUED)
                .issuedAt(LocalDateTime.now())
                .build();

        invoiceRepository.save(invoice);
        return toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse addItemAndRecalculate(Long invoiceId, String description,
                                                  int qty, BigDecimal unitPrice) {
        Invoice invoice = findInvoice(invoiceId);
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

        InvoiceItem item = InvoiceItem.builder()
                .invoice(invoice)
                .description(description)
                .quantity(qty)
                .unitPrice(unitPrice)
                .lineTotal(lineTotal)
                .build();
        invoice.getItems().add(item);

        BigDecimal subtotal = invoice.getItems().stream()
                .map(InvoiceItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal svc = subtotal.multiply(SERVICE_RATE).setScale(2, RoundingMode.HALF_UP);

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(tax);
        invoice.setServiceCharge(svc);
        invoice.setTotalAmount(subtotal.add(tax).add(svc).subtract(invoice.getDiscountAmount()));

        return toResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse getInvoice(Long id) {
        return toResponse(findInvoice(id));
    }

    public List<InvoiceResponse> myInvoices(Long customerId) {
        return invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).toList();
    }

    public List<InvoiceResponse> allInvoices() {
        return invoiceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentResponse processPayment(CreatePaymentRequest req) {
        Invoice invoice = findInvoice(req.getInvoiceId());
        if (invoice.getStatus() == InvoiceStatus.PAID)
            throw new BadRequestException("Invoice is already paid");

        PaymentMethod method = parseMethod(req.getMethod());

        Payment payment = Payment.builder()
                .paymentReference(generatePaymentRef())
                .invoice(invoice)
                .amount(invoice.getTotalAmount())
                .method(method)
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo")))
                .gatewayReference("SIM-" + System.currentTimeMillis())
                .build();

        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
        return toPaymentResponse(paymentRepository.save(payment));
    }

    public List<PaymentResponse> getPaymentsForInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId)
                .stream().map(this::toPaymentResponse).toList();
    }

    private Invoice findInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    private String generateInvoiceNumber() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "INV-" + ts + "-" + String.format("%04X", new Random().nextInt(0xFFFF));
    }

    private String generatePaymentRef() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PAY-" + ts + "-" + String.format("%04X", new Random().nextInt(0xFFFF));
    }

    private InvoiceType parseType(String s) {
        try { return InvoiceType.valueOf(s.toUpperCase()); }
        catch (Exception e) { throw new BadRequestException("Invalid invoice type: " + s); }
    }

    private PaymentMethod parseMethod(String s) {
        try { return PaymentMethod.valueOf(s.toUpperCase()); }
        catch (Exception e) { throw new BadRequestException("Invalid payment method: " + s); }
    }

    private InvoiceResponse toResponse(Invoice inv) {
        List<InvoiceResponse.InvoiceItemResponse> items = inv.getItems().stream()
                .map(i -> InvoiceResponse.InvoiceItemResponse.builder()
                        .id(i.getId()).description(i.getDescription())
                        .quantity(i.getQuantity()).unitPrice(i.getUnitPrice())
                        .lineTotal(i.getLineTotal()).build())
                .toList();
        return InvoiceResponse.builder()
                .id(inv.getId()).invoiceNumber(inv.getInvoiceNumber())
                .customerId(inv.getCustomerId())
                .foodOrderId(inv.getFoodOrderId()).eventBookingId(inv.getEventBookingId())
                .invoiceType(inv.getInvoiceType().name())
                .subtotal(inv.getSubtotal()).serviceCharge(inv.getServiceCharge())
                .taxAmount(inv.getTaxAmount()).discountAmount(inv.getDiscountAmount())
                .totalAmount(inv.getTotalAmount()).status(inv.getStatus().name())
                .issuedAt(inv.getIssuedAt()).createdAt(inv.getCreatedAt())
                .items(items).build();
    }

    private PaymentResponse toPaymentResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId()).paymentReference(p.getPaymentReference())
                .invoiceId(p.getInvoice().getId()).amount(p.getAmount())
                .method(p.getMethod().name()).status(p.getStatus().name())
                .paidAt(p.getPaidAt()).createdAt(p.getCreatedAt()).build();
    }
}
