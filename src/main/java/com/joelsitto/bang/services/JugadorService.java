package com.joelsitto.bang.services;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.TipusEquipament;
import com.joelsitto.bang.model.enums.TipusUs;
import com.joelsitto.bang.repositories.CartaRepository;
import com.joelsitto.bang.repositories.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

// Service para la lógica de los jugadores
@Service
public class JugadorService {

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private CartaRepository cartaRepository;

    private final Random random = new Random();

    // Método para que un jugador ataque a otro con BANG
    @Transactional
    public String usarBANG(int idJugadorAtacant, int idJugadorObjectiu) {
        // Buscar los jugadores en la base de datos
        Optional<Jugador> atacantOpt = jugadorRepository.findById(idJugadorAtacant);
        Optional<Jugador> objectiuOpt = jugadorRepository.findById(idJugadorObjectiu);

        if (atacantOpt.isEmpty()) {
            return "Error: El jugador atacante con ID " + idJugadorAtacant + " no existe";
        }
        if (objectiuOpt.isEmpty()) {
            return "Error: El jugador objetivo con ID " + idJugadorObjectiu + " no existe";
        }

        Jugador atacant = atacantOpt.get();
        Jugador objectiu = objectiuOpt.get();

        // Validar que els jugadors estan vius
        if (atacant.getVidaActual() <= 0) {
            return "Error: El jugador atacante está muerto";
        }

        if (objectiu.getVidaActual() <= 0) {
            return "Error: El jugador objetivo está muerto";
        }

        // Verificar que l'atacant té una carta BANG a la mà
        Optional<Carta> cartaBangOpt = atacant.getMa().stream()
                .filter(carta -> carta instanceof CartaUs)
                .filter(carta -> ((CartaUs) carta).getTipusUs() == TipusUs.BANG)
                .findFirst();

        if (cartaBangOpt.isEmpty()) {
            return "Error: El jugador atacante no tiene una carta BANG en su mano";
        }

        // Calcular distància i alcance
        int distancia = calcularDistancia(atacant, objectiu);

        int alcanceArma = 1;
        if (atacant.getArmaEquipada() != null) {
            alcanceArma = atacant.getArmaEquipada().getDistanciaArma();
        }

        if (distancia > alcanceArma) {
            return "BANG fallido: El objetivo está fuera de alcance (distancia: " + distancia + ", alcance: " + alcanceArma + ")";
        }

        // Comprovar si l'objectiu té BARRIL equipat
        boolean tieneBarril = objectiu.getEquipaments().stream()
                .anyMatch(eq -> eq.getTipus() == TipusEquipament.BARRIL);

        if (tieneBarril) {
            boolean esquivaConBarril = random.nextBoolean();
            if (esquivaConBarril) {
                return "¡BARRIL! " + objectiu.getNom() + " esquiva el ataque con su BARRIL. No recibe daño.";
            }
        }

        // Comprovar si l'objectiu té FALLASTE a la mà
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

                return "¡FALLASTE! " + objectiu.getNom() + " esquiva el BANG con su carta FALLASTE. Ambas cartas se descartan.";
            }
        }

        // L'atac té èxit - restar vida a l'objectiu
        int nuevaVida = objectiu.getVidaActual() - 1;
        objectiu.setVidaActual(Math.max(0, nuevaVida));

        // Descartar la carta BANG de l'atacant
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

        return resultado;
    }

    // Método para calcular la distancia entre dos jugadores
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

    // Método para descartar una carta de la mano del jugador
    @Transactional
    public Map<String, Object> descartarCarta(int idJugador, int idCarta) {
        Optional<Jugador> jugadorOpt = jugadorRepository.findById(idJugador);
        if (jugadorOpt.isEmpty()) {
            throw new RuntimeException("Error: El jugador con ID " + idJugador + " no existe");
        }

        Jugador jugador = jugadorOpt.get();

        Optional<Carta> cartaOpt = jugador.getMa().stream()
                .filter(carta -> carta.getId() == idCarta)
                .findFirst();

        if (cartaOpt.isEmpty()) {
            throw new RuntimeException("Error: La carta con ID " + idCarta + " no está en la mano del jugador");
        }

        Carta carta = cartaOpt.get();

        // Quitar la carta de la mano del jugador
        carta.setJugadorMa(null);
        jugador.getMa().remove(carta);

        cartaRepository.save(carta);
        jugadorRepository.save(jugador);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("mano", jugador.getMa());

        return resultado;
    }
}

