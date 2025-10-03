package com.example.lab06.controller;

import com.example.lab06.entity.JuegoCaminoDulces;
import com.example.lab06.entity.Usuario;
import com.example.lab06.repository.JuegoCaminoDulcesRepository;
import com.example.lab06.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/camino-dulces")
public class CaminoDulcesController {

    @Autowired
    private JuegoCaminoDulcesRepository juegoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String mostrarJuego(Model model, HttpSession session) {
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
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<JuegoCaminoDulces> juegosActivos = juegoRepository.findByJuegoHabilitadoTrue();
            List<JuegoCaminoDulces> juegosDeshabilitados = juegoRepository.findByJuegoHabilitadoFalse();
            
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("juegosActivos", juegosActivos);
            model.addAttribute("juegosDeshabilitados", juegosDeshabilitados);
            model.addAttribute("totalJuegosActivos", juegosActivos.size());
            model.addAttribute("totalJuegosCompletados", juegoRepository.countByJuegoCompletadoTrue());
        } else {
            Optional<JuegoCaminoDulces> juegoOpt = juegoRepository.findByUsuario(usuario);
            
            if (juegoOpt.isPresent()) {
                JuegoCaminoDulces juego = juegoOpt.get();
                model.addAttribute("tieneJuego", true);
                model.addAttribute("juego", juego);
                model.addAttribute("juegoHabilitado", juego.getJuegoHabilitado());
                model.addAttribute("juegoCompletado", juego.getJuegoCompletado());
                
                if (juego.getJuegoHabilitado() && !juego.getJuegoCompletado()) {
                    int pasosMinimos = calcularPasosMinimos(juego.getPosicionActual(), juego.getPosicionCasaDulces());
                    model.addAttribute("pasosMinimos", pasosMinimos);
                }
            } else {
                model.addAttribute("tieneJuego", false);
            }
        }

        List<JuegoCaminoDulces> ranking = juegoRepository.findByJuegoCompletadoTrueOrderByIntentosRealizadosAsc();
        if (ranking.size() > 10) {
            ranking = ranking.subList(0, 10);
        }
        model.addAttribute("ranking", ranking);

