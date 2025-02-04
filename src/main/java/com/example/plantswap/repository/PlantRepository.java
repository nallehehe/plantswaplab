package com.example.plantswap.repository;

import com.example.plantswap.enums.Status;
import com.example.plantswap.models.Plant;
import com.example.plantswap.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    //query method to count all users owning plants but not plants with either two statuses
    long countByUserAndStatusNotAndStatusNot(User user, Status firstStatus, Status secondStatus);

    //List<Plant> findPlantByItemStatus(ItemStatus itemStatus);

    List<Plant> findPlantByStatus(Status status);
}
