package com.joelsitto.bang.controllers;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.TipusEquipament;
import com.joelsitto.bang.model.enums.TipusUs;
import com.joelsitto.bang.repositories.JugadorRepository;
import com.joelsitto.bang.repositories.CartaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/jugador")
public class JugadorController {

    private final JugadorRepository jugadorRepository;
    private final CartaRepository cartaRepository;
    private final Random random = new Random();

    public JugadorController(JugadorRepository jugadorRepository, CartaRepository cartaRepository) {
        this.jugadorRepository = jugadorRepository;
        this.cartaRepository = cartaRepository;
    }

    /**
     * UsarBANG - Ejecuta un ataque BANG! entre dos jugadores
     * @param idJugadorAtacant ID del jugador que ataca
     * @param idJugadorObjectiu ID del jugador objetivo
     * @return String describiendo el resultado del ataque
     */
    @PostMapping("/usarBANG")
    public ResponseEntity<String> usarBANG(@RequestParam int idJugadorAtacant, @RequestParam int idJugadorObjectiu) {
        Optional<Jugador> atacantOpt = jugadorRepository.findById(idJugadorAtacant);
        Optional<Jugador> objectiuOpt = jugadorRepository.findById(idJugadorObjectiu);

        if (atacantOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El jugador atacante con ID " + idJugadorAtacant + " no existe");
        }
        if (objectiuOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El jugador objetivo con ID " + idJugadorObjectiu + " no existe");
        }

        Jugador atacant = atacantOpt.get();
        Jugador objectiu = objectiuOpt.get();

        if (atacant.getVidaActual() <= 0) {
            return ResponseEntity.badRequest().body("Error: El jugador atacante está muerto");
        }

        if (objectiu.getVidaActual() <= 0) {
            return ResponseEntity.badRequest().body("Error: El jugador objetivo está muerto");
        }

        Optional<Carta> cartaBangOpt = atacant.getMa().stream()
                .filter(carta -> carta instanceof CartaUs)
                .filter(carta -> ((CartaUs) carta).getTipusUs() == TipusUs.BANG)
                .findFirst();

        if (cartaBangOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El jugador atacante no tiene una carta BANG en su mano");
        }

        int distancia = calcularDistancia(atacant, objectiu);

        int alcanceArma = 1;
        if (atacant.getArmaEquipada() != null) {
            alcanceArma = atacant.getArmaEquipada().getDistanciaArma();
        }

        if (distancia > alcanceArma) {
            return ResponseEntity.ok("BANG fallido: El objetivo está fuera de alcance (distancia: " + distancia + ", alcance: " + alcanceArma + ")");
        }

        boolean tieneBarril = objectiu.getEquipaments().stream()
                .anyMatch(eq -> eq.getTipus() == TipusEquipament.BARRIL);

        if (tieneBarril) {
            boolean esquivaConBarril = random.nextBoolean();
            if (esquivaConBarril) {
                return ResponseEntity.ok("¡BARRIL! " + objectiu.getNom() + " esquiva el ataque con su BARRIL. No recibe daño.");
            }
        }

        boolean tieneFallaste = objectiu.getMa().stream()
                .filter(carta -> carta instanceof CartaUs)
                .anyMatch(carta -> ((CartaUs) carta).getTipusUs() == TipusUs.FALLASTE);

        if (tieneFallaste) {
            Optional<Carta> cartaFallaste = objectiu.getMa().stream()
                    .filter(carta -> carta instanceof CartaUs)
                    .filter(carta -> ((CartaUs) carta).getTipusUs() == TipusUs.FALLASTE)
                    .findFirst();

            if (cartaFallaste.isPresent()) {
                Carta fallaste = cartaFallaste.get();
                fallaste.setJugadorMa(null);
                objectiu.getMa().remove(fallaste);

                Carta bang = cartaBangOpt.get();
                bang.setJugadorMa(null);
                atacant.getMa().remove(bang);

                cartaRepository.save(fallaste);
                cartaRepository.save(bang);
                jugadorRepository.save(atacant);
                jugadorRepository.save(objectiu);

                return ResponseEntity.ok("¡FALLASTE! " + objectiu.getNom() + " esquiva el BANG con su carta FALLASTE. Ambas cartas se descartan.");
            }
        }

        int nuevaVida = objectiu.getVidaActual() - 1;
        objectiu.setVidaActual(Math.max(0, nuevaVida));

        Carta bang = cartaBangOpt.get();
        bang.setJugadorMa(null);
        atacant.getMa().remove(bang);

        cartaRepository.save(bang);
        jugadorRepository.save(atacant);
        jugadorRepository.save(objectiu);

        String resultado = "¡BANG acertado! " + atacant.getNom() + " dispara a " + objectiu.getNom() +
                          ". " + objectiu.getNom() + " recibe 1 de daño. Vida actual: " + objectiu.getVidaActual();

        if (objectiu.getVidaActual() <= 0) {
            resultado += ". ¡" + objectiu.getNom() + " ha sido eliminado!";
        }

        return ResponseEntity.ok(resultado);
    }


    private int calcularDistancia(Jugador atacant, Jugador objectiu) {
        int distanciaBase = atacant.getDistanciesJugadors1().stream()
                .filter(d -> d.getJugador2().getId() == objectiu.getId())
                .findFirst()
                .map(DistanciesJugadors::getDistancia)
                .orElseGet(() ->
                    atacant.getDistanciesJugadors2().stream()
                        .filter(d -> d.getJugador1().getId() == objectiu.getId())
                        .findFirst()
                        .map(DistanciesJugadors::getDistancia)
                        .orElse(1)
                );

        int modificadorAtacant = atacant.getModificadorDistanciaOff();
        int modificadorObjectiu = objectiu.getModificadorDistanciaDef();

        return Math.max(1, distanciaBase + modificadorAtacant + modificadorObjectiu);
    }

    /**
     * DescartarCarta - Descarta una carta de la mano del jugador
     * @param idJugador ID del jugador
     * @param idCarta ID de la carta a descartar
     * @return El jugador actualizado con su mano
     */
    @DeleteMapping("/descartar/{idJugador}/{idCarta}")
    public ResponseEntity<?> descartarCarta(@PathVariable int idJugador, @PathVariable int idCarta) {
        Optional<Jugador> jugadorOpt = jugadorRepository.findById(idJugador);
        if (jugadorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El jugador con ID " + idJugador + " no existe");
        }

        Jugador jugador = jugadorOpt.get();

        Optional<Carta> cartaOpt = jugador.getMa().stream()
                .filter(carta -> carta.getId() == idCarta)
                .findFirst();

        if (cartaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: La carta con ID " + idCarta + " no está en la mano del jugador");
        }

        Carta carta = cartaOpt.get();

        carta.setJugadorMa(null);
        jugador.getMa().remove(carta);

        cartaRepository.save(carta);
        Jugador jugadorActualizado = jugadorRepository.save(jugador);

        return ResponseEntity.ok(jugadorActualizado);
    }
}

