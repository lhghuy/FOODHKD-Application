package com.example.FoodHKD.service;

import java.time.LocalDate;

import com.example.FoodHKD.model.ReportDTO;

public interface ReportService {
    ReportDTO getReport(LocalDate dateFrom, LocalDate dateTo);
} 