        return "camino-dulces";
    }

    @PostMapping("/asignar")
    public String asignarJuego(@RequestParam("usuarioId") Long usuarioId,
                              @RequestParam("posicionCasa") Integer posicionCasa,
                              Model model, HttpSession session) {
        // Verificar que sea admin
        Long adminId = (Long) session.getAttribute("usuarioId");
        if (adminId == null) {
            return "redirect:/login";
        }

        Usuario admin = usuarioRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRol().getNombre())) {
            return "redirect:/camino-dulces";
        }

        // Validar que la posición sea mayor a 64
        if (posicionCasa <= 64) {
            model.addAttribute("error", "La posición de la casa debe ser mayor a 64");
            return mostrarJuego(model, session);
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            model.addAttribute("error", "Usuario no encontrado");
            return mostrarJuego(model, session);
        }

        // Verificar si ya tiene un juego asignado
        Optional<JuegoCaminoDulces> juegoExistente = juegoRepository.findByUsuario(usuario);
        
        if (juegoExistente.isPresent()) {
            JuegoCaminoDulces juego = juegoExistente.get();
            juego.setPosicionCasaDulces(posicionCasa);
            juego.setJuegoHabilitado(true);
            // Reiniciar juego si se cambia la posición
            juego.setPosicionActual(1);
            juego.setIntentosRealizados(0);
            juego.setJuegoCompletado(false);
            juego.setFechaCompletado(null);
            juegoRepository.save(juego);
        } else {
            JuegoCaminoDulces nuevoJuego = new JuegoCaminoDulces(usuario, posicionCasa);
            juegoRepository.save(nuevoJuego);
        }

        model.addAttribute("exito", "Juego asignado exitosamente a " + usuario.getNombre() + 
                                   " con casa en posición " + posicionCasa);
        return mostrarJuego(model, session);
    }

    @PostMapping("/deshabilitar/{id}")
    public String deshabilitarJuego(@PathVariable Long id, Model model, HttpSession session) {
        // Verificar que sea admin
        Long adminId = (Long) session.getAttribute("usuarioId");
        if (adminId == null) {
            return "redirect:/login";
        }

        Usuario admin = usuarioRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRol().getNombre())) {
            return "redirect:/camino-dulces";
        }

        Optional<JuegoCaminoDulces> juegoOpt = juegoRepository.findById(id);
        if (juegoOpt.isPresent()) {
            JuegoCaminoDulces juego = juegoOpt.get();
            juego.setJuegoHabilitado(false);
            juegoRepository.save(juego);
            model.addAttribute("exito", "Juego deshabilitado para " + juego.getUsuario().getNombre());
        } else {
            model.addAttribute("error", "Juego no encontrado");
        }

        return mostrarJuego(model, session);
    }

    @PostMapping("/habilitar/{id}")
    public String habilitarJuego(@PathVariable Long id, Model model, HttpSession session) {
        // Verificar que sea admin
        Long adminId = (Long) session.getAttribute("usuarioId");
        if (adminId == null) {
            return "redirect:/login";
        }

        Usuario admin = usuarioRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRol().getNombre())) {
            return "redirect:/camino-dulces";
        }

        Optional<JuegoCaminoDulces> juegoOpt = juegoRepository.findById(id);
        if (juegoOpt.isPresent()) {
            JuegoCaminoDulces juego = juegoOpt.get();
            juego.setJuegoHabilitado(true);
            juegoRepository.save(juego);
            model.addAttribute("exito", "Juego habilitado para " + juego.getUsuario().getNombre());
        } else {
            model.addAttribute("error", "Juego no encontrado");
        }

        return mostrarJuego(model, session);
    }

    @PostMapping("/intentar")
    public String procesarIntento(@RequestParam("numeroObjetivo") Integer numeroObjetivo,
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

        Optional<JuegoCaminoDulces> juegoOpt = juegoRepository.findByUsuario(usuario);
        if (!juegoOpt.isPresent()) {
            model.addAttribute("error", "No tienes un juego asignado");
            return mostrarJuego(model, session);
        }

        JuegoCaminoDulces juego = juegoOpt.get();
        
        if (!juego.getJuegoHabilitado()) {
            model.addAttribute("error", "Tu juego está deshabilitado");
            return mostrarJuego(model, session);
        }

        if (juego.getJuegoCompletado()) {
            model.addAttribute("error", "Ya completaste este juego");
            return mostrarJuego(model, session);
        }

        // Incrementar intentos
        juego.setIntentosRealizados(juego.getIntentosRealizados() + 1);

        // Calcular pasos mínimos al número objetivo
        int pasosAlObjetivo = calcularPasosMinimos(juego.getPosicionActual(), numeroObjetivo);
        
        // Verificar si llegó a la casa con dulces
        if (numeroObjetivo.equals(juego.getPosicionCasaDulces())) {
            juego.setJuegoCompletado(true);
            juego.setPosicionActual(numeroObjetivo);
            juegoRepository.save(juego);
            
            model.addAttribute("exito", "¡Felicitaciones! Llegaste a la casa con dulces en " + 
                             juego.getIntentosRealizados() + " intentos!");
        } else {
            // Actualizar posición actual y mostrar información
            juego.setPosicionActual(numeroObjetivo);
            juegoRepository.save(juego);
            
            int pasosACasa = calcularPasosMinimos(numeroObjetivo, juego.getPosicionCasaDulces());
            model.addAttribute("info", "Te moviste a la posición " + numeroObjetivo + 
                             ". Necesitas " + pasosACasa + " pasos más para llegar a la casa con dulces.");
            model.addAttribute("pasosRequeridos", pasosACasa);
        }

        return mostrarJuego(model, session);
    }

    // Método para calcular pasos mínimos entre dos posiciones
    private int calcularPasosMinimos(int posicionActual, int posicionObjetivo) {
        if (posicionActual == posicionObjetivo) {
            return 0;
        }
        
        // El niño puede avanzar 1, 2, 3, 4, 5 o 6 posiciones por movimiento
        // Usamos BFS para encontrar el mínimo número de pasos
        java.util.Queue<Integer> cola = new java.util.LinkedList<>();
        java.util.Set<Integer> visitados = new java.util.HashSet<>();
        java.util.Map<Integer, Integer> distancias = new java.util.HashMap<>();
        
        cola.offer(posicionActual);
        visitados.add(posicionActual);
        distancias.put(posicionActual, 0);
        
        while (!cola.isEmpty()) {
            int posicionActualBFS = cola.poll();
            int distanciaActual = distancias.get(posicionActualBFS);
            
            if (posicionActualBFS == posicionObjetivo) {
                return distanciaActual;
            }
            
            // Probar movimientos de 1 a 6 posiciones
            for (int movimiento = 1; movimiento <= 6; movimiento++) {
                int nuevaPosicion = posicionActualBFS + movimiento;
                
                // Limitar búsqueda para evitar exploración infinita
                if (nuevaPosicion <= posicionObjetivo + 6 && !visitados.contains(nuevaPosicion)) {
                    visitados.add(nuevaPosicion);
                    distancias.put(nuevaPosicion, distanciaActual + 1);
                    cola.offer(nuevaPosicion);
                }
            }
        }
        
        // Si no se puede llegar (caso poco probable), calcular estimación
        return Math.abs(posicionObjetivo - posicionActual) / 6 + 1;
    }
}