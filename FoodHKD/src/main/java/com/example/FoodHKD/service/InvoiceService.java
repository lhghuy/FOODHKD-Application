package com.example.FoodHKD.service;

import java.util.List;

import com.example.FoodHKD.model.Invoice; // Ensure this is the correct package for the Invoice class
import com.example.FoodHKD.model.Order; // Ensure this is the correct package for the Order class

public interface InvoiceService {
    List<Invoice> getAllInvoices();
    Invoice getInvoiceById(Integer id);
    Invoice createInvoice(Invoice invoice);
    Invoice getInvoiceByOrder(Order order);
}
