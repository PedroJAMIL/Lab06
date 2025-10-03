package com.example.lab06.controller;

import com.example.lab06.entity.AsignacionCancion;
import com.example.lab06.entity.Intencion;
import com.example.lab06.entity.JuegoCaminoDulces;
import com.example.lab06.entity.Usuario;
import com.example.lab06.repository.AsignacionCancionRepository;
import com.example.lab06.repository.IntencionRepository;
import com.example.lab06.repository.JuegoCaminoDulcesRepository;
import com.example.lab06.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/juegos")
public class JuegoController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AsignacionCancionRepository asignacionRepository;

    @Autowired
    private IntencionRepository intencionRepository;

    @Autowired
    private JuegoCaminoDulcesRepository caminoDulcesRepository;

    @GetMapping
    public String mostrarJuegos(Model model, HttpSession session) {
        // Verificar que el usuario esté logueado
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        // Información del usuario
        model.addAttribute("usuario", usuario);
        boolean esAdmin = "ADMIN".equals(usuario.getRol().getNombre());
        model.addAttribute("esAdmin", esAdmin);

        // === ESTADÍSTICAS GENERALES ===
        // Juego de Canciones Criollas
        long totalAsignaciones = asignacionRepository.count();
        long juegoCompletado = asignacionRepository.countByJuegoCompletadoTrue();
        long solicitudesPendientes = asignacionRepository.findBySolicitudPendienteTrue().size();
        
        model.addAttribute("totalAsignaciones", totalAsignaciones);
        model.addAttribute("juegoCompletado", juegoCompletado);
        model.addAttribute("solicitudesPendientes", solicitudesPendientes);

        // Juego de Intenciones
        long totalIntenciones = intencionRepository.count();
        List<Intencion> intencionesRecientes = intencionRepository.findTop5ByOrderByFechaCreacionDesc();
        
        model.addAttribute("totalIntenciones", totalIntenciones);
        model.addAttribute("intencionesRecientes", intencionesRecientes);

        // Juego de Camino de Dulces
        long totalJuegosDulces = caminoDulcesRepository.count();
        long juegosCompletadosDulces = caminoDulcesRepository.countByJuegoCompletadoTrue();
        long juegosActivosDulces = caminoDulcesRepository.countByJuegoHabilitadoTrue();
        
        model.addAttribute("totalJuegosDulces", totalJuegosDulces);
        model.addAttribute("juegosCompletadosDulces", juegosCompletadosDulces);
        model.addAttribute("juegosActivosDulces", juegosActivosDulces);

        // === ESTADO DEL USUARIO ACTUAL ===
        if (!esAdmin) {
            // Para usuarios normales - su progreso personal
            Optional<AsignacionCancion> asignacion = asignacionRepository.findByUsuario(usuario);
            if (asignacion.isPresent()) {
                AsignacionCancion asig = asignacion.get();
                boolean tieneCancionAsignada = asig.getCancion() != null && !asig.getSolicitudPendiente();
                
                model.addAttribute("tieneAsignacion", tieneCancionAsignada);
                model.addAttribute("tieneSolicitudPendiente", asig.getSolicitudPendiente());
                model.addAttribute("juegoCompletadoUsuario", asig.getJuegoCompletado());
                model.addAttribute("intentosRealizados", asig.getIntentosRealizados());
                
                if (tieneCancionAsignada) {
                    model.addAttribute("cancionAsignada", asig.getCancion());
                }
            } else {
                model.addAttribute("tieneAsignacion", false);
                model.addAttribute("tieneSolicitudPendiente", false);
                model.addAttribute("juegoCompletadoUsuario", false);
            }

            // Intenciones del usuario
            List<Intencion> intencionesUsuario = intencionRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
            model.addAttribute("intencionesUsuario", intencionesUsuario);

            // Juego de Camino de Dulces del usuario
            Optional<JuegoCaminoDulces> juegoDulces = caminoDulcesRepository.findByUsuario(usuario);
            if (juegoDulces.isPresent()) {
                JuegoCaminoDulces juego = juegoDulces.get();
                model.addAttribute("tieneJuegoDulces", true);
                model.addAttribute("juegoDulces", juego);
                model.addAttribute("juegoHabilitadoDulces", juego.getJuegoHabilitado());
                model.addAttribute("juegoCompletadoDulces", juego.getJuegoCompletado());
            } else {
                model.addAttribute("tieneJuegoDulces", false);
            }
        } else {
            // Para admin - ranking y estadísticas
            List<AsignacionCancion> ranking = asignacionRepository.findByJuegoCompletadoTrueOrderByIntentosRealizadosAsc();
            if (ranking.size() > 5) {
                ranking = ranking.subList(0, 5); // Top 5 para el dashboard
            }
            model.addAttribute("topRanking", ranking);
        }

        return "juegos";
    }
}