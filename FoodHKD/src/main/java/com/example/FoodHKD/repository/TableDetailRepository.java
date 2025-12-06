package com.example.FoodHKD.repository;

import com.example.FoodHKD.model.TableDetail;
import com.example.FoodHKD.model.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TableDetailRepository extends JpaRepository<TableDetail, Integer> {
    List<TableDetail> findByTable(TableEntity table);
}