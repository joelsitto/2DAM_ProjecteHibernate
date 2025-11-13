package com.joelsitto.bang.services;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.TipusColl;
import com.joelsitto.bang.model.enums.TipusEquipament;
import com.joelsitto.bang.model.enums.TipusUs;
import com.joelsitto.bang.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service per gestionar la lògica de negoci de les Partides
 */
@Service
public class PartidaService {

    @Autowired
    private PartidaRepository partidaRepository;
    @Autowired
    private JugadorRepository jugadorRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private CartaRepository cartaRepository;
    @Autowired
    private DistanciesJugadorsRepository distanciesJugadorsRepository;

    /**
     * Obté els jugadors d'una partida
     * @param idPartida ID de la partida
     * @return Llista de jugadors de la partida
     */
    public List<Jugador> llistarJugadorsPartida(int idPartida) {
        Optional<Partida> partidaOpt = partidaRepository.findById(idPartida);

        if (partidaOpt.isEmpty()) {
            return new ArrayList<>();
        }

        return partidaOpt.get().getJugadors();
    }

    /**
     * Inicia una nova partida amb 4 jugadors
     * @return La partida creada amb tots els jugadors i cartes
     */
    @Transactional
    public Partida iniciarPartida() {
        // 1. Crear o obtenir els rols necessaris
        Rol rolSheriff = obtenerOCrearRol("Sheriff");
        Rol rolForajido = obtenerOCrearRol("Forajido");
        Rol rolRenegado = obtenerOCrearRol("Renegado");

        // 2. Crear la partida nova
        Partida partida = new Partida("En curs", new Date(), true);
        partida = partidaRepository.save(partida);

        // 3. Preparar els rols per assignar aleatòriament (1 Sheriff, 2 Forajidos, 1 Renegado)
        List<Rol> rolesDisponibles = new ArrayList<>();
        rolesDisponibles.add(rolSheriff);
        rolesDisponibles.add(rolForajido);
        rolesDisponibles.add(rolForajido);
        rolesDisponibles.add(rolRenegado);
        Collections.shuffle(rolesDisponibles); // Barrejar els rols

        // 4. Crear els 4 jugadors amb noms genèrics i assignar-los rols aleatoris
        List<Jugador> jugadores = new ArrayList<>();
        String[] nombresJugadores = {"Jugador 1", "Jugador 2", "Jugador 3", "Jugador 4"};

        for (int i = 0; i < 4; i++) {
            // Crear jugador amb 4 vides i el rol assignat
            Jugador jugador = new Jugador(nombresJugadores[i], 4, 4, rolesDisponibles.get(i));
            jugador = jugadorRepository.save(jugador);
            jugadores.add(jugador);
            partida.getJugadors().add(jugador);
        }

        // 5. Establir distàncies entre jugadors (disposats en cercle)
        // La distància entre jugadors adjacents és 1
        for (int i = 0; i < jugadores.size(); i++) {
            for (int j = i + 1; j < jugadores.size(); j++) {
                // Calcular distància en cercle: mínim entre anar endavant o endarrere
                int distancia = Math.min(j - i, jugadores.size() - (j - i));
                DistanciesJugadors dist = new DistanciesJugadors(jugadores.get(i), jugadores.get(j), distancia);
                distanciesJugadorsRepository.save(dist);
            }
        }

        // 6. Crear el mazo de cartes
        List<Carta> mazo = crearMazo();

        // 7. Barrejar el mazo
        Collections.shuffle(mazo);

        // 8. Afegir totes les cartes a la pila de robar de la partida
        partida.getPilaRobar().addAll(mazo);

        // 9. Repartir 4 cartes a cada jugador
        int indiceCartaActual = 0;
        for (Jugador jugador : jugadores) {
            for (int i = 0; i < 4; i++) {
                if (indiceCartaActual < mazo.size()) {
                    Carta carta = mazo.get(indiceCartaActual);
                    carta.setJugadorMa(jugador);
                    jugador.getMa().add(carta);
                    partida.getPilaRobar().remove(carta);
                    cartaRepository.save(carta);
                    indiceCartaActual++;
                }
            }
            jugadorRepository.save(jugador);
        }

        // 10. Establir el jugador inicial (comença el Sheriff)
        for (Jugador jugador : jugadores) {
            if (jugador.getRol().getObjectiu().equals("Sheriff")) {
                partida.setJugadorActual(jugador);
                break;
            }
        }

        // 11. Guardar la partida completa
        partida = partidaRepository.save(partida);

        return partida;
    }

