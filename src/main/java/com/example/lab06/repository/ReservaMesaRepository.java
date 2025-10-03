package com.example.lab06.repository;

import com.example.lab06.entity.Mesa;
import com.example.lab06.entity.ReservaMesa;
import com.example.lab06.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaMesaRepository extends JpaRepository<ReservaMesa, Long> {
    
    Optional<ReservaMesa> findByUsuario(Usuario usuario);
    
    List<ReservaMesa> findByMesaOrderByFechaAsc(Mesa mesa);
    
    List<ReservaMesa> findByOrderByFechaDesc();
    
    @Query("SELECT COUNT(r) FROM ReservaMesa r")
    Long countReservas();
    
    boolean existsByUsuario(Usuario usuario);
}