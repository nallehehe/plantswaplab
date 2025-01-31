package com.example.plantswap.repository;

import com.example.plantswap.enumHolder.Status;
import com.example.plantswap.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findPlantByStatus(Status status);
}
