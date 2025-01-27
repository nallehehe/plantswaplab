package com.example.plantswap.repository;

import com.example.plantswap.models.Plant;
import com.example.plantswap.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    long countByUser(User user);
}
