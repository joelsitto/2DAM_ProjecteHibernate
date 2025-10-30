package com.joelsitto;

import com.joelsitto.bang.dao.*;
import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.*;
import com.joelsitto.bang.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.*;

/**
 * Main interactiu per jugar al BANG!
 */
public class MainJuego {

    private static SessionFactory sessionFactory;
    private static IPartidaDAO partidaDAO;
    private static IJugadorDAO jugadorDAO;
    private static Scanner scanner;
    private static Partida partidaActual;
    private static int tornActual = 0;

    public static void main(String[] args) {
        sessionFactory = HibernateUtil.getSessionFactory();
        partidaDAO = new PartidaDAOImpl();
        jugadorDAO = new JugadorDAOImpl();
        scanner = new Scanner(System.in);

        try {
            mostrarBenvinguda();
            prepararJoc();
            iniciarPartida();
            jugar();

        } catch (Exception e) {
            System.err.println("Error en el joc: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
            HibernateUtil.shutdown();
        }
    }

    private static void mostrarBenvinguda() {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║         BENVINGUT AL JOC BANG!            ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
    }

    private static void prepararJoc() {
        System.out.println(">>> Preparant el joc...\n");

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
            for (int i = 1; i <= 20; i++) {
                CartaUs bang = new CartaUs();
                bang.setNom_carta("Bang!");
                bang.setDescripcio_carta("Dispara a un jugador");
                bang.setTipusUs(TipusUs.BANG);
                session.persist(bang);
            }

            // Crear cartes FALLASTE
            for (int i = 1; i <= 15; i++) {
                CartaUs fallaste = new CartaUs();
                fallaste.setNom_carta("Fallaste!");
                fallaste.setDescripcio_carta("Esquiva un Bang!");
                fallaste.setTipusUs(TipusUs.FALLASTE);
                session.persist(fallaste);
            }

            // Crear cartes BIRRA
            for (int i = 1; i <= 10; i++) {
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
            volcanic.setDescripcio_carta("Permet disparar diverses vegades");
            volcanic.setDistanciaArma(1);
            session.persist(volcanic);

            CartaArma schofield = new CartaArma();
            schofield.setNom_carta("Schofield");
            schofield.setDescripcio_carta("Arma de distancia mitjana");
            schofield.setDistanciaArma(2);
            session.persist(schofield);

            // Crear equipaments
            for (int i = 0; i < 3; i++) {
                CartaEquipament barril = new CartaEquipament();
                barril.setNom_carta("Barril");
                barril.setDescripcio_carta("Permet esquivar amb sort");
                barril.setTipus(TipusEquipament.BARRIL);
                barril.setModificadorDistancia(0);
                session.persist(barril);
            }

            for (int i = 0; i < 2; i++) {
                CartaEquipament mustang = new CartaEquipament();
                mustang.setNom_carta("Mustang");
                mustang.setDescripcio_carta("Els altres jugadors et veuen a +1 distancia");
                mustang.setTipus(TipusEquipament.CAVALL);
                mustang.setModificadorDistancia(1);
                session.persist(mustang);
            }

            tx.commit();
            System.out.println("Joc preparat!\n");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    private static void iniciarPartida() {
        System.out.print("Quants jugadors? (4-7): ");
        int numJugadors = scanner.nextInt();
        scanner.nextLine();

        if (numJugadors < 4 || numJugadors > 7) {
            System.out.println("El nombre de jugadors ha de ser entre 4 i 7. Usant 4 per defecte.");
            numJugadors = 4;
        }

        List<String> noms = new ArrayList<>();
        for (int i = 1; i <= numJugadors; i++) {
            System.out.print("Nom del jugador " + i + ": ");
            noms.add(scanner.nextLine());
        }

        // Obtenir roles
        Session session = sessionFactory.openSession();
        List<Rol> totsRoles = session.createQuery("FROM Rol", Rol.class).getResultList();
        session.close();

        // Assignar roles segons el nombre de jugadors
        List<Rol> rolesPartida = new ArrayList<>();
        rolesPartida.add(totsRoles.get(0)); // Sheriff (sempre)

        if (numJugadors >= 4) {
            rolesPartida.add(totsRoles.get(1)); // Bandit
            rolesPartida.add(totsRoles.get(2)); // Renegat
        }
        if (numJugadors >= 5) {
            rolesPartida.add(totsRoles.get(1)); // Bandit
        }
        if (numJugadors >= 6) {
            rolesPartida.add(totsRoles.get(3)); // Ajudant
        }
        if (numJugadors >= 7) {
            rolesPartida.add(totsRoles.get(1)); // Bandit
        }

        // Crear partida
        partidaActual = partidaDAO.iniciarPartida(sessionFactory, noms, rolesPartida);

        // Assignar cartes a la pila de robar
        session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        List<Carta> totesCartes = session.createQuery("FROM Carta", Carta.class).getResultList();
        Collections.shuffle(totesCartes);
        partidaActual = session.get(Partida.class, partidaActual.getId());
        partidaActual.getPilaRobar().addAll(totesCartes);
        session.merge(partidaActual);
        tx.commit();
        session.close();

        // Repartir 4 cartes a cada jugador
        System.out.println("\n>>> Repartint cartes inicials...");
        for (Jugador j : obtenirJugadorsVius()) {
            for (int i = 0; i < 4; i++) {
                jugadorDAO.robarCarta(sessionFactory, partidaActual.getId(), j.getId());
            }
            System.out.println(j.getNom() + " rep 4 cartes");
        }

        System.out.println("\n>>> PARTIDA INICIADA! <<<\n");
        mostrarRoles();
    }

    private static void mostrarRoles() {
        System.out.println("\n=== ROLS DELS JUGADORS ===");
        for (Jugador j : obtenirJugadorsVius()) {
            String rolNom = j.getRol().getObjectiu().contains("Eliminar a tots") ? "SHERIFF" :
                           j.getRol().getObjectiu().contains("Eliminar al Sheriff") ? "[OCULT - Bandit]" :
                           j.getRol().getObjectiu().contains("ultim jugador") ? "[OCULT - Renegat]" :
                           "[OCULT - Ajudant]";

            if (j.getRol().getObjectiu().contains("Eliminar a tots")) {
                System.out.println(j.getNom() + " - " + rolNom);
            } else {
                System.out.println(j.getNom() + " - Rol ocult");
            }
        }
        System.out.println();
    }

    private static void jugar() {
        List<Jugador> jugadors = obtenirJugadorsVius();

        while (true) {
            Jugador jugadorActiu = jugadors.get(tornActual % jugadors.size());

            // Verificar si el jugador esta viu
            Session session = sessionFactory.openSession();
            Jugador jug = session.get(Jugador.class, jugadorActiu.getId());
            session.close();

            if (jug.getVidaActual() <= 0) {
                tornActual++;
                continue;
            }

            System.out.println("\n╔════════════════════════════════════════════╗");
            System.out.println("║  TORN DE: " + jug.getNom() + " (" + jug.getVidaActual() + " vides)");
            System.out.println("╚════════════════════════════════════════════╝");

            // FASE 1: Robar 2 cartes
            System.out.println("\n--- FASE 1: Robar cartes ---");
            System.out.println("Robant 2 cartes...");
            jugadorDAO.robarCarta(sessionFactory, partidaActual.getId(), jug.getId());
            jugadorDAO.robarCarta(sessionFactory, partidaActual.getId(), jug.getId());

            // FASE 2: Jugar cartes
            boolean continuarTorn = true;
            while (continuarTorn) {
                System.out.println("\n--- FASE 2: Jugar cartes ---");
                jugadorDAO.mostrarMaJugador(sessionFactory, jug.getId());
                partidaDAO.mostrarPartida(sessionFactory, partidaActual.getId());

                System.out.println("\nQue vols fer?");
                System.out.println("1. Equipar arma");
                System.out.println("2. Equipar equipament");
                System.out.println("3. Usar BANG!");
                System.out.println("4. Usar BIRRA");
                System.out.println("5. Passar torn");
                System.out.print("Opcio: ");

                int opcio = scanner.nextInt();
                scanner.nextLine();

                switch (opcio) {
                    case 1:
                        equiparArma(jug);
                        break;
                    case 2:
                        equiparEquipament(jug);
                        break;
                    case 3:
                        usarBang(jug);
                        break;
                    case 4:
                        usarBirra(jug);
                        break;
                    case 5:
                        continuarTorn = false;
                        break;
                    default:
                        System.out.println("Opcio no valida");
                }
            }

            // FASE 3: Descartar si te mes de 4 cartes
            jugadorDAO.passarTorn(sessionFactory, partidaActual.getId(), jug.getId());

            // Comprovar victoria
            String victoria = partidaDAO.comprovarVictoria(sessionFactory, partidaActual.getId());
            if (victoria != null) {
                System.out.println("\n╔════════════════════════════════════════════╗");
                System.out.println("║  " + victoria);
                System.out.println("╚════════════════════════════════════════════╝");
                mostrarRolesFinals();
                return;
            }

            tornActual++;
            jugadors = obtenirJugadorsVius();
        }
    }

    private static void equiparArma(Jugador jugador) {
        Session session = sessionFactory.openSession();
        List<Carta> ma = session.createQuery("FROM Carta c WHERE c.jugadorMa.id = :id", Carta.class)
                .setParameter("id", jugador.getId())
                .getResultList();

        List<CartaArma> armes = new ArrayList<>();
        for (Carta c : ma) {
            if (c instanceof CartaArma) {
                armes.add((CartaArma) c);
            }
        }
        session.close();

        if (armes.isEmpty()) {
            System.out.println("No tens armes per equipar!");
            return;
        }

        System.out.println("\nArmes disponibles:");
        for (int i = 0; i < armes.size(); i++) {
            System.out.println((i + 1) + ". " + armes.get(i).getNom_carta() +
                             " (distancia: " + armes.get(i).getDistanciaArma() + ")");
        }

        System.out.print("Quina arma vols equipar? (0 per cancel·lar): ");
        int opcio = scanner.nextInt();
        scanner.nextLine();

        if (opcio > 0 && opcio <= armes.size()) {
            jugadorDAO.equiparCarta(sessionFactory, jugador.getId(), armes.get(opcio - 1).getId());
        }
    }

    private static void equiparEquipament(Jugador jugador) {
        Session session = sessionFactory.openSession();
        List<Carta> ma = session.createQuery("FROM Carta c WHERE c.jugadorMa.id = :id", Carta.class)
                .setParameter("id", jugador.getId())
                .getResultList();

        List<CartaEquipament> equipaments = new ArrayList<>();
        for (Carta c : ma) {
            if (c instanceof CartaEquipament) {
                equipaments.add((CartaEquipament) c);
            }
        }
        session.close();

        if (equipaments.isEmpty()) {
            System.out.println("No tens equipaments per equipar!");
            return;
        }

        System.out.println("\nEquipaments disponibles:");
        for (int i = 0; i < equipaments.size(); i++) {
            System.out.println((i + 1) + ". " + equipaments.get(i).getNom_carta());
        }

        System.out.print("Quin equipament vols equipar? (0 per cancel·lar): ");
        int opcio = scanner.nextInt();
        scanner.nextLine();

        if (opcio > 0 && opcio <= equipaments.size()) {
            jugadorDAO.equiparCarta(sessionFactory, jugador.getId(), equipaments.get(opcio - 1).getId());
        }
    }

    private static void usarBang(Jugador atacant) {
        List<Jugador> objectius = obtenirJugadorsVius();
        objectius.removeIf(j -> j.getId() == atacant.getId());

        if (objectius.isEmpty()) {
            System.out.println("No hi ha objectius!");
            return;
        }

        System.out.println("\nObjectius disponibles:");
        for (int i = 0; i < objectius.size(); i++) {
            Jugador obj = objectius.get(i);
            int dist = jugadorDAO.calcularDistancia(sessionFactory, atacant.getId(), obj.getId());
            System.out.println((i + 1) + ". " + obj.getNom() + " (vides: " + obj.getVidaActual() +
                             ", distancia: " + dist + ")");
        }

        System.out.print("A qui vols atacar? (0 per cancel·lar): ");
        int opcio = scanner.nextInt();
        scanner.nextLine();

        if (opcio > 0 && opcio <= objectius.size()) {
            jugadorDAO.usarBANG(sessionFactory, partidaActual.getId(),
                              atacant.getId(), objectius.get(opcio - 1).getId());
        }
    }

    private static void usarBirra(Jugador jugador) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        List<CartaUs> birres = session.createQuery("FROM CartaUs c WHERE c.jugadorMa.id = :id AND c.tipusUs = :tipus", CartaUs.class)
                .setParameter("id", jugador.getId())
                .setParameter("tipus", TipusUs.BIRRA)
                .getResultList();

        if (birres.isEmpty()) {
            System.out.println("No tens Birres!");
            session.close();
            return;
        }

        Jugador jug = session.get(Jugador.class, jugador.getId());
        if (jug.getVidaActual() >= jug.getVidaMaxima()) {
            System.out.println("Ja tens la vida al maxim!");
            session.close();
            return;
        }

        CartaUs birra = birres.get(0);
        birra.setJugadorMa(null);
        jug.setVidaActual(Math.min(jug.getVidaActual() + 1, jug.getVidaMaxima()));

        session.merge(birra);
        session.merge(jug);
        tx.commit();
        session.close();

        System.out.println(jug.getNom() + " recupera 1 vida! Vida actual: " + jug.getVidaActual());
    }

    private static List<Jugador> obtenirJugadorsVius() {
        Session session = sessionFactory.openSession();
        Partida partida = session.get(Partida.class, partidaActual.getId());
        partida.getJugadors().size(); // Forçar carrega
        List<Jugador> jugadors = new ArrayList<>();
        for (Jugador j : partida.getJugadors()) {
            if (j.getVidaActual() > 0) {
                jugadors.add(j);
            }
        }
        session.close();
        return jugadors;
    }

    private static void mostrarRolesFinals() {
        System.out.println("\n=== ROLS FINALS ===");
        Session session = sessionFactory.openSession();
        Partida partida = session.get(Partida.class, partidaActual.getId());
        partida.getJugadors().size();

        for (Jugador j : partida.getJugadors()) {
            String estat = j.getVidaActual() > 0 ? "VIU" : "MORT";
            System.out.println(j.getNom() + " - " + j.getRol().getObjectiu() + " [" + estat + "]");
        }
        session.close();
    }
}

