package com.example.lab06.repository;

import com.example.lab06.entity.CancionCriolla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancionCriollaRepository extends JpaRepository<CancionCriolla, Long> {
}