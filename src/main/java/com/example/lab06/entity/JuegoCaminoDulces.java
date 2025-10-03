package com.example.lab06.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "juego_camino_dulces")
public class JuegoCaminoDulces {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "posicion_casa_dulces", nullable = false)
    private Integer posicionCasaDulces;
    
    @Column(name = "posicion_actual", nullable = false)
    private Integer posicionActual = 1;
    
    @Column(name = "intentos_realizados", nullable = false)
    private Integer intentosRealizados = 0;
    
    @Column(name = "juego_completado", nullable = false)
    private Boolean juegoCompletado = false;
    
    @Column(name = "juego_habilitado", nullable = false)
    private Boolean juegoHabilitado = true;
    
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;
    
    @Column(name = "fecha_completado")
    private LocalDateTime fechaCompletado;
    
    public JuegoCaminoDulces() {
        this.fechaAsignacion = LocalDateTime.now();
    }
    
    public JuegoCaminoDulces(Usuario usuario, Integer posicionCasaDulces) {
        this.usuario = usuario;
        this.posicionCasaDulces = posicionCasaDulces;
        this.fechaAsignacion = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Integer getPosicionCasaDulces() {
        return posicionCasaDulces;
    }
    
    public void setPosicionCasaDulces(Integer posicionCasaDulces) {
        this.posicionCasaDulces = posicionCasaDulces;
    }
    
    public Integer getPosicionActual() {
        return posicionActual;
    }
    
    public void setPosicionActual(Integer posicionActual) {
        this.posicionActual = posicionActual;
    }
    
    public Integer getIntentosRealizados() {
        return intentosRealizados;
    }
    
    public void setIntentosRealizados(Integer intentosRealizados) {
        this.intentosRealizados = intentosRealizados;
    }
    
    public Boolean getJuegoCompletado() {
        return juegoCompletado;
    }
    
    public void setJuegoCompletado(Boolean juegoCompletado) {
        this.juegoCompletado = juegoCompletado;
        if (juegoCompletado && this.fechaCompletado == null) {
            this.fechaCompletado = LocalDateTime.now();
        }
    }
    
    public Boolean getJuegoHabilitado() {
        return juegoHabilitado;
    }
    
    public void setJuegoHabilitado(Boolean juegoHabilitado) {
        this.juegoHabilitado = juegoHabilitado;
    }
    
    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }
    
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
    
    public LocalDateTime getFechaCompletado() {
        return fechaCompletado;
    }
    
    public void setFechaCompletado(LocalDateTime fechaCompletado) {
        this.fechaCompletado = fechaCompletado;
    }
}