package com.example.lab06.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "canciones_criollas")
public class CancionCriolla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String titulo;
    
    @Column(length = 100)
    private String artista;
    
    // Constructores
    public CancionCriolla() {}
    
    public CancionCriolla(String titulo, String artista) {
        this.titulo = titulo;
        this.artista = artista;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getArtista() {
        return artista;
    }
    
    public void setArtista(String artista) {
        this.artista = artista;
    }
}