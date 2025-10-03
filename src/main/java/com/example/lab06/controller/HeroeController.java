package com.example.lab06.controller;

import com.example.lab06.entity.HeroeNaval;
import com.example.lab06.entity.Usuario;
import com.example.lab06.repository.HeroeNavalRepository;
import com.example.lab06.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class HeroeController {

    @Autowired
    private HeroeNavalRepository heroeNavalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/heroes")
    public String listarHeroes(Model model, HttpSession session) {
        List<HeroeNaval> heroes = heroeNavalRepository.findAll();
        model.addAttribute("heroes", heroes);

        Boolean esAdmin = (Boolean) session.getAttribute("esAdmin");
        if (esAdmin == null) {
            esAdmin = false;
        }
        model.addAttribute("esAdmin", esAdmin);

        if (esAdmin) {
            model.addAttribute("nuevoHeroe", new HeroeNaval());
        }

        return "heroes";
    }

    @PostMapping("/heroes/guardar")
    public String guardarHeroe(@ModelAttribute HeroeNaval heroe, HttpSession session) {
        Boolean esAdmin = (Boolean) session.getAttribute("esAdmin");
        if (esAdmin != null && esAdmin) {
            heroeNavalRepository.save(heroe);
        }
        return "redirect:/heroes";
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo, 
                               @RequestParam String password, 
                               HttpSession session,
                               Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        
        if (usuario != null && usuario.getPassword().equals(password)) {
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioNombre", usuario.getNombre());
            
            boolean esAdmin = usuario.getRol().getId().equals(1L);
            session.setAttribute("esAdmin", esAdmin);
            
            return "redirect:/heroes";
        } else {
            model.addAttribute("error", "Credenciales incorrectas");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/usuarios")
    public String verUsuarios(Model model, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null || !"ADMIN".equals(usuario.getRol().getNombre())) {
            return "redirect:/";
        }
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuario", usuario);
        model.addAttribute("esAdmin", true);

        return "usuarios";
    }
}