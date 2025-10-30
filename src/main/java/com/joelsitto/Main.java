package com.joelsitto;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import com.joelsitto.bang.util.HibernateUtil;
import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.*;
import com.joelsitto.bang.dao.*;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        System.out.println("=== INICIALIITZANT BASE DE DADES ===\n");

        // Crear dades inicials
        crearDadesInicials(sessionFactory);

        // Provar els DAOs
        provarDAOs(sessionFactory);

        HibernateUtil.shutdown();
    }

    private static void crearDadesInicials(SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        try {
            // Crear roles
            Rol sheriff = new Rol();
            sheriff.setObjectiu("Eliminar a tots els bandits i renegats");
            session.persist(sheriff);

            Rol bandit = new Rol();
            bandit.setObjectiu("Eliminar al Sheriff");
            session.persist(bandit);

            Rol renegat = new Rol();
            renegat.setObjectiu("Ser l'ultim jugador en peu");
            session.persist(renegat);

            // Crear cartes per la pila
            for (int i = 1; i <= 10; i++) {
                CartaUs bang = new CartaUs();
                bang.setNom_carta("Bang!");
                bang.setDescripcio_carta("Dispara a un jugador");
                bang.setTipusUs(TipusUs.BANG);
                session.persist(bang);
            }

            for (int i = 1; i <= 8; i++) {
                CartaUs fallaste = new CartaUs();
                fallaste.setNom_carta("Fallaste!");
                fallaste.setDescripcio_carta("Esquiva un Bang!");
                fallaste.setTipusUs(TipusUs.FALLASTE);
                session.persist(fallaste);
            }

            for (int i = 1; i <= 6; i++) {
                CartaUs birra = new CartaUs();
                birra.setNom_carta("Birra");
                birra.setDescripcio_carta("Recupera 1 punt de vida");
                birra.setTipusUs(TipusUs.BIRRA);
                session.persist(birra);
            }

            // Crear armes
            CartaArma colt = new CartaArma();
            colt.setNom_carta("Colt .45");
            colt.setDescripcio_carta("Arma basica");
            colt.setDistanciaArma(1);
            session.persist(colt);

            CartaArma winchester = new CartaArma();
            winchester.setNom_carta("Winchester");
            winchester.setDescripcio_carta("Rifle de llarg abast");
            winchester.setDistanciaArma(5);
            session.persist(winchester);

            // Crear equipaments
            CartaEquipament barril = new CartaEquipament();
            barril.setNom_carta("Barril");
            barril.setDescripcio_carta("Permet esquivar");
            barril.setTipus(TipusEquipament.BARRIL);
            barril.setModificadorDistancia(0);
            session.persist(barril);

            CartaEquipament mustang = new CartaEquipament();
            mustang.setNom_carta("Mustang");
            mustang.setDescripcio_carta("Augmenta la distancia");
            mustang.setTipus(TipusEquipament.CAVALL);
            mustang.setModificadorDistancia(1);
            session.persist(mustang);

            tx.commit();
            System.out.println("Dades inicials creades correctament!\n");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    private static void provarDAOs(SessionFactory sessionFactory) {
        IPartidaDAO partidaDAO = new PartidaDAOImpl();
        IJugadorDAO jugadorDAO = new JugadorDAOImpl();

        // Obtenir roles
        Session session = sessionFactory.openSession();
        List<Rol> roles = session.createQuery("FROM Rol", Rol.class).getResultList();
        session.close();

        // Iniciar partida
        System.out.println("\n=== INICIANT PARTIDA ===");
        List<String> noms = Arrays.asList("Bart Cassidy", "Jesse Jones", "Paul Regret");
        Partida partida = partidaDAO.iniciarPartida(sessionFactory, noms, roles);

        // Llistar jugadors
        System.out.println("\n=== LLISTANT JUGADORS ===");
        List<Jugador> jugadors = partidaDAO.llistarJugadorsPartida(sessionFactory, partida.getId());

        if (jugadors.size() >= 2) {
            int jugador1Id = jugadors.get(0).getId();
            int jugador2Id = jugadors.get(1).getId();

            // Donar cartes als jugadors
            System.out.println("\n=== ROBANT CARTES ===");
            for (int i = 0; i < 3; i++) {
                jugadorDAO.robarCarta(sessionFactory, partida.getId(), jugador1Id);
                jugadorDAO.robarCarta(sessionFactory, partida.getId(), jugador2Id);
            }

            // Mostrar ma
            System.out.println("\n=== MOSTRANT MA ===");
            jugadorDAO.mostrarMaJugador(sessionFactory, jugador1Id);

            // Mostrar estat partida
            System.out.println("\n=== ESTAT PARTIDA ===");
            partidaDAO.mostrarPartida(sessionFactory, partida.getId());
        }

        System.out.println("\n=== TEST FINALITZAT ===");
    }
}

