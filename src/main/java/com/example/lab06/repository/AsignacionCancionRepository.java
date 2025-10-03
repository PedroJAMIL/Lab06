package com.example.lab06.repository;

import com.example.lab06.entity.AsignacionCancion;
import com.example.lab06.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionCancionRepository extends JpaRepository<AsignacionCancion, Long> {
    Optional<AsignacionCancion> findByUsuario(Usuario usuario);
    List<AsignacionCancion> findBySolicitudPendienteTrue();
    List<AsignacionCancion> findAllByOrderByFechaAsignacionDesc();
    
    List<AsignacionCancion> findByJuegoCompletadoTrueOrderByIntentosRealizadosAsc();
    Long countByJuegoCompletadoTrue();
}