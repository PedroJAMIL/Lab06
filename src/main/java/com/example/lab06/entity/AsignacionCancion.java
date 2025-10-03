package com.example.lab06.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones_canciones")
public class AsignacionCancion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "cancion_id", nullable = true) // Cambiar a nullable=true para solicitudes
    private CancionCriolla cancion;
    
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;
    
    @Column(name = "solicitud_pendiente", nullable = false)
    private Boolean solicitudPendiente = false;
    
    // Campos para el juego
    @Column(name = "intentos_realizados", nullable = false)
    private Integer intentosRealizados = 0;
    
    @Column(name = "progreso_juego", length = 1000)
    private String progresoJuego; // JSON con el progreso del juego
    
    @Column(name = "juego_completado", nullable = false)
    private Boolean juegoCompletado = false;
    
    // Constructores
    public AsignacionCancion() {
        this.fechaAsignacion = LocalDateTime.now();
    }
    
    public AsignacionCancion(Usuario usuario, CancionCriolla cancion) {
        this.usuario = usuario;
        this.cancion = cancion;
        this.fechaAsignacion = LocalDateTime.now();
        this.solicitudPendiente = false;
    }
    
    // Getters y Setters
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
    
    public CancionCriolla getCancion() {
        return cancion;
    }
    
    public void setCancion(CancionCriolla cancion) {
        this.cancion = cancion;
    }
    
    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }
    
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
    
    public Boolean getSolicitudPendiente() {
        return solicitudPendiente;
    }
    
    public void setSolicitudPendiente(Boolean solicitudPendiente) {
        this.solicitudPendiente = solicitudPendiente;
    }
    
    public Integer getIntentosRealizados() {
        return intentosRealizados;
    }
    
    public void setIntentosRealizados(Integer intentosRealizados) {
        this.intentosRealizados = intentosRealizados;
    }
    
    public String getProgresoJuego() {
        return progresoJuego;
    }
    
    public void setProgresoJuego(String progresoJuego) {
        this.progresoJuego = progresoJuego;
    }
    
    public Boolean getJuegoCompletado() {
        return juegoCompletado;
    }
    
    public void setJuegoCompletado(Boolean juegoCompletado) {
        this.juegoCompletado = juegoCompletado;
    }
}