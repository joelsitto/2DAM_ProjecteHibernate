package com.joelsitto.bang.controllers;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.services.JugadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Controller para gestionar cosas de los jugadores
@RestController
@RequestMapping("/jugador")
public class JugadorController {

    @Autowired
    private JugadorService jugadorService;

    // Endpoint para usar BANG - un jugador ataca a otro
    // POST /jugador/usarBANG?idJugadorAtacant=1&idJugadorObjectiu=2 per exemple
    @PostMapping("/usarBANG")
    public ResponseEntity<String> usarBANG(@RequestParam int idJugadorAtacant, @RequestParam int idJugadorObjectiu) {
        // Llamar al service que hace la lógica del ataque
        String resultado = jugadorService.usarBANG(idJugadorAtacant, idJugadorObjectiu);
        return ResponseEntity.ok(resultado);
    }


    // Endpoint para descartar una carta de la mano del jugador
    // DELETE /jugador/descartar/{idJugador}/{idCarta}
    @DeleteMapping("/descartar/{idJugador}/{idCarta}")
    public ResponseEntity<?> descartarCarta(@PathVariable int idJugador, @PathVariable int idCarta) {
        try {
            // Llamar al service para descartar la carta
            Map<String, Object> resultado = jugadorService.descartarCarta(idJugador, idCarta);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            // Si hay error, devolver el mensaje de error
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

