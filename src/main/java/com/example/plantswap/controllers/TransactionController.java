package com.example.plantswap.controllers;

import com.example.plantswap.enumHolder.ItemStatus;
import com.example.plantswap.enumHolder.Status;
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

    @PostMapping("/sale")
    public ResponseEntity<Transaction> createSellTransaction(@RequestBody Transaction transaction) {
        if(transaction.getPlant() != null && !plantRepository.existsById(transaction.getPlant().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant not found");
        }

        Plant plant = plantRepository.findById(transaction.getPlant().getId()).get();

        User user = userRepository.findById(transaction.getUser().getId()).get();

        if(plant.getItemStatus() == ItemStatus.TRADE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That plant is not for sale.");
        }

        if(plant.getStatus() == Status.SOLD || plant.getStatus() == Status.RESERVED ||
                plant.getStatus() == Status.TRADED || plant.getStatus() == Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That plant is not available.");
        }

        // https://stackoverflow.com/questions/1514910/how-can-i-properly-compare-two-integers-in-java
        if (!plant.getPrice().equals(transaction.getTotalcost())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The total cost does not match the price.");
        }

        if (transaction.getUser().getId() == plant.getUser().getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are the owner of this ad.");
        }

        /*if(plant.getPrice() != transaction.getTotalcost()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total cost does not match with the price.");
        }*/

        transaction.setStatus(Status.BOUGHT);

        Transaction savedTransaction = transactionRepository.save(transaction);

        plant.setStatus(Status.SOLD);
        plant.setUser(user);
        plantRepository.save(plant);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
    }

    @PostMapping("/trade")
    public ResponseEntity<Transaction> createTradeTransaction(@RequestBody Transaction transaction) {
        if(transaction.getPlant() != null && !plantRepository.existsById(transaction.getPlant().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant not found");
        }

        Plant plant = plantRepository.findById(transaction.getPlant().getId()).get();

        if(plant.getStatus() == Status.SOLD || plant.getStatus() == Status.RESERVED
                || plant.getStatus() == Status.TRADED || plant.getStatus() == Status.PENDING) {
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

        transaction.setStatus(Status.PENDING);
        transactionRepository.save(savedTransaction);

        plant.setStatus(Status.RESERVED);
        plantRepository.save(plant);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Transaction>> getPendingTransactions() {
        List<Transaction> transactions = transactionRepository.findPlantByStatus(Status.PENDING);
        return ResponseEntity.ok(transactions);
    }

    //https://stackoverflow.com/questions/23042944/architectural-rest-how-do-i-design-a-rest-api-for-requestapproval-2-resources
    @PutMapping("/pending/{id}")
    public ResponseEntity<Transaction> updatePendingTransaction(@PathVariable long id, @RequestBody Transaction transaction) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found."));

        Plant existingPlant = plantRepository.findById(existingTransaction.getPlant().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found."));

        User user = userRepository.findById(transaction.getUser().getId()).get();

        if (transaction.getBuyerStatus() != null) {
            existingTransaction.setBuyerStatus(transaction.getBuyerStatus());
        }

        if(transaction.getSellerStatus() != null) {
            existingTransaction.setSellerStatus(transaction.getSellerStatus());
        }

        if (transaction.getSellerStatus() == Status.REJECTED || transaction.getBuyerStatus() == Status.REJECTED) {
            existingTransaction.setStatus(Status.CANCELLED);
            existingPlant.setStatus(Status.AVAILABLE);
        }

        else if (transaction.getSellerStatus() == Status.ACCEPTED && transaction.getBuyerStatus() == Status.ACCEPTED) {
            existingTransaction.setStatus(Status.TRADED);
            existingPlant.setStatus(Status.TRADED);
            existingPlant.setUser(user);
        }

        plantRepository.save(existingPlant);

        Transaction updatedTranscation = transactionRepository.save(existingTransaction);
        return ResponseEntity.ok(updatedTranscation);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        return ResponseEntity.ok(transaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found."));

        if (transaction.getPlant() != null) {
            existingTransaction.setPlant(transaction.getPlant());
        }

        if (transaction.getPlant() != null) {
            Plant plant = plantRepository.findById(transaction.getPlant().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction  not found"));
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
