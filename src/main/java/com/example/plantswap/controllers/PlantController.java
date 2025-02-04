package com.example.plantswap.controllers;

import com.example.plantswap.enums.ItemStatus;
import com.example.plantswap.enums.Status;
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

    private int maxPlants = 10;
    private int minimumPrice = 50;

    //post method to create plant
    @PostMapping
    public ResponseEntity<Plant> createPlant(@RequestBody Plant plant) {

        if(plant.getUser() != null && !userRepository.existsById(plant.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }

        //https://stackoverflow.com/questions/10696490/does-spring-data-jpa-have-any-way-to-count-entites-using-method-name-resolving
        //https://docs.spring.io/spring-data/jpa/docs/1.6.4.RELEASE/reference/html/repositories.html

        //counts each plant owned by users but not the plants with the status sold or traded
        long userPlantAds = plantRepository.countByUserAndStatusNotAndStatusNot(plant.getUser(), Status.SOLD, Status.TRADED);

        //user cannot have more than 10 available plant ads out
        if (userPlantAds >= maxPlants) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user cannot advertise more than " + maxPlants + " plants");
        }

        //if the plant is getting put up for sale the price cannot be less than the minimum assigned price (50)
        if (plant.getItemStatus() == ItemStatus.SALE && plant.getPrice() < minimumPrice) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The price cannot be less than " + minimumPrice);
        }
        //if the plant is getting put up for trade you cannot assign a price to it
        else if (plant.getItemStatus() == ItemStatus.TRADE && plant.getPrice() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This item is up for trade, it cannot have a price.");
        }

        //you can't add a plant as traded or sold when you're putting up an add
        if (plant.getStatus() == Status.TRADED || plant.getStatus() == Status.SOLD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't add a plant as traded or sold.");
        }

        Plant savedPlant = plantRepository.save(plant);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedPlant);
    }

    //get all plants
    @GetMapping
    public ResponseEntity<List<Plant>> getAllPlants() {
        List<Plant> plants = plantRepository.findAll();
        return ResponseEntity.ok(plants);
    }

    //get all available plants
    @GetMapping("/available")
    public ResponseEntity<List<Plant>> getAllAvailablePlants() {
        List<Plant> plants = plantRepository.findPlantByStatus(Status.AVAILABLE);
        return ResponseEntity.ok(plants);
    }

    //get specific plant by id
    @GetMapping("/{id}")
    public ResponseEntity<Plant> getPlantById(@PathVariable Long id) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));
        return ResponseEntity.ok(plant);
    }

    //update specific plant by id
    @PutMapping("/{id}")
    public ResponseEntity<Plant> updatePlant(@PathVariable Long id, @RequestBody Plant plant) {
        Plant existingPlant = plantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found."));

        long userPlantAds = plantRepository.countByUserAndStatusNotAndStatusNot(plant.getUser(), Status.SOLD, Status.TRADED);

        if (plant.getStatus() == Status.TRADED || plant.getStatus() == Status.SOLD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't add a plant as traded or sold.");
        }

        if (userPlantAds >= maxPlants) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user cannot advertise more than " + maxPlants + " plants");
        }

        if (plant.getItemStatus() == ItemStatus.SALE && plant.getPrice() < minimumPrice) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The price cannot be less than " + minimumPrice);
        }

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

        if (plant.getPrice() != null) {
            existingPlant.setPrice(plant.getPrice());
        }

        if (plant.getUser() != null) {
            User user = userRepository.findById(plant.getUser().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
            existingPlant.setUser(user);
        }

        Plant updatedPlant = plantRepository.save(existingPlant);
        return ResponseEntity.ok(updatedPlant);
    }

    //delete a plant by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Plant> deletePlant(@PathVariable Long id) {
        if (!plantRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found");
        }

        plantRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
