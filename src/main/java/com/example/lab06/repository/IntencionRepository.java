package com.example.lab06.repository;

import com.example.lab06.entity.Intencion;
import com.example.lab06.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntencionRepository extends JpaRepository<Intencion, Long> {
    List<Intencion> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
    Optional<Intencion> findByUsuario(Usuario usuario);
    List<Intencion> findAllByOrderByFechaCreacionDesc();
}