package com.example.lab06.repository;

import com.example.lab06.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    
    Optional<Mesa> findByNumero(Integer numero);
    
    List<Mesa> findByOrderByNumeroAsc();
    
    List<Mesa> findByDisponibleTrueOrderByNumeroAsc();
    
    List<Mesa> findByDisponibleFalseOrderByNumeroAsc();
    
    @Query("SELECT COUNT(m) FROM Mesa m WHERE m.disponible = true")
    Long countMesasDisponibles();
    
    @Query("SELECT COUNT(m) FROM Mesa m WHERE m.disponible = false")
    Long countMesasOcupadas();
}