package com.example.plantswap.repository;

import com.example.plantswap.enumHolder.Status;
import com.example.plantswap.models.Plant;
import com.example.plantswap.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    long countByUserAndStatusNot(User user, Status status);

    //List<Plant> findPlantByItemStatus(ItemStatus itemStatus);

    List<Plant> findPlantByStatus(Status status);
}
