package com.example.plantswap.controllers;

import com.example.plantswap.models.Plant;
import com.example.plantswap.models.User;
import com.example.plantswap.repository.PlantRepository;
import com.example.plantswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/plants")
public class PlantController {
    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Plant> createPlant(@RequestBody Plant plant) {
        Plant savedPlant = plantRepository.save(plant);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedPlant);
    }

    @GetMapping
    public ResponseEntity<List<Plant>> getAllPlants() {
        List<Plant> plants = plantRepository.findAll();
        return ResponseEntity.ok(plants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plant> getPlantById(@PathVariable Long id) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));
        return ResponseEntity.ok(plant);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Plant> updatePlant(@PathVariable Long id, @RequestBody Plant plant) {
        Plant existingPlant = plantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found."));

        if (plant.getName() != null) {
            existingPlant.setName(plant.getName());
        }

        if (plant.getAge() != null) {
            existingPlant.setAge(plant.getAge());
        }

        if (plant.getLight() != null) {
            existingPlant.setLight(plant.getLight());
        }

        if (plant.getWater() != null) {
            existingPlant.setWater(plant.getWater());
        }

        if (plant.getCareDifficulty() != null) {
            existingPlant.setCareDifficulty(plant.getCareDifficulty());
        }

        if (plant.getPlantUrl() != null) {
            existingPlant.setPlantUrl(plant.getPlantUrl());
        }

        if (plant.getItemStatus() != null) {
            existingPlant.setItemStatus(plant.getItemStatus());
        }

        if (plant.getStatus() != null) {
            existingPlant.setStatus(plant.getStatus());
        }

        if (plant.getUser() != null) {
            User user = userRepository.findById(plant.getUser().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
            existingPlant.setUser(user);
        }

        Plant updatedPlant = plantRepository.save(existingPlant);
        return ResponseEntity.ok(updatedPlant);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Plant> deletePlant(@PathVariable Long id) {
        if (!plantRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found");
        }

        plantRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
