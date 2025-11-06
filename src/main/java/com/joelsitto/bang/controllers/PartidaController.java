package com.joelsitto.bang.controllers;

import com.joelsitto.bang.repositories.PartidaRepository;
import com.joelsitto.bang.repositories.JugadorRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/partida")
public class PartidaController {

    private final PartidaRepository partidaRepository;
    private final JugadorRepository jugadorRepository;

    public PartidaController(PartidaRepository partidaRepository, JugadorRepository jugadorRepository) {
        this.partidaRepository = partidaRepository;
        this.jugadorRepository = jugadorRepository;
    }

    @GetMapping("/llistar/{idPartida}")
    public Object llistarJugadorsPartida(@PathVariable int idPartida) {
        return null; // Aquí irà la consulta que devuelva los jugadores en JSON
    }

    @PostMapping("/iniciar")
    public Object iniciarPartida() {
        return null; // Aquí se inicializará una nueva partida
    }
}