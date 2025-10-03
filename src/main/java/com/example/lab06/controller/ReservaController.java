package com.example.lab06.controller;

import com.example.lab06.entity.Mesa;
import com.example.lab06.entity.ReservaMesa;
import com.example.lab06.entity.Usuario;
import com.example.lab06.repository.MesaRepository;
import com.example.lab06.repository.ReservaMesaRepository;
import com.example.lab06.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private ReservaMesaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String mostrarReservas(Model model, HttpSession session) {
        // Verificar que el usuario esté logueado
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        Boolean esAdmin = "ADMIN".equals(usuario.getRol().getNombre());
        model.addAttribute("usuario", usuario);
        model.addAttribute("esAdmin", esAdmin);

        // Estadísticas generales
        Long mesasDisponibles = mesaRepository.countMesasDisponibles();
        Long mesasOcupadas = mesaRepository.countMesasOcupadas();
        Long totalMesas = mesaRepository.count();
        Long reservasActivas = reservaRepository.countReservas();

        model.addAttribute("mesasDisponibles", mesasDisponibles);
        model.addAttribute("mesasOcupadas", mesasOcupadas);
        model.addAttribute("totalMesas", totalMesas);
        model.addAttribute("reservasActivas", reservasActivas);

        if (esAdmin) {
            // Vista de administrador
            List<Mesa> todasLasMesas = mesaRepository.findByOrderByNumeroAsc();
            List<ReservaMesa> reservasActivasList = reservaRepository.findByOrderByFechaDesc();
            List<Usuario> usuarios = usuarioRepository.findAll();

            model.addAttribute("mesas", todasLasMesas);
            model.addAttribute("reservasActivasList", reservasActivasList);
            model.addAttribute("usuarios", usuarios);
        } else {
            // Vista de usuario
            List<Mesa> mesasDisponiblesList = mesaRepository.findByDisponibleTrueOrderByNumeroAsc();
            Optional<ReservaMesa> reservaActual = reservaRepository.findByUsuario(usuario);

            model.addAttribute("mesasDisponiblesList", mesasDisponiblesList);
            model.addAttribute("tieneReserva", reservaActual.isPresent());
            
            if (reservaActual.isPresent()) {
                model.addAttribute("reservaActual", reservaActual.get());
            }
        }

        return "reservas";
    }

    @PostMapping("/crear-mesa")
    public String crearMesa(@RequestParam("numeroMesa") Integer numeroMesa,
                           Model model, HttpSession session) {
        // Verificar que sea admin
        Long adminId = (Long) session.getAttribute("usuarioId");
        if (adminId == null) {
            return "redirect:/login";
        }

        Usuario admin = usuarioRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRol().getNombre())) {
            return "redirect:/reservas";
        }

        // Verificar que no exista una mesa con ese número
        Optional<Mesa> mesaExistente = mesaRepository.findByNumero(numeroMesa);
        if (mesaExistente.isPresent()) {
            model.addAttribute("error", "Ya existe una mesa con el número " + numeroMesa);
            return mostrarReservas(model, session);
        }

        Mesa nuevaMesa = new Mesa(numeroMesa);
        mesaRepository.save(nuevaMesa);

        model.addAttribute("exito", "Mesa " + numeroMesa + " creada exitosamente");
        return mostrarReservas(model, session);
    }

    @PostMapping("/reservar")
    public String reservarMesa(@RequestParam("mesaId") Long mesaId,
                              Model model, HttpSession session) {
        // Verificar que el usuario esté logueado
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        // Validar que el usuario no tenga ya una reserva activa
        if (reservaRepository.existsByUsuario(usuario)) {
            model.addAttribute("error", "Ya tienes una reserva activa. Solo puedes reservar una mesa por cuenta.");
            return mostrarReservas(model, session);
        }

        Mesa mesa = mesaRepository.findById(mesaId).orElse(null);
        if (mesa == null) {
            model.addAttribute("error", "Mesa no encontrada");
            return mostrarReservas(model, session);
        }

        // Validar que la mesa esté disponible y tenga espacio
        if (!mesa.getDisponible() || !mesa.puedeReservar()) {
            model.addAttribute("error", "La mesa " + mesa.getNumero() + " no está disponible o está llena");
            return mostrarReservas(model, session);
        }

        // Crear la reserva
        ReservaMesa nuevaReserva = new ReservaMesa(usuario, mesa);
        reservaRepository.save(nuevaReserva);

        // Si la mesa está llena después de esta reserva, marcarla como no disponible
        if (mesa.getReservasActivas() >= mesa.getCapacidad() - 1) {
            mesa.setDisponible(false);
            mesaRepository.save(mesa);
        }

        model.addAttribute("exito", "Reserva realizada exitosamente en la mesa " + mesa.getNumero());
        return mostrarReservas(model, session);
    }

    @PostMapping("/liberar/{reservaId}")
    public String liberarReserva(@PathVariable Long reservaId,
                                Model model, HttpSession session) {
        // Verificar que sea admin
        Long adminId = (Long) session.getAttribute("usuarioId");
        if (adminId == null) {
            return "redirect:/login";
        }

        Usuario admin = usuarioRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRol().getNombre())) {
            return "redirect:/reservas";
        }

        Optional<ReservaMesa> reservaOpt = reservaRepository.findById(reservaId);
        if (reservaOpt.isPresent()) {
            ReservaMesa reserva = reservaOpt.get();
            
            // Eliminar la reserva
            reservaRepository.delete(reserva);

            // Marcar la mesa como disponible
            Mesa mesa = reserva.getMesa();
            mesa.setDisponible(true);
            mesaRepository.save(mesa);

            model.addAttribute("exito", "Reserva liberada exitosamente para " + 
                              reserva.getUsuario().getNombre());
        } else {
            model.addAttribute("error", "Reserva no encontrada");
        }

        return mostrarReservas(model, session);
    }

    @PostMapping("/cambiar-disponibilidad/{mesaId}")
    public String cambiarDisponibilidad(@PathVariable Long mesaId,
                                       @RequestParam("disponible") Boolean disponible,
                                       Model model, HttpSession session) {
        // Verificar que sea admin
        Long adminId = (Long) session.getAttribute("usuarioId");
        if (adminId == null) {
            return "redirect:/login";
        }

        Usuario admin = usuarioRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRol().getNombre())) {
            return "redirect:/reservas";
        }

        Optional<Mesa> mesaOpt = mesaRepository.findById(mesaId);
        if (mesaOpt.isPresent()) {
            Mesa mesa = mesaOpt.get();
            mesa.setDisponible(disponible);
            mesaRepository.save(mesa);

            String estado = disponible ? "habilitada" : "deshabilitada";
            model.addAttribute("exito", "Mesa " + mesa.getNumero() + " " + estado + " exitosamente");
        } else {
            model.addAttribute("error", "Mesa no encontrada");
        }

        return mostrarReservas(model, session);
    }

    @PostMapping("/cancelar-reserva")
    public String cancelarReserva(Model model, HttpSession session) {
        // Verificar que el usuario esté logueado
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        Optional<ReservaMesa> reservaOpt = reservaRepository.findByUsuario(usuario);
        if (reservaOpt.isPresent()) {
            ReservaMesa reserva = reservaOpt.get();
            
            // Eliminar la reserva
            reservaRepository.delete(reserva);

            // Marcar la mesa como disponible
            Mesa mesa = reserva.getMesa();
            mesa.setDisponible(true);
            mesaRepository.save(mesa);

            model.addAttribute("exito", "Tu reserva ha sido cancelada exitosamente");
        } else {
            model.addAttribute("error", "No tienes ninguna reserva activa para cancelar");
        }

        return mostrarReservas(model, session);
    }
}