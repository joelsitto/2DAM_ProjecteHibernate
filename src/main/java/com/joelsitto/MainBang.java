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

                System.out.println("\n=== TORN DE: " + jugadorActual.getNom() + " ===");
                System.out.println("Rol: " + jugadorActual.getRol().getObjectiu());
                System.out.println("Vida: " + jugadorActual.getVidaActual() + "/" + jugadorActual.getVidaMaxima());

                // Mostrar arma equipada
                if (jugadorActual.getArmaEquipada() != null) {
                    System.out.println("Arma: " + jugadorActual.getArmaEquipada().getNom_carta() +
                                     " (Dist: " + jugadorActual.getArmaEquipada().getDistanciaArma() + ")");
                } else {
                    System.out.println("Arma: Cap (Dist: 1)");
                }

                // Mostrar equipaments
                if (!jugadorActual.getEquipaments().isEmpty()) {
                    System.out.print("Equipaments: ");
                    for (int i = 0; i < jugadorActual.getEquipaments().size(); i++) {
                        if (i > 0) System.out.print(", ");
                        System.out.print(jugadorActual.getEquipaments().get(i).getNom_carta());
                    }
                    System.out.println();
                } else {
                    System.out.println("Equipaments: Cap");
                }

                // Mostrar modificadors de distància
                System.out.println("Modificador ofensiu: " + jugadorActual.getModificadorDistanciaOff());
                System.out.println("Modificador defensiu: " + jugadorActual.getModificadorDistanciaDef());

                // FASE 1: Robar 2 cartes
                System.out.println("\n--- FASE 1: Robar cartes ---");
                jugadorDAO.robarCarta(sessionFactory, partida.getId(), jugadorActual.getId());
                jugadorDAO.robarCarta(sessionFactory, partida.getId(), jugadorActual.getId());

                // Refrescar el jugador per tenir les cartes actualitzades
                jugadorActual = partidaDAO.obtenirJugadorActual(sessionFactory, partida.getId());

                // FASE 2: Jugar cartes
                System.out.println("\n--- FASE 2: Jugar cartes ---");
                boolean continuarJugant = true;

                while (continuarJugant) {
                    jugadorDAO.mostrarMaJugador(sessionFactory, jugadorActual.getId());

                    System.out.println("\n1. Equipar carta");
                    System.out.println("2. Usar BANG");
                    System.out.println("3. Veure partida");
                    System.out.println("4. Passar torn");
                    System.out.print("Opcio: ");

                    int opcio = -1;
                    try {
                        opcio = scanner.nextInt();
                    } catch (Exception e) {
                        System.out.println("Error: Has d'introduir un numero!");
                        scanner.nextLine(); // Netejar el buffer
                        continue;
                    }

                    if (opcio < 1 || opcio > 4) {
                        System.out.println("Opcio no valida! Tria entre 1 i 4");
                        continue;
                    }

                    switch (opcio) {
                        case 1:
                            equiparCarta(jugadorActual);
                            // Refrescar el jugador després d'equipar
                            jugadorActual = partidaDAO.obtenirJugadorActual(sessionFactory, partida.getId());
                            break;
                        case 2:
                            usarBang(jugadorActual);
                            // Refrescar el jugador després d'atacar
                            jugadorActual = partidaDAO.obtenirJugadorActual(sessionFactory, partida.getId());
                            break;
                        case 3:
                            partidaDAO.mostrarPartida(sessionFactory, partida.getId());
                            break;
                        case 4:
                            continuarJugant = false;
                            break;
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
                System.out.println("\n--- FASE 3: Passar torn ---");
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

        System.out.println("0. Cancel·lar");
        System.out.print("\nEscull la carta a equipar (0-" + cartesEquipables.size() + "): ");

        int opcio = -1;
        try {
            opcio = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Error: Has d'introduir un numero!");
            scanner.nextLine(); // Netejar el buffer
            return;
        }

        if (opcio == 0) {
            System.out.println("Operacio cancel·lada");
            return;
        }

        if (opcio < 1 || opcio > cartesEquipables.size()) {
            System.out.println("Opcio no valida! Tria entre 0 i " + cartesEquipables.size());
            return;
        }

        Carta cartaSeleccionada = cartesEquipables.get(opcio - 1);
        jugadorDAO.equiparCarta(sessionFactory, jugador.getId(), cartaSeleccionada.getId());
    }

    private static void usarBang(Jugador jugador) {
        // Comprovar que té carta BANG
        boolean teBang = false;
        for (Carta carta : jugador.getMa()) {
            if (carta instanceof CartaUs) {
                CartaUs cartaUs = (CartaUs) carta;
                if (cartaUs.getTipusUs() == TipusUs.BANG) {
                    teBang = true;
                    break;
                }
            }
        }

        if (!teBang) {
            System.out.println("\nNo tens cap carta BANG a la ma!");
            return;
        }

        // Obtenir l'abast de l'arma
        int abastArma = 1;
        if (jugador.getArmaEquipada() != null) {
            abastArma = jugador.getArmaEquipada().getDistanciaArma();
        }

        System.out.println("\n=== OBJECTIUS POSSIBLES ===");
        System.out.println("La teva arma te un abast de: " + abastArma);
        System.out.println();

        // Obtenir tots els jugadors i mostrar només els vàlids
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Partida partidaActual = session.get(Partida.class, partida.getId());
            List<Jugador> jugadorsVius = new ArrayList<>();

            for (Jugador j : partidaActual.getJugadors()) {
                // Filtrar: no pot ser el mateix jugador i ha d'estar viu
                if (j.getId() != jugador.getId() && j.getVidaActual() > 0) {
                    // Calcular distància
                    int distancia = jugadorDAO.calcularDistancia(sessionFactory, jugador.getId(), j.getId());
                    boolean potAtacar = distancia <= abastArma;

                    System.out.println("- " + j.getNom() + " [ID: " + j.getId() + "]");
                    System.out.println("  Rol: " + j.getRol().getObjectiu());
                    System.out.println("  Vida: " + j.getVidaActual() + "/" + j.getVidaMaxima());
                    System.out.println("  Distancia: " + distancia);

                    if (potAtacar) {
                        System.out.println("  >>> POTS ATACAR <<<");
                        jugadorsVius.add(j);
                    } else {
                        System.out.println("  >>> MASSA LLUNY (necesites arma amb dist " + distancia + " o mes) <<<");
                    }
                    System.out.println();
                }
            }

            if (jugadorsVius.isEmpty()) {
                System.out.println("No pots atacar a cap jugador amb la teva arma actual!");
                return;
            }

            System.out.println("0. Cancel·lar");
            System.out.print("ID del jugador a atacar (0 per cancel·lar): ");

            int idObjectiu = -1;
            try {
                idObjectiu = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Error: Has d'introduir un numero!");
                scanner.nextLine(); // Netejar el buffer
                return;
            }

            if (idObjectiu == 0) {
                System.out.println("Atac cancel·lat");
                return;
            }

            // Verificar que el jugador seleccionat és vàlid
            boolean jugadorValid = false;
            for (Jugador j : jugadorsVius) {
                if (j.getId() == idObjectiu) {
                    jugadorValid = true;
                    break;
                }
            }

            if (!jugadorValid) {
                System.out.println("No pots atacar aquest jugador! (no existeix, esta mort o esta massa lluny)");
                return;
            }

            jugadorDAO.usarBANG(sessionFactory, partida.getId(), jugador.getId(), idObjectiu);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}


