package com.example.plantswap.repository;

import com.example.plantswap.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
