package com.example.lab06.repository;

import com.example.lab06.entity.JuegoCaminoDulces;
import com.example.lab06.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JuegoCaminoDulcesRepository extends JpaRepository<JuegoCaminoDulces, Long> {
    
    Optional<JuegoCaminoDulces> findByUsuario(Usuario usuario);
    
    List<JuegoCaminoDulces> findByJuegoCompletadoTrueOrderByIntentosRealizadosAsc();
    
    List<JuegoCaminoDulces> findByJuegoHabilitadoTrue();
    
    List<JuegoCaminoDulces> findByJuegoHabilitadoFalse();
    
    Long countByJuegoCompletadoTrue();
    
    Long countByJuegoHabilitadoTrue();
}