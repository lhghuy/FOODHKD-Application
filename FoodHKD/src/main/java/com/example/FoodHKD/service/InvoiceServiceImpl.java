package com.example.FoodHKD.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.FoodHKD.model.Invoice;
import com.example.FoodHKD.model.Order;
import com.example.FoodHKD.repository.InvoiceRepository;


@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Override
    public Invoice getInvoiceById(Integer id) {
        return invoiceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        invoice.setIssuedAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getInvoiceByOrder(Order order) {
        return invoiceRepository.findByOrder(order)
            .orElseThrow(() -> new RuntimeException("Invoice not found for order"));
    }
}
