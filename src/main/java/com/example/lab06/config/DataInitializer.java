package com.example.lab06.config;

import com.example.lab06.entity.CancionCriolla;
import com.example.lab06.entity.Rol;
import com.example.lab06.entity.Usuario;
import com.example.lab06.repository.CancionCriollaRepository;
import com.example.lab06.repository.RolRepository;
import com.example.lab06.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private CancionCriollaRepository cancionRepository;

    @Override
    public void run(String... args) throws Exception {
        // Crear roles si no existen
        if (rolRepository.count() == 0) {
            Rol rolAdmin = new Rol();
            rolAdmin.setNombre("ADMIN");
            rolRepository.save(rolAdmin);

            Rol rolUsuario = new Rol();
            rolUsuario.setNombre("USUARIO");
            rolRepository.save(rolUsuario);

            System.out.println("Roles creados: ADMIN y USUARIO");
        }

        // Crear usuarios si no existen
        if (usuarioRepository.count() == 0) {
            // Buscar roles
            Rol rolAdmin = rolRepository.findByNombre("ADMIN");
            Rol rolUsuario = rolRepository.findByNombre("USUARIO");

            // Crear usuario admin
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setCorreo("admin@example.com");
            admin.setPassword("admin123");
            admin.setRol(rolAdmin);
            usuarioRepository.save(admin);

            // Crear usuario normal
            Usuario usuario = new Usuario();
            usuario.setNombre("Carlos Vargas");
            usuario.setCorreo("carlos.vargas@example.com");
            usuario.setPassword("123456");
            usuario.setRol(rolUsuario);
            usuarioRepository.save(usuario);

            System.out.println("Usuarios creados:");
            System.out.println("- Admin: admin@example.com / admin123");
            System.out.println("- Usuario: carlos.vargas@example.com / 123456");
        }

        // Crear canciones criollas si no existen
        if (cancionRepository.count() == 0) {
            String[][] cancionesData = {
                {"La Flor de la Canela", "Chabuca Granda"},
                {"El Cóndor Pasa", "Daniel Alomía Robles"},
                {"Fina Estampa", "Chabuca Granda"},
                {"Vals de las Flores", "Pinglo Alva"},
                {"El Plebeyo", "Felipe Pinglo Alva"},
                {"José Antonio", "Chabuca Granda"},
                {"Alma Corazón y Vida", "Adrián Flores Alván"},
                {"Regresa", "Augusto Polo Campos"},
                {"Y se llama Perú", "Augusto Polo Campos"},
                {"Cuando Llora mi Guitarra", "Felipe Pinglo Alva"}
            };

            for (String[] datos : cancionesData) {
                CancionCriolla cancion = new CancionCriolla();
                cancion.setTitulo(datos[0]);
                cancion.setArtista(datos[1]);
                cancionRepository.save(cancion);
            }

            System.out.println("Canciones criollas creadas: " + cancionesData.length + " canciones");
        }
    }
}