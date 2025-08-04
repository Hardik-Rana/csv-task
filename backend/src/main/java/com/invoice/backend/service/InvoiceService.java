package com.invoice.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.invoice.backend.model.Invoice;
import com.invoice.backend.repository.InvoiceRepository;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice saveInvoice(Invoice invoice) {
        boolean exists = invoiceRepository.existsByCustomerIdAndInvoiceNumber(
                invoice.getCustomerId(), invoice.getInvoiceNumber());
        if (!exists) {
            return invoiceRepository.save(invoice);
        }
        return null;
    }
}