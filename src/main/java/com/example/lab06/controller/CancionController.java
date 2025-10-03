package com.example.lab06.controller;

import com.example.lab06.entity.AsignacionCancion;
import com.example.lab06.entity.CancionCriolla;
import com.example.lab06.entity.Usuario;
import com.example.lab06.repository.AsignacionCancionRepository;
import com.example.lab06.repository.CancionCriollaRepository;
import com.example.lab06.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/canciones")
public class CancionController {

    @Autowired
    private CancionCriollaRepository cancionRepository;

    @Autowired
    private AsignacionCancionRepository asignacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String mostrarCanciones(Model model, HttpSession session) {
        // Verificar si el usuario está logueado
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

        if (esAdmin) {
            List<CancionCriolla> canciones = cancionRepository.findAll();
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<AsignacionCancion> solicitudes = asignacionRepository.findBySolicitudPendienteTrue();
            
            model.addAttribute("canciones", canciones);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("solicitudes", solicitudes);
        } else {
            Optional<AsignacionCancion> asignacion = asignacionRepository.findByUsuario(usuario);
            model.addAttribute("tieneAsignacion", asignacion.isPresent());
            if (asignacion.isPresent()) {
                model.addAttribute("cancionAsignada", asignacion.get().getCancion());
            }
        }

        return "canciones";
    }

    @PostMapping("/asignar")
    public String asignarCancion(@RequestParam("usuarioId") Long usuarioId,
                                @RequestParam("cancionId") Long cancionId,
                                Model model, HttpSession session) {
        // Verificar que sea admin
        Long adminId = (Long) session.getAttribute("usuarioId");
        if (adminId == null) {
            return "redirect:/login";
        }

        Usuario admin = usuarioRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRol().getNombre())) {
            return "redirect:/canciones";
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        CancionCriolla cancion = cancionRepository.findById(cancionId).orElse(null);

        if (usuario != null && cancion != null) {
            Optional<AsignacionCancion> asignacionExistente = asignacionRepository.findByUsuario(usuario);
            
            if (asignacionExistente.isPresent()) {
                AsignacionCancion asignacion = asignacionExistente.get();
                asignacion.setCancion(cancion);
                asignacion.setSolicitudPendiente(false);
                asignacionRepository.save(asignacion);
            } else {
                AsignacionCancion nuevaAsignacion = new AsignacionCancion(usuario, cancion);
                asignacionRepository.save(nuevaAsignacion);
            }
            
            model.addAttribute("exito", "Canción asignada exitosamente a " + usuario.getNombre());
        } else {
            model.addAttribute("error", "Error al asignar la canción");
        }

        return mostrarCanciones(model, session);
    }

    @PostMapping("/solicitar")
    public String solicitarAsignacion(Model model, HttpSession session) {
        // Verificar que el usuario esté logueado
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        // Verificar si ya tiene una asignación o solicitud pendiente
        Optional<AsignacionCancion> asignacionExistente = asignacionRepository.findByUsuario(usuario);
        
        if (asignacionExistente.isPresent()) {
            AsignacionCancion asignacion = asignacionExistente.get();
            if (asignacion.getSolicitudPendiente()) {
                model.addAttribute("error", "Ya tienes una solicitud pendiente");
            } else {
                model.addAttribute("error", "Ya tienes una canción asignada");
            }
        } else {
            // Crear solicitud pendiente
            AsignacionCancion solicitud = new AsignacionCancion();
            solicitud.setUsuario(usuario);
            solicitud.setCancion(null); // Explícitamente establecer como null
            solicitud.setSolicitudPendiente(true);
            asignacionRepository.save(solicitud);
            
            model.addAttribute("exito", "Solicitud enviada exitosamente");
        }

        return mostrarCanciones(model, session);
    }
}