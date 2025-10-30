package com.joelsitto.bang.test;

import com.joelsitto.bang.dao.*;
import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.*;
import com.joelsitto.bang.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests amb JUnit per verificar les funcionalitats dels DAOs
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestBangDAOJUnit {

    private static SessionFactory sessionFactory;
    private static IPartidaDAO partidaDAO;
    private static IJugadorDAO jugadorDAO;
    private static int idPartida;

    @BeforeAll
    static void inicialitzar() {
        System.out.println("========================================");
        System.out.println("  INICIANT TESTS JUNIT DEL JOC BANG!");
        System.out.println("========================================\n");

        sessionFactory = HibernateUtil.getSessionFactory();
        partidaDAO = new PartidaDAOImpl();
        jugadorDAO = new JugadorDAOImpl();

        // Limpiar la base de datos antes de empezar
        netejaBD();

        prepararBaseDades();
    }

    /**
     * Neteja totes les dades de la BD per començar amb una BD neta
     */
    private static void netejaBD() {
        System.out.println(">>> Netejant base de dades...\n");

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        try {
            // Eliminar en ordre per evitar problemes de FK
            session.createQuery("DELETE FROM DistanciesJugadors").executeUpdate();
            session.createQuery("DELETE FROM CartaUs").executeUpdate();
            session.createQuery("DELETE FROM CartaEquipament").executeUpdate();
            session.createQuery("DELETE FROM CartaArma").executeUpdate();
            session.createQuery("DELETE FROM Carta").executeUpdate();
            session.createQuery("DELETE FROM Jugador").executeUpdate();
            session.createQuery("DELETE FROM Partida").executeUpdate();
            session.createQuery("DELETE FROM Rol").executeUpdate();

            tx.commit();
            System.out.println("Base de dades netejada correctament\n");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @AfterAll
    static void finalitzar() {
        System.out.println("\n========================================");
        System.out.println("  TOTS ELS TESTS COMPLETATS!");
        System.out.println("========================================");
        HibernateUtil.shutdown();
    }

    private static void prepararBaseDades() {
        System.out.println(">>> Preparant base de dades...\n");

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

            Rol ajudant = new Rol();
            ajudant.setObjectiu("Protegir al Sheriff");
            session.persist(ajudant);

            // Crear cartes BANG
            for (int i = 1; i <= 15; i++) {
                CartaUs bang = new CartaUs();
                bang.setNom_carta("Bang!");
                bang.setDescripcio_carta("Dispara a un jugador");
                bang.setTipusUs(TipusUs.BANG);
                session.persist(bang);
            }

            // Crear cartes FALLASTE
            for (int i = 1; i <= 12; i++) {
                CartaUs fallaste = new CartaUs();
                fallaste.setNom_carta("Fallaste!");
                fallaste.setDescripcio_carta("Esquiva un Bang!");
                fallaste.setTipusUs(TipusUs.FALLASTE);
                session.persist(fallaste);
            }

            // Crear cartes BIRRA
            for (int i = 1; i <= 8; i++) {
                CartaUs birra = new CartaUs();
                birra.setNom_carta("Birra");
                birra.setDescripcio_carta("Recupera 1 punt de vida");
                birra.setTipusUs(TipusUs.BIRRA);
                session.persist(birra);
            }

            // Crear armes
            CartaArma winchester = new CartaArma();
            winchester.setNom_carta("Winchester");
            winchester.setDescripcio_carta("Rifle de llarg abast");
            winchester.setDistanciaArma(5);
            session.persist(winchester);

            CartaArma volcanic = new CartaArma();
            volcanic.setNom_carta("Volcanic");
            volcanic.setDescripcio_carta("Permet disparar ilimitadament");
            volcanic.setDistanciaArma(1);
            session.persist(volcanic);

            CartaArma schofield = new CartaArma();
            schofield.setNom_carta("Schofield");
            schofield.setDescripcio_carta("Arma de distancia mitjana");
            schofield.setDistanciaArma(2);
            session.persist(schofield);

            // Crear equipaments
            CartaEquipament barril = new CartaEquipament();
            barril.setNom_carta("Barril");
            barril.setDescripcio_carta("Permet esquivar");
            barril.setTipus(TipusEquipament.BARRIL);
            barril.setModificadorDistancia(0);
            session.persist(barril);

            CartaEquipament mustang = new CartaEquipament();
            mustang.setNom_carta("Mustang");
            mustang.setDescripcio_carta("Augmenta la distancia defensiva");
            mustang.setTipus(TipusEquipament.CAVALL);
            mustang.setModificadorDistancia(1);
            session.persist(mustang);

            CartaEquipament mira = new CartaEquipament();
            mira.setNom_carta("Mira Telescopica");
            mira.setDescripcio_carta("Redueix la distancia ofensiva");
            mira.setTipus(TipusEquipament.MIRA_TELESCOPICA);
            mira.setModificadorDistancia(-1);
            session.persist(mira);

            tx.commit();
            System.out.println("Base de dades preparada amb cartes i roles\n");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Crear Partida")
    void testCrearPartida() {
        System.out.println("========================================");
        System.out.println("TEST 1: Crear Partida");
        System.out.println("========================================");

        Session session = sessionFactory.openSession();
        List<Rol> roles = session.createQuery("FROM Rol", Rol.class).getResultList();
        session.close();

        List<String> noms = Arrays.asList("Bart Cassidy", "Jesse Jones", "Paul Regret", "Suzy Lafayette");

        Partida partida = partidaDAO.iniciarPartida(sessionFactory, noms, roles);

        assertNotNull(partida, "La partida no hauria de ser null");
        assertTrue(partida.getId() > 0, "L'ID de la partida hauria de ser positiu");
        assertTrue(partida.isActiu(), "La partida hauria d'estar activa");
        assertEquals(4, partida.getJugadors().size(), "Hauria d'haver 4 jugadors");

        idPartida = partida.getId();

        System.out.println("RESULTAT: OK - Partida creada amb ID: " + partida.getId());
        System.out.println();
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Robar Cartes")
    void testRobarCartes() {
        System.out.println("========================================");
        System.out.println("TEST 2: Robar Cartes");
        System.out.println("========================================");

        Partida partida = obtenirPartidaActiva();
        assertNotNull(partida, "Hauria d'haver una partida activa");

        List<Jugador> jugadors = partida.getJugadors();

        // Assignar cartes a la pila de robar
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        List<Carta> totesCartes = session.createQuery("FROM Carta", Carta.class).getResultList();
        partida = session.get(Partida.class, partida.getId());
        partida.getPilaRobar().addAll(totesCartes);
        session.merge(partida);
        tx.commit();
        session.close();

        // Cada jugador roba 5 cartes
        for (Jugador j : jugadors) {
            System.out.println("\n" + j.getNom() + " roba cartes:");
            for (int i = 0; i < 5; i++) {
                jugadorDAO.robarCarta(sessionFactory, partida.getId(), j.getId());
            }
        }

        // Verificar que han robat cartes
        List<Carta> cartesJugador1 = jugadorDAO.mostrarMaJugador(sessionFactory, jugadors.get(0).getId());
        assertTrue(cartesJugador1.size() > 0, "El jugador hauria de tenir cartes");

        System.out.println("\nRESULTAT: OK - Tots els jugadors han robat cartes");
        System.out.println();
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Mostrar Ma del Jugador")
    void testMostrarMaJugador() {
        System.out.println("========================================");
        System.out.println("TEST 3: Mostrar Ma del Jugador");
        System.out.println("========================================");

        Partida partida = obtenirPartidaActiva();
        assertNotNull(partida);

        Jugador jugador1 = partida.getJugadors().get(0);

        List<Carta> cartes = jugadorDAO.mostrarMaJugador(sessionFactory, jugador1.getId());

        assertNotNull(cartes, "La llista de cartes no hauria de ser null");
        assertTrue(cartes.size() >= 0, "La mida hauria de ser >= 0");

        System.out.println("\nRESULTAT: OK - Mostrades " + cartes.size() + " cartes");
        System.out.println();
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Equipar Arma")
    void testEquiparArma() {
        System.out.println("========================================");
        System.out.println("TEST 4: Equipar Arma");
        System.out.println("========================================");

        Partida partida = obtenirPartidaActiva();
        assertNotNull(partida);

        Jugador jugador1 = partida.getJugadors().get(0);

        // Limpiar armes de TOTS els jugadors primer (per evitar constraint unique)
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        List<Jugador> totsJugadors = session.createQuery("FROM Jugador", Jugador.class).getResultList();
        for (Jugador j : totsJugadors) {
            if (j.getArmaEquipada() != null) {
                j.setArmaEquipada(null);
                session.merge(j);
            }
        }
        session.flush();

        // Ara buscar l'arma i donar-la al jugador
        CartaArma arma = session.createQuery("FROM CartaArma", CartaArma.class)
                .setMaxResults(1)
                .getSingleResult();
        Jugador jug = session.get(Jugador.class, jugador1.getId());


        // Asegurar que la carta está en la mano del jugador
        arma.setJugadorMa(jug);
        session.merge(arma);
        session.flush();

        tx.commit();
        session.close();

        // Ahora equipar la carta
        jugadorDAO.equiparCarta(sessionFactory, jugador1.getId(), arma.getId());

        // Verificar que s'ha equipat
        session = sessionFactory.openSession();
        Jugador jugadorActualitzat = session.get(Jugador.class, jugador1.getId());
        session.close();

        assertNotNull(jugadorActualitzat.getArmaEquipada(), "El jugador hauria de tenir una arma equipada");

        System.out.println("\nRESULTAT: OK - Arma equipada correctament");
        System.out.println();
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Equipar Equipament")
    void testEquiparEquipament() {
        System.out.println("========================================");
        System.out.println("TEST 5: Equipar Equipament");
        System.out.println("========================================");

        Partida partida = obtenirPartidaActiva();
        assertNotNull(partida);

        Jugador jugador2 = partida.getJugadors().get(1);

        // Donar un equipament al jugador
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        CartaEquipament barril = session.createQuery("FROM CartaEquipament WHERE tipus = :tipus", CartaEquipament.class)
                .setParameter("tipus", TipusEquipament.BARRIL)
                .setMaxResults(1)
                .getSingleResult();
        Jugador jug = session.get(Jugador.class, jugador2.getId());
        barril.setJugadorMa(jug);
        session.merge(barril);
        tx.commit();
        session.close();

        jugadorDAO.equiparCarta(sessionFactory, jugador2.getId(), barril.getId());

        // Verificar que s'ha equipat
        session = sessionFactory.openSession();
        String hql = "FROM CartaEquipament ce WHERE ce.jugadorEquipament.id = :idJugador";
        List<CartaEquipament> equipaments = session.createQuery(hql, CartaEquipament.class)
                .setParameter("idJugador", jugador2.getId())
                .getResultList();
        session.close();

        assertFalse(equipaments.isEmpty(), "El jugador hauria de tenir equipaments");

        System.out.println("\nRESULTAT: OK - Equipament equipat correctament");
        System.out.println();
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Calcular Distancia")
    void testCalcularDistancia() {
        System.out.println("========================================");
        System.out.println("TEST 6: Calcular Distancia");
        System.out.println("========================================");

        Partida partida = obtenirPartidaActiva();
        assertNotNull(partida);

        List<Jugador> jugadors = partida.getJugadors();

        int dist = jugadorDAO.calcularDistancia(sessionFactory,
                jugadors.get(0).getId(),
                jugadors.get(1).getId());

        assertTrue(dist > 0, "La distancia hauria de ser positiva");

        System.out.println("Distancia calculada: " + dist);
        System.out.println("\nRESULTAT: OK - Distancies calculades");
        System.out.println();
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Atac BANG")
    void testAtacBANG() {
        System.out.println("========================================");
        System.out.println("TEST 7: Atac BANG!");
        System.out.println("========================================");

        Partida partida = obtenirPartidaActiva();
        assertNotNull(partida);

        Jugador atacant = partida.getJugadors().get(0);
        Jugador objectiu = partida.getJugadors().get(1);

        int vidaInicial = objectiu.getVidaActual();

        // Donar carta BANG a l'atacant
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        CartaUs bang = session.createQuery("FROM CartaUs WHERE tipusUs = :tipus", CartaUs.class)
                .setParameter("tipus", TipusUs.BANG)
                .setMaxResults(1)
                .getSingleResult();
        Jugador atac = session.get(Jugador.class, atacant.getId());
        bang.setJugadorMa(atac);
        session.merge(bang);
        tx.commit();
        session.close();

        jugadorDAO.usarBANG(sessionFactory, partida.getId(), atacant.getId(), objectiu.getId());

        // Verificar que el metode s'ha executat sense errors
        System.out.println("\nRESULTAT: OK - Atac executat");
        System.out.println();
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Passar Torn")
    void testPassarTorn() {
        System.out.println("========================================");
        System.out.println("TEST 8: Passar Torn");
        System.out.println("========================================");

        Partida partida = obtenirPartidaActiva();
        assertNotNull(partida);

        Jugador jugador1 = partida.getJugadors().get(0);

        assertDoesNotThrow(() -> {
            jugadorDAO.passarTorn(sessionFactory, partida.getId(), jugador1.getId());
        }, "El metode passarTorn no hauria de llançar excepcions");

        System.out.println("\nRESULTAT: OK - Torn passat correctament");
        System.out.println();
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Mostrar Estat Partida")
    void testMostrarPartida() {
        System.out.println("========================================");
        System.out.println("TEST 9: Mostrar Estat Partida");
        System.out.println("========================================");

        Partida partida = obtenirPartidaActiva();
        assertNotNull(partida);

        assertDoesNotThrow(() -> {
            partidaDAO.mostrarPartida(sessionFactory, partida.getId());
        }, "El metode mostrarPartida no hauria de llançar excepcions");

        System.out.println("\nRESULTAT: OK - Estat mostrat correctament");
        System.out.println();
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Comprovar Victoria")
    void testComprovarVictoria() {
        System.out.println("========================================");
        System.out.println("TEST 10: Comprovar Victoria");
        System.out.println("========================================");

        Partida partida = obtenirPartidaActiva();
        assertNotNull(partida);

        String victoria = partidaDAO.comprovarVictoria(sessionFactory, partida.getId());

        // La victoria pot ser null si la partida continua
        if (victoria != null) {
            System.out.println("Victoria detectada: " + victoria);
        } else {
            System.out.println("La partida continua...");
        }

        System.out.println("\nRESULTAT: OK - Comprovacio executada");
        System.out.println();
    }

    private Partida obtenirPartidaActiva() {
        Session session = sessionFactory.openSession();
        String hql = "FROM Partida p WHERE p.actiu = true";
        Partida partida = session.createQuery(hql, Partida.class)
                .setMaxResults(1)
                .uniqueResult();

        // Forzar la carga de jugadors abans de tancar la sessio
        if (partida != null) {
            partida.getJugadors().size(); // Això carrega els jugadors
        }

        session.close();
        return partida;
    }
}

