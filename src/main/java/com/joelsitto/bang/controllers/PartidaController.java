package com.joelsitto.bang.controllers;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.services.PartidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller REST per gestionar les peticions relacionades amb Partides
 */
@RestController
@RequestMapping("/partida")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    /**
     * Llista els jugadors d'una partida
     * GET /partida/llistar/{idPartida}
     * Retorna un JSON amb la llista de jugadors de la partida
     *
     * @param idPartida ID de la partida (PathVariable)
     * @return ResponseEntity amb la llista de jugadors o 404 si no existeix
     */
    @GetMapping("/llistar/{idPartida}")
    public ResponseEntity<List<Jugador>> llistarJugadorsPartida(@PathVariable int idPartida) {
        // Obtenir els jugadors de la partida des del service
        List<Jugador> jugadors = partidaService.llistarJugadorsPartida(idPartida);

        // Si la llista està buida, retornar 404
        if (jugadors.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Retornar la llista de jugadors en format JSON
        return ResponseEntity.ok(jugadors);
    }

    /**
     * Inicia una nova partida
     * POST /partida/iniciar
     * Retorna un JSON amb la partida completa, incloent pila de cartes, jugadors i cartes en ma
     *
     * @return ResponseEntity amb la partida creada
     */
    @PostMapping("/iniciar")
    public ResponseEntity<Partida> iniciarPartida() {
        // Crear la nova partida utilitzant el service
        Partida partida = partidaService.iniciarPartida();
        // Retornar la partida completa en format JSON
        return ResponseEntity.ok(partida);
    }
}