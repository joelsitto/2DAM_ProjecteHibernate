package com.joelsitto;

import com.joelsitto.bang.dao.*;
import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.*;
import com.joelsitto.bang.util.HibernateUtil;
import org.hibernate.SessionFactory;

import java.util.*;

/**
 * Main del joc BANG!
 */
public class MainBang {

    private static SessionFactory sessionFactory;
    private static IPartidaDAO partidaDAO;
    private static IJugadorDAO jugadorDAO;
    private static Scanner scanner;
    private static Partida partida;

    public static void main(String[] args) {
        sessionFactory = HibernateUtil.getSessionFactory();
        partidaDAO = new PartidaDAOImpl();
        jugadorDAO = new JugadorDAOImpl();
        scanner = new Scanner(System.in);

        try {
            System.out.println("=== BENVINGUT AL JOC BANG! ===\n");

            // Crear partida
            System.out.println("Iniciant partida...\n");
            partida = partidaDAO.iniciarPartida(sessionFactory);

            System.out.println("\n=== PARTIDA INICIADA ===");
            partidaDAO.llistarJugadorsPartida(sessionFactory, partida.getId());

            // Bucle del joc
            boolean partidaActiva = true;

            while (partidaActiva) {
                // Obtenir jugador actual
                Jugador jugadorActual = partidaDAO.obtenirJugadorActual(sessionFactory, partida.getId());

                System.out.println("\n--- TORN DE: " + jugadorActual.getNom() + " ---");
                System.out.println("Vida: " + jugadorActual.getVidaActual() + "/" + jugadorActual.getVidaMaxima());

                // FASE 1: Robar 2 cartes
                System.out.println("\nFASE 1: Robar cartes");
                jugadorDAO.robarCarta(sessionFactory, partida.getId(), jugadorActual.getId());
                jugadorDAO.robarCarta(sessionFactory, partida.getId(), jugadorActual.getId());

                // FASE 2: Jugar cartes
                System.out.println("\nFASE 2: Jugar cartes");
                boolean continuarJugant = true;

                while (continuarJugant) {
                    jugadorDAO.mostrarMaJugador(sessionFactory, jugadorActual.getId());

                    System.out.println("\n1. Equipar carta");
                    System.out.println("2. Usar BANG");
                    System.out.println("3. Veure partida");
                    System.out.println("4. Passar torn");
                    System.out.print("Opcio: ");

                    int opcio = scanner.nextInt();

                    switch (opcio) {
                        case 1:
                            equiparCarta(jugadorActual);
                            break;
                        case 2:
                            usarBang(jugadorActual);
                            break;
                        case 3:
                            partidaDAO.mostrarPartida(sessionFactory, partida.getId());
                            break;
                        case 4:
                            continuarJugant = false;
                            break;
                        default:
                            System.out.println("Opcio no valida");
                    }

                    // Comprovar victoria
                    EstatVictoria estat = partidaDAO.comprovarVictoria(sessionFactory, partida);
                    if (estat != EstatVictoria.EN_CURS) {
                        partidaActiva = false;
                        break;
                    }
                }

                if (!partidaActiva) break;

                // FASE 3: Descartar i passar torn
                System.out.println("\nFASE 3: Passar torn");
                jugadorDAO.passarTorn(sessionFactory, partida.getId(), jugadorActual.getId());
            }

            // Fi de la partida
            System.out.println("\n=== PARTIDA FINALITZADA ===");
            partidaDAO.mostrarPartida(sessionFactory, partida.getId());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
            HibernateUtil.shutdown();
        }
    }

    private static void equiparCarta(Jugador jugador) {
        List<Carta> cartesMa = jugador.getMa();

        // Filtrar només les cartes equipables (armes i equipaments)
        List<Carta> cartesEquipables = new ArrayList<>();
        for (Carta carta : cartesMa) {
            if (carta instanceof CartaArma || carta instanceof CartaEquipament) {
                cartesEquipables.add(carta);
            }
        }

        if (cartesEquipables.isEmpty()) {
            System.out.println("No tens cap carta equipable!");
            return;
        }

        // Mostrar les cartes equipables
        System.out.println("\n=== CARTES EQUIPABLES ===");
        for (int i = 0; i < cartesEquipables.size(); i++) {
            Carta carta = cartesEquipables.get(i);
            System.out.print((i + 1) + ". " + carta.getNom_carta() + " (" + carta.getColl() + ")");

            if (carta instanceof CartaArma) {
                CartaArma arma = (CartaArma) carta;
                System.out.print(" - Arma distancia " + arma.getDistanciaArma());
            } else if (carta instanceof CartaEquipament) {
                CartaEquipament eq = (CartaEquipament) carta;
                System.out.print(" - " + eq.getTipus());
            }
            System.out.println();
        }

        System.out.print("\nEscull la carta a equipar (1-" + cartesEquipables.size() + "): ");
        int opcio = scanner.nextInt();

        if (opcio >= 1 && opcio <= cartesEquipables.size()) {
            Carta cartaSeleccionada = cartesEquipables.get(opcio - 1);
            jugadorDAO.equiparCarta(sessionFactory, jugador.getId(), cartaSeleccionada.getId());
        } else {
            System.out.println("Opcio no valida!");
        }
    }

    private static void usarBang(Jugador jugador) {
        System.out.println("\nJugadors:");
        partidaDAO.llistarJugadorsPartida(sessionFactory, partida.getId());
        System.out.print("ID del jugador a atacar: ");
        int idObjectiu = scanner.nextInt();
        jugadorDAO.usarBANG(sessionFactory, partida.getId(), jugador.getId(), idObjectiu);
    }
}


