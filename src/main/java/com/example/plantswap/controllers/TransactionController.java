package com.example.plantswap.controllers;

import com.example.plantswap.enums.ItemStatus;
import com.example.plantswap.enums.Status;
import com.example.plantswap.models.Plant;
import com.example.plantswap.models.Transaction;
import com.example.plantswap.models.User;
import com.example.plantswap.repository.PlantRepository;
import com.example.plantswap.repository.TransactionRepository;
import com.example.plantswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PlantRepository plantRepository;

    //post method to create a sale transaction
    @PostMapping("/sale")
    public ResponseEntity<Transaction> createSellTransaction(@RequestBody Transaction transaction) {

        if(transaction.getPlant() != null && !plantRepository.existsById(transaction.getPlant().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant not found");
        }


        if(transaction.getPlantTrade() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot add a plant trade in a sale transaction.");
        }

        Plant plant = plantRepository.findById(transaction.getPlant().getId()).get();

        User user = userRepository.findById(transaction.getUser().getId()).get();

        //if the plant has the itemstatus trade you cannot purchase it
        if(plant.getItemStatus() == ItemStatus.TRADE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That plant is not for sale.");
        }

        //if the plant is not available you cannot purchase it
        if(plant.getStatus() != Status.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That plant is not available.");
        }

        // https://stackoverflow.com/questions/1514910/how-can-i-properly-compare-two-integers-in-java
        //if the plant price and transaction totalamount does not match you cannot purchase it
        if (!plant.getPrice().equals(transaction.getTotalcost())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The total cost does not match the price.");
        }

        //if you are set as the plant ad owner you cannot purchase it
        if (transaction.getUser().getId() == plant.getUser().getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are the owner of this ad.");
        }

        transaction.setStatus(Status.BOUGHT);

        Transaction savedTransaction = transactionRepository.save(transaction);

        //if the sale transaction is successful the ad gets set as sold and the user who
        // bought it gets set as the owner id
        plant.setStatus(Status.SOLD);
        plant.setUser(user);
        plantRepository.save(plant);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
    }

    //post method to create a trade transaction
    @PostMapping("/trade")
    public ResponseEntity<Transaction> createTradeTransaction(@RequestBody Transaction transaction) {
        if(transaction.getPlant() != null && !plantRepository.existsById(transaction.getPlant().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant not found");
        }

        Plant plant = plantRepository.findById(transaction.getPlant().getId()).get();
        Plant plant_trading = plantRepository.findById(transaction.getPlantTrade().getId()).get();

        if(plant.getStatus() != Status.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That plant is not available.");
        }

        if(plant.getItemStatus() == ItemStatus.SALE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That plant is not for up for trade.");
        }

        if (transaction.getUser().getId() == plant.getUser().getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are the owner of this ad.");
        }

        if(transaction.getTotalcost() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This plant is up for trade, you cannot pay.");
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        //if the trade goes through the transaction gets set as pending
        //and the plant gets set as reserved awaiting the buyer and sellers response
        transaction.setStatus(Status.PENDING);
        transactionRepository.save(savedTransaction);

        plant.setStatus(Status.RESERVED);
        plant_trading.setStatus(Status.RESERVED);
        plantRepository.save(plant);
        plantRepository.save(plant_trading);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
    }

    //gets all transactions that are pending and waiting for a response
    @GetMapping("/pending")
    public ResponseEntity<List<Transaction>> getPendingTransactions() {
        List<Transaction> transactions = transactionRepository.findPlantByStatus(Status.PENDING);
        return ResponseEntity.ok(transactions);
    }

    //https://stackoverflow.com/questions/23042944/architectural-rest-how-do-i-design-a-rest-api-for-requestapproval-2-resources
   //update method for pending transactions
    @PutMapping("/pending/{id}")
    public ResponseEntity<Transaction> updatePendingTransaction(@PathVariable long id, @RequestBody Transaction transaction) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found."));

        Plant existingPlant = plantRepository.findById(existingTransaction.getPlant().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found."));

        Plant existingTradePlant = plantRepository.findById(existingTransaction.getPlantTrade().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found."));

        if (transaction.getBuyerStatus() != null) {
            existingTransaction.setBuyerStatus(transaction.getBuyerStatus());
        }

        if(transaction.getSellerStatus() != null) {
            existingTransaction.setSellerStatus(transaction.getSellerStatus());
        }

        //if either the buyer or seller rejects the trade the transaction gets cancelled and
        //the plant gets set as available again
        if (transaction.getSellerStatus() == Status.REJECTED || transaction.getBuyerStatus() == Status.REJECTED) {
            existingTransaction.setStatus(Status.CANCELLED);
            existingPlant.setStatus(Status.AVAILABLE);
            existingTradePlant.setStatus(Status.AVAILABLE);
        }

        //if both accept the transaction and plant gets set as traded
        else if (transaction.getSellerStatus() == Status.ACCEPTED && transaction.getBuyerStatus() == Status.ACCEPTED) {
            existingTransaction.setStatus(Status.TRADED);
            existingPlant.setStatus(Status.TRADED);
            existingTradePlant.setStatus(Status.TRADED);

            //holds one of the plant owners ids so it updates the new owners id properly
            User userHolder = existingPlant.getUser();

            existingPlant.setUser(existingTradePlant.getUser());
            existingTradePlant.setUser(userHolder);
        }

        Transaction updatedTranscation = transactionRepository.save(existingTransaction);

        plantRepository.save(existingPlant);
        plantRepository.save(existingTradePlant);

        return ResponseEntity.ok(updatedTranscation);
    }

    //gets all transactions
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    //gets a specific transaction
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        return ResponseEntity.ok(transaction);
    }

    //update a transaction
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found."));

        if (transaction.getPlant() != null) {
            existingTransaction.setPlant(transaction.getPlant());
        }

        if (transaction.getPlant() != null) {
            Plant plant = plantRepository.findById(transaction.getPlant().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant not found"));

            existingTransaction.setPlant(plant);
        }

        Transaction updatedTranscation = transactionRepository.save(existingTransaction);
        return ResponseEntity.ok(updatedTranscation);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Transaction> deleteTransaction(@PathVariable Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        }

        transactionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
