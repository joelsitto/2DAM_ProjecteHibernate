package com.joelsitto.bang.controllers;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.services.PartidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// Controller per gestionar les partides
@RestController
@RequestMapping("/partida")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    // Endpoint per obtenir els jugadors d'una partida
    // URL: GET /partida/llistar/1 per exemple
    @GetMapping("/llistar/{idPartida}")
    public ResponseEntity<?> llistarJugadorsPartida(@PathVariable int idPartida) {
        try {
            // Buscar els jugadors de la partida
            List<Jugador> jugadors = partidaService.llistarJugadorsPartida(idPartida);

            // Retornar els jugadors en format JSON
            return ResponseEntity.ok(jugadors);
        } catch (RuntimeException e) {
            // Si hi ha error, retornar el missatge d'error
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint per crear una partida nova
    // URL: POST /partida/iniciar
    @PostMapping("/iniciar")
    public ResponseEntity<Partida> iniciarPartida() {
        // Crear la partida
        Partida partida = partidaService.iniciarPartida();

        // Retornar la partida en format JSON
        return ResponseEntity.ok(partida);
    }
}