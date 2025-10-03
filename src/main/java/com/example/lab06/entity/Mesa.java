package com.example.lab06.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "mesas")
public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero", nullable = false, unique = true)
    private Integer numero;
    
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad = 4;
    
    @Column(name = "disponible", nullable = false)
    private Boolean disponible = true;
    
    @OneToMany(mappedBy = "mesa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReservaMesa> reservas;
    
    public Mesa() {}
    
    public Mesa(Integer numero) {
        this.numero = numero;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getNumero() {
        return numero;
    }
    
    public void setNumero(Integer numero) {
        this.numero = numero;
    }
    
    public Integer getCapacidad() {
        return capacidad;
    }
    
    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }
    
    public Boolean getDisponible() {
        return disponible;
    }
    
    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
    
    public List<ReservaMesa> getReservas() {
        return reservas;
    }
    
    public void setReservas(List<ReservaMesa> reservas) {
        this.reservas = reservas;
    }
    
    // Métodos de utilidad
    public long getReservasActivas() {
        if (reservas == null) return 0;
        return reservas.size();
    }
    
    public boolean puedeReservar() {
        return disponible && getReservasActivas() < capacidad;
    }
    
    public long getAsientosDisponibles() {
        return capacidad - getReservasActivas();
    }
}