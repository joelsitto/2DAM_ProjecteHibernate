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

// Service amb la lògica de les partides
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

    // Mètode per obtenir els jugadors d'una partida
    @Transactional
    public List<Jugador> llistarJugadorsPartida(int idPartida) {
        // Buscar la partida
        Optional<Partida> partidaOpt = partidaRepository.findById(idPartida);

        // Si no existeix, llançar error
        if (partidaOpt.isEmpty()) {
            throw new RuntimeException("Error: La partida amb ID " + idPartida + " no existeix");
        }

        // Obtenir la partida i forçar la càrrega dels jugadors
        Partida partida = partidaOpt.get();
        List<Jugador> jugadors = partida.getJugadors();

        // Forçar la inicialització de les col·leccions lazy
        if (!jugadors.isEmpty()) {
            jugadors.size(); // Això força Hibernate a carregar els jugadors
        }

        // Retornar els jugadors
        return jugadors;
    }

    // Mètode per crear una partida nova amb tot el necessari
    @Transactional
    public Partida iniciarPartida() {
        // Pas 1: Crear els rols (Sheriff, Forajido, Renegado)
        Rol rolSheriff = obtenerOCrearRol("Sheriff");
        Rol rolForajido = obtenerOCrearRol("Forajido");
        Rol rolRenegado = obtenerOCrearRol("Renegado");

        // Pas 2: Crear la partida i guardar-la
        Partida partida = new Partida("En curs", new Date(), true);
        partida = partidaRepository.save(partida);

        // Pas 3: Preparar els rols (1 Sheriff, 2 Forajidos, 1 Renegado) i barrejar-los
        List<Rol> rolesDisponibles = new ArrayList<>();
        rolesDisponibles.add(rolSheriff);
        rolesDisponibles.add(rolForajido);
        rolesDisponibles.add(rolForajido);
        rolesDisponibles.add(rolRenegado);
        Collections.shuffle(rolesDisponibles);

        // Pas 4: Crear 4 jugadors i assignar-los un rol aleatori
        List<Jugador> jugadores = new ArrayList<>();
        String[] nombresJugadores = {"Jugador 1", "Jugador 2", "Jugador 3", "Jugador 4"};

        for (int i = 0; i < 4; i++) {
            // Crear cada jugador amb 4 vides
            Jugador jugador = new Jugador(nombresJugadores[i], 4, 4, rolesDisponibles.get(i));
            jugador = jugadorRepository.save(jugador);
            jugadores.add(jugador);
            partida.getJugadors().add(jugador);
        }

        // Pas 5: Calcular les distàncies entre jugadors (estan en cercle)
        for (int i = 0; i < jugadores.size(); i++) {
            for (int j = i + 1; j < jugadores.size(); j++) {
                // La distància és el mínim entre anar endavant o endarrere
                int distancia = Math.min(j - i, jugadores.size() - (j - i));
                DistanciesJugadors dist = new DistanciesJugadors(jugadores.get(i), jugadores.get(j), distancia);
                distanciesJugadorsRepository.save(dist);
            }
        }

        // Pas 6: Crear totes les cartes del joc
        List<Carta> mazo = crearMazo();

        // Pas 7: Barrejar les cartes
        Collections.shuffle(mazo);

        // Pas 8: Posar totes les cartes a la pila de robar
        partida.getPilaRobar().addAll(mazo);

        // Pas 9: Repartir 4 cartes a cada jugador
        int indiceCartaActual = 0;
        for (Jugador jugador : jugadores) {
            for (int i = 0; i < 4; i++) {
                if (indiceCartaActual < mazo.size()) {
                    Carta carta = mazo.get(indiceCartaActual);
                    // Assignar la carta al jugador
                    carta.setJugadorMa(jugador);
                    jugador.getMa().add(carta);
                    partida.getPilaRobar().remove(carta);
                    cartaRepository.save(carta);
                    indiceCartaActual++;
                }
            }
            jugadorRepository.save(jugador);
        }

        // Pas 10: El Sheriff és qui comença
        for (Jugador jugador : jugadores) {
            if (jugador.getRol().getObjectiu().equals("Sheriff")) {
                partida.setJugadorActual(jugador);
                break;
            }
        }

        // Pas 11: Guardar la partida amb tot
        partida = partidaRepository.save(partida);

        return partida;
    }

    // Buscar un rol, si no existeix el crea
    private Rol obtenerOCrearRol(String objectiu) {
        Optional<Rol> rolOpt = rolRepository.findByObjectiu(objectiu);

        if (rolOpt.isPresent()) {
            return rolOpt.get();
        }

        // Crear el rol si no existeix
        Rol rol = new Rol(objectiu);
        return rolRepository.save(rol);
    }

    // Crear totes les cartes del joc
    private List<Carta> crearMazo() {
        List<Carta> mazo = new ArrayList<>();
        TipusColl[] colls = TipusColl.values();

        // 25 cartes BANG
        for (int i = 0; i < 25; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaUs cartaBang = new CartaUs("BANG!", "Ataca a un jugador", coll, TipusUs.BANG);
            cartaRepository.save(cartaBang);
            mazo.add(cartaBang);
        }

        // 12 cartes FALLASTE
        for (int i = 0; i < 12; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaUs cartaFallaste = new CartaUs("Fallaste!", "Evita un BANG!", coll, TipusUs.FALLASTE);
            cartaRepository.save(cartaFallaste);
            mazo.add(cartaFallaste);
        }

        // 6 cartes BIRRA
        for (int i = 0; i < 6; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaUs cartaBirra = new CartaUs("Birra", "Recupera 1 vida", coll, TipusUs.BIRRA);
            cartaRepository.save(cartaBirra);
            mazo.add(cartaBirra);
        }

        // Crear armes (2 de cada tipus)
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

        // 3 Mustang (augmenta distància defensiva)
        for (int i = 0; i < 3; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaEquipament mustang = new CartaEquipament("Mustang", "Augmenta la distancia defensiva",
                                                          coll, TipusEquipament.CAVALL, 1);
            cartaRepository.save(mustang);
            mazo.add(mustang);
        }

        // 3 Mira Telescòpica (augmenta distància ofensiva)
        for (int i = 0; i < 3; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaEquipament mira = new CartaEquipament("Mira Telescopica", "Augmenta la distancia ofensiva",
                                                       coll, TipusEquipament.MIRA_TELESCOPICA, 1);
            cartaRepository.save(mira);
            mazo.add(mira);
        }

        // 3 Barrils (pot esquivar BANG)
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

