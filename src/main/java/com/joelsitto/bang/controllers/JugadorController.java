package com.joelsitto.bang.controllers;

import com.joelsitto.bang.repositories.JugadorRepository;
import com.joelsitto.bang.repositories.CartaRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jugador")
public class JugadorController {

    private final JugadorRepository jugadorRepository;
    private final CartaRepository cartaRepository;

    public JugadorController(JugadorRepository jugadorRepository, CartaRepository cartaRepository) {
        this.jugadorRepository = jugadorRepository;
        this.cartaRepository = cartaRepository;
    }

    @PostMapping("/usarBANG")
    public String usarBANG(@RequestParam int idJugadorAtacant, @RequestParam int idJugadorObjectiu) {
        return null; // Aquí se implementará la lógica del ataque BANG!
    }

    @DeleteMapping("/descartar/{idJugador}/{idCarta}")
    public Object descartarCarta(@PathVariable int idJugador, @PathVariable int idCarta) {
        return null; // Aquí se actualizará la BD y se devolverá el jugador con su mano
    }
}
