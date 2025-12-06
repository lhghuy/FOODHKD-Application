package com.example.FoodHKD.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.service.TableService;

@RestController
@RequestMapping("/admin/tables")
public class TableAdminController {

    @Autowired
    private TableService tableService;

    @GetMapping
    public String viewTables(Model model) {
        return "tablesAdmin";
    }

    @GetMapping("/{tableId}")
    public String viewTableDetails(@PathVariable Integer tableId, Model model) {

        return "table_detailAdmin";

    }
}