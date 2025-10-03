package com.example.lab06.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "heroes_navales")
public class HeroeNaval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(length = 255)
    private String descripcion;
    
    @Column(length = 50)
    private String pais;
    
    // Constructores
    public HeroeNaval() {}
    
    public HeroeNaval(String nombre, String descripcion, String pais) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.pais = pais;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getPais() {
        return pais;
    }
    
    public void setPais(String pais) {
        this.pais = pais;
    }
}