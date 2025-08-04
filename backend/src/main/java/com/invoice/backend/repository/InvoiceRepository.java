package com.invoice.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.invoice.backend.model.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByCustomerIdAndInvoiceNumber(String customerId, String invoiceNumber);
}