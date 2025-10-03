package com.example.lab06.controller;

import com.example.lab06.entity.Intencion;
import com.example.lab06.entity.Usuario;
import com.example.lab06.repository.IntencionRepository;
import com.example.lab06.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/intenciones")
public class IntencionController {

    @Autowired
    private IntencionRepository intencionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Lista de palabras prohibidas
    private static final List<String> PALABRAS_PROHIBIDAS = Arrays.asList(
        "odio", "pelea", "violencia", "agresion", "matar", "golpear", 
        "destruir", "dañar", "lastimar", "herir"
    );

    @GetMapping
    public String mostrarIntenciones(Model model, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        Boolean esAdmin = "ADMIN".equals(usuario.getRol().getNombre());

        Boolean tieneIntencion = Boolean.TRUE.equals(session.getAttribute("intencionRegistrada"));

        model.addAttribute("usuario", usuario);
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("tieneIntencion", tieneIntencion);

        if (esAdmin) {
            List<Intencion> todasIntenciones = intencionRepository.findAllByOrderByFechaCreacionDesc();
            model.addAttribute("intenciones", todasIntenciones);
        }

        return "intenciones";
    }

    @PostMapping("/registrar")
    public String registrarIntencion(@RequestParam("contenido") String contenido,
                                   Model model, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        if (Boolean.TRUE.equals(session.getAttribute("intencionRegistrada"))) {
            model.addAttribute("error", "Ya has registrado una intención en esta sesión.");
            return mostrarIntenciones(model, session);
        }

        if (contenido == null || contenido.trim().length() < 15) {
            model.addAttribute("error", "La intención debe tener al menos 15 caracteres.");
            return mostrarIntenciones(model, session);
        }

        String contenidoLower = contenido.toLowerCase();
        for (String palabraProhibida : PALABRAS_PROHIBIDAS) {
            if (contenidoLower.contains(palabraProhibida)) {
                model.addAttribute("error", "El contenido contiene palabras no permitidas.");
                return mostrarIntenciones(model, session);
            }
        }

        // Obtener usuario
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            // Crear y guardar la intención
            Intencion intencion = new Intencion();
            intencion.setContenido(contenido.trim());
            intencion.setDescripcion(contenido.trim()); // Usar el mismo contenido para descripcion
            intencion.setUsuario(usuario);
            intencionRepository.save(intencion);

            // Marcar en la sesión que ya registró una intención
            session.setAttribute("intencionRegistrada", Boolean.TRUE);

            model.addAttribute("exito", "Intención registrada exitosamente.");
        } catch (Exception e) {
            System.out.println("Error al guardar intención: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al guardar la intención. Por favor, intenta de nuevo.");
        }
        
        return mostrarIntenciones(model, session);
    }
}