    /**
     * Obté un rol de la BD o el crea si no existeix
     * @param objectiu Descripció de l'objectiu del rol
     * @return El rol obtingut o creat
     */
    private Rol obtenerOCrearRol(String objectiu) {
        Optional<Rol> rolOpt = rolRepository.findByObjectiu(objectiu);

        if (rolOpt.isPresent()) {
            return rolOpt.get();
        }

        // Si no existeix, crear-lo
        Rol rol = new Rol(objectiu);
        return rolRepository.save(rol);
    }

    /**
     * Crea el mazo complet de cartes
     * Inclou: cartes d'ús (BANG, FALLASTE, BIRRA), armes i equipaments
     * @return Llista amb totes les cartes del mazo
     */
    private List<Carta> crearMazo() {
        List<Carta> mazo = new ArrayList<>();
        TipusColl[] colls = TipusColl.values();

        // Crear 25 cartes BANG!
        for (int i = 0; i < 25; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaUs cartaBang = new CartaUs("BANG!", "Ataca a un jugador", coll, TipusUs.BANG);
            cartaRepository.save(cartaBang);
            mazo.add(cartaBang);
        }

        // Crear 12 cartes FALLASTE!
        for (int i = 0; i < 12; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaUs cartaFallaste = new CartaUs("Fallaste!", "Evita un BANG!", coll, TipusUs.FALLASTE);
            cartaRepository.save(cartaFallaste);
            mazo.add(cartaFallaste);
        }

        // Crear 6 cartes BIRRA
        for (int i = 0; i < 6; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaUs cartaBirra = new CartaUs("Birra", "Recupera 1 vida", coll, TipusUs.BIRRA);
            cartaRepository.save(cartaBirra);
            mazo.add(cartaBirra);
        }

        // Crear armes amb diferents distàncies (2 de cada tipus)
        String[] nombresArmas = {"Volcanic", "Schofield", "Remington", "Rev Carabine", "Winchester"};
        int[] distanciasArmas = {1, 2, 3, 4, 5};

        for (int i = 0; i < nombresArmas.length; i++) {
            for (int j = 0; j < 2; j++) {
                TipusColl coll = colls[(i + j) % colls.length];
                CartaArma arma = new CartaArma(nombresArmas[i], "Arma de distancia " + distanciasArmas[i],
                                               coll, distanciasArmas[i]);
                cartaRepository.save(arma);
                mazo.add(arma);
            }
        }

        // Crear 3 Mustang (CAVALL - augmenta distància defensiva)
        for (int i = 0; i < 3; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaEquipament mustang = new CartaEquipament("Mustang", "Augmenta la distancia defensiva",
                                                          coll, TipusEquipament.CAVALL, 1);
            cartaRepository.save(mustang);
            mazo.add(mustang);
        }

        // Crear 3 Mira Telescòpica (augmenta distància ofensiva)
        for (int i = 0; i < 3; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaEquipament mira = new CartaEquipament("Mira Telescopica", "Augmenta la distancia ofensiva",
                                                       coll, TipusEquipament.MIRA_TELESCOPICA, 1);
            cartaRepository.save(mira);
            mazo.add(mira);
        }

        // Crear 3 Barrils (pot esquivar BANG!)
        for (int i = 0; i < 3; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaEquipament barril = new CartaEquipament("Barril", "Pot esquivar BANG!",
                                                         coll, TipusEquipament.BARRIL, 0);
            cartaRepository.save(barril);
            mazo.add(barril);
        }

        return mazo;
    }
}

