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
    // URL: GET /partida/llistar/1
    @GetMapping("/llistar/{idPartida}")
    public ResponseEntity<List<Jugador>> llistarJugadorsPartida(@PathVariable int idPartida) {
        // Buscar els jugadors de la partida
        List<Jugador> jugadors = partidaService.llistarJugadorsPartida(idPartida);

        // Si no hi ha jugadors, retornar error 404
        if (jugadors.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Retornar els jugadors en format JSON
        return ResponseEntity.ok(jugadors);
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