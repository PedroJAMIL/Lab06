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
            if (asignacion.isPresent()) {
                AsignacionCancion asig = asignacion.get();
                boolean tieneCancionAsignada = asig.getCancion() != null && !asig.getSolicitudPendiente();
                model.addAttribute("tieneAsignacion", tieneCancionAsignada);
                model.addAttribute("tieneSolicitudPendiente", asig.getSolicitudPendiente());
                
                if (tieneCancionAsignada) {
                    CancionCriolla cancion = asig.getCancion();
                    model.addAttribute("cancionAsignada", cancion);
                    
                    model.addAttribute("intentosRealizados", asig.getIntentosRealizados());
                    model.addAttribute("juegoCompletado", asig.getJuegoCompletado());
                    
                    String titulo = cancion.getTitulo();
                    model.addAttribute("espaciosEnTitulo", contarEspacios(titulo));
                    model.addAttribute("palabrasEnTitulo", contarPalabras(titulo));
                    
                    String progreso = asig.getProgresoJuego();
                    if (progreso != null && !progreso.isEmpty()) {
                        String progresoVisual = getProgresoVisual(progreso, titulo);
                        model.addAttribute("progresoVisual", progresoVisual);
                        model.addAttribute("tituloMostrado", progresoVisual);
                    } else {
                        String tituloOculto = crearTituloOculto(titulo);
                        model.addAttribute("tituloMostrado", tituloOculto);
                        model.addAttribute("progresoVisual", tituloOculto);
                    }
                }
            } else {
                model.addAttribute("tieneAsignacion", false);
                model.addAttribute("tieneSolicitudPendiente", false);
            }
        }

        return "canciones";
    }

    @PostMapping("/asignar")
    public String asignarCancion(@RequestParam("usuarioId") Long usuarioId,
                                @RequestParam("cancionId") Long cancionId,
                                Model model, HttpSession session) {
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

        Optional<AsignacionCancion> asignacionExistente = asignacionRepository.findByUsuario(usuario);
        
        if (asignacionExistente.isPresent()) {
            AsignacionCancion asignacion = asignacionExistente.get();
            if (asignacion.getSolicitudPendiente()) {
                model.addAttribute("error", "Ya tienes una solicitud pendiente");
            } else {
                model.addAttribute("error", "Ya tienes una canción asignada");
            }
        } else {
            AsignacionCancion solicitud = new AsignacionCancion();
            solicitud.setUsuario(usuario);
            solicitud.setCancion(null);
            solicitud.setSolicitudPendiente(true);
            asignacionRepository.save(solicitud);
            
            model.addAttribute("exito", "Solicitud enviada exitosamente");
        }

        return mostrarCanciones(model, session);
    }
    
    @PostMapping("/intentar")
    public String procesarIntento(@RequestParam("intento") String intento, 
                                 Model model, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        Optional<AsignacionCancion> asignacionOpt = asignacionRepository.findByUsuario(usuario);
        if (!asignacionOpt.isPresent()) {
            model.addAttribute("error", "No tienes una canción asignada");
            return mostrarCanciones(model, session);
        }

        AsignacionCancion asignacion = asignacionOpt.get();
        
        if (asignacion.getJuegoCompletado()) {
            model.addAttribute("error", "Ya completaste este juego");
            return mostrarCanciones(model, session);
        }

        String titulo = asignacion.getCancion().getTitulo();
        
        asignacion.setIntentosRealizados(asignacion.getIntentosRealizados() + 1);
        
        String nuevoProgreso = procesarPalabrasIndividuales(intento, titulo, asignacion.getProgresoJuego());
        asignacion.setProgresoJuego(nuevoProgreso);
        
        boolean juegoCompletado = isJuegoCompletado(nuevoProgreso, titulo);
        
        boolean acierto = esIntentoCompleto(intento, titulo);
        
        if (acierto || juegoCompletado) {
            asignacion.setJuegoCompletado(true);
            model.addAttribute("exito", "¡Felicitaciones! Adivinaste la canción en " + 
                             asignacion.getIntentosRealizados() + " intentos.");
        } else {
            String progresoVisual = getProgresoVisual(nuevoProgreso, titulo);
            model.addAttribute("progresoVisual", progresoVisual);
            model.addAttribute("palabrasAdivinadas", "Progreso: " + progresoVisual);
        }
        
        asignacionRepository.save(asignacion);
        
        return mostrarCanciones(model, session);
    }
    
    @GetMapping("/ranking")
    public String mostrarRanking(Model model, HttpSession session) {
        // Verificar que el usuario esté logueado
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        // Obtener top 10 usuarios que completaron el juego, ordenados por menos intentos
        List<AsignacionCancion> ranking = asignacionRepository.findByJuegoCompletadoTrueOrderByIntentosRealizadosAsc();
        
        // Limitar a top 10
        if (ranking.size() > 10) {
            ranking = ranking.subList(0, 10);
        }
        
        model.addAttribute("ranking", ranking);
        model.addAttribute("totalCompletados", asignacionRepository.countByJuegoCompletadoTrue());
        
        return "ranking";
    }
    
    // Métodos auxiliares para el juego
    private int contarEspacios(String texto) {
        return (int) texto.chars().filter(ch -> ch == ' ').count();
    }
    
    private int contarPalabras(String texto) {
        return texto.trim().split("\\s+").length;
    }
    
    private String crearTituloOculto(String titulo) {
        return titulo.replaceAll("[a-zA-ZáéíóúÁÉÍÓÚñÑ]", "*");
    }
    
    private boolean esIntentoCompleto(String intento, String titulo) {
        return intento.toLowerCase().trim().equals(titulo.toLowerCase().trim());
    }
    
    // Método para procesar palabras individuales y actualizar progreso
    private String procesarPalabrasIndividuales(String intento, String titulo, String progresoActual) {
        String[] palabrasIntento = intento.toLowerCase().trim().split("\\s+");
        String[] palabrasTitulo = titulo.toLowerCase().trim().split("\\s+");
        
        // Inicializar progreso si está vacío
        String[] palabrasAdivinadas;
        if (progresoActual == null || progresoActual.isEmpty()) {
            palabrasAdivinadas = new String[palabrasTitulo.length];
            for (int i = 0; i < palabrasAdivinadas.length; i++) {
                palabrasAdivinadas[i] = "";
            }
        } else {
            palabrasAdivinadas = progresoActual.split("\\|");
            // Asegurar que el array tenga el tamaño correcto
            if (palabrasAdivinadas.length != palabrasTitulo.length) {
                String[] temp = new String[palabrasTitulo.length];
                for (int i = 0; i < temp.length; i++) {
                    temp[i] = i < palabrasAdivinadas.length ? palabrasAdivinadas[i] : "";
                }
                palabrasAdivinadas = temp;
            }
        }
        
        // Verificar cada palabra del intento contra cada palabra del título
        for (String palabraIntento : palabrasIntento) {
            for (int i = 0; i < palabrasTitulo.length; i++) {
                if (palabraIntento.equals(palabrasTitulo[i]) && palabrasAdivinadas[i].isEmpty()) {
                    palabrasAdivinadas[i] = palabrasTitulo[i];
                    break; // Solo marcar la primera coincidencia para evitar duplicados
                }
            }
        }
        
        // Convertir el array de progreso de vuelta a string
        return String.join("|", palabrasAdivinadas);
    }
    
    // Método para verificar si el juego está completado
    private boolean isJuegoCompletado(String progreso, String titulo) {
        if (progreso == null || progreso.isEmpty()) {
            return false;
        }
        
        String[] palabrasAdivinadas = progreso.split("\\|");
        String[] palabrasTitulo = titulo.toLowerCase().trim().split("\\s+");
        
        if (palabrasAdivinadas.length != palabrasTitulo.length) {
            return false;
        }
        
        // Verificar que todas las palabras estén adivinadas
        for (String palabra : palabrasAdivinadas) {
            if (palabra.isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    // Método para obtener el progreso visual para mostrar en la plantilla
    private String getProgresoVisual(String progreso, String titulo) {
        if (progreso == null || progreso.isEmpty()) {
            // Mostrar guiones bajos para palabras no adivinadas
            String[] palabrasTitulo = titulo.split("\\s+");
            StringBuilder visual = new StringBuilder();
            for (int i = 0; i < palabrasTitulo.length; i++) {
                if (i > 0) visual.append(" ");
                visual.append("_".repeat(palabrasTitulo[i].length()));
            }
            return visual.toString();
        }
        
        String[] palabrasAdivinadas = progreso.split("\\|");
        String[] palabrasTitulo = titulo.split("\\s+");
        StringBuilder visual = new StringBuilder();
        
        for (int i = 0; i < palabrasTitulo.length; i++) {
            if (i > 0) visual.append(" ");
            if (i < palabrasAdivinadas.length && !palabrasAdivinadas[i].isEmpty()) {
                visual.append(palabrasAdivinadas[i]);
            } else {
                visual.append("_".repeat(palabrasTitulo[i].length()));
            }
        }
        
        return visual.toString();
    }
}