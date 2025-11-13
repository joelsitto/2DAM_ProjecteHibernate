package com.joelsitto.bang.controllers;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.services.JugadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST per gestionar les peticions relacionades amb Jugadors
 */
@RestController
@RequestMapping("/jugador")
public class JugadorController {

    @Autowired
    private JugadorService jugadorService;

    /**
     * UsarBANG - Ejecuta un ataque BANG! entre dos jugadores
     * POST /jugador/usarBANG?idJugadorAtacant=1&idJugadorObjectiu=2
     * Retorna un String amb el que passa (atac encertat, Fallaste, Barril, etc)
     *
     * @param idJugadorAtacant ID del jugador que ataca (RequestParam)
     * @param idJugadorObjectiu ID del jugador objetivo (RequestParam)
     * @return String describiendo el resultado del ataque
     */
    @PostMapping("/usarBANG")
    public ResponseEntity<String> usarBANG(@RequestParam int idJugadorAtacant, @RequestParam int idJugadorObjectiu) {
        // Delegar la lògica al service
        String resultado = jugadorService.usarBANG(idJugadorAtacant, idJugadorObjectiu);
        return ResponseEntity.ok(resultado);
    }


    /**
     * DescartarCarta - Descarta una carta de la mano del jugador
     * DELETE /jugador/descartar/{idJugador}/{idCarta}
     * Retorna el jugador amb la seva mà actualitzada
     *
     * @param idJugador ID del jugador (PathVariable)
     * @param idCarta ID de la carta a descartar (PathVariable)
     * @return El jugador actualizado con su mano
     */
    @DeleteMapping("/descartar/{idJugador}/{idCarta}")
    public ResponseEntity<?> descartarCarta(@PathVariable int idJugador, @PathVariable int idCarta) {
        try {
            // Delegar la lògica al service
            Jugador jugadorActualizado = jugadorService.descartarCarta(idJugador, idCarta);
            return ResponseEntity.ok(jugadorActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

