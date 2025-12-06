package com.example.FoodHKD.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.Invoice;
import com.example.FoodHKD.model.Order;
import com.example.FoodHKD.service.InvoiceService;

@RestController
@RequestMapping("/client/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping
    public Invoice createInvoice(@RequestBody Invoice invoice) {
        return invoiceService.createInvoice(invoice);
    }

    @GetMapping("/{orderId}")
    public Invoice getInvoiceByOrder(@PathVariable Integer orderId) {
        Order order = new Order();
order.setOrderID(orderId);
return invoiceService.getInvoiceByOrder(order);

    }
}
