package com.joelsitto.bang.dao;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.EstatVictoria;
import com.joelsitto.bang.model.enums.TipusColl;
import com.joelsitto.bang.model.enums.TipusEquipament;
import com.joelsitto.bang.model.enums.TipusUs;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.*;

public class PartidaDAOImpl implements IPartidaDAO {

    @Override
    public void llistarJugadorsPartida(SessionFactory sessionFactory, int idPartida) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Partida partida = session.get(Partida.class, idPartida);

            if (partida == null) {
                System.out.println("No s'ha trobat cap partida amb l'id: " + idPartida);
                tx.rollback();
                return;
            }


            List<Jugador> jugadors = partida.getJugadors();

            if (jugadors.isEmpty()) {
                System.out.println("No hi ha jugadors en aquesta partida.");
            } else {
                System.out.println("=== Jugadors de la Partida " + idPartida + " ===");
                for (Jugador jugador : jugadors) {
                    System.out.println("\n- " + jugador.getNom() + " [ID: " + jugador.getId() + "]");
                    System.out.println("  Vida: " + jugador.getVidaActual() + "/" + jugador.getVidaMaxima());
                    System.out.println("  Rol: " + jugador.getRol().getObjectiu());

                    // Mostrar arma
                    if (jugador.getArmaEquipada() != null) {
                        System.out.println("  Arma: " + jugador.getArmaEquipada().getNom_carta() +
                                         " (Dist: " + jugador.getArmaEquipada().getDistanciaArma() + ")");
                    } else {
                        System.out.println("  Arma: Cap (Dist: 1)");
                    }

                    // Mostrar equipaments
                    if (!jugador.getEquipaments().isEmpty()) {
                        System.out.print("  Equipaments: ");
                        for (int i = 0; i < jugador.getEquipaments().size(); i++) {
                            if (i > 0) System.out.print(", ");
                            System.out.print(jugador.getEquipaments().get(i).getNom_carta());
                        }
                        System.out.println();
                    } else {
                        System.out.println("  Equipaments: Cap");
                    }

                    // Mostrar modificadors
                    System.out.println("  Modificador ofensiu: " + jugador.getModificadorDistanciaOff());
                    System.out.println("  Modificador defensiu: " + jugador.getModificadorDistanciaDef());

                    if (jugador.getVidaActual() <= 0) {
                        System.out.println("  [ELIMINAT]");
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void mostrarPartida(SessionFactory sessionFactory, int idPartida) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Partida partida = session.get(Partida.class, idPartida);

            if (partida == null) {
                System.out.println("No s'ha trobat cap partida amb l'id: " + idPartida);
                tx.rollback();
                return;
            }

            System.out.println("\n=== ESTAT DE LA PARTIDA " + idPartida + " ===");
            System.out.println("Estat: " + partida.getEstat());
            System.out.println();

            List<Jugador> jugadors = partida.getJugadors();

            if (jugadors.isEmpty()) {
                System.out.println("No hi ha jugadors en aquesta partida.");
            } else {
                System.out.println("--- JUGADORS VIUS ---");
                for (Jugador jugador : jugadors) {
                    if (jugador.getVidaActual() > 0) {
                        System.out.println("\n- " + jugador.getNom() + " [ID: " + jugador.getId() + "]");
                        System.out.println("  Vida: " + jugador.getVidaActual() + "/" + jugador.getVidaMaxima());
                        System.out.println("  Rol: " + jugador.getRol().getObjectiu());

                        if (jugador.getArmaEquipada() != null) {
                            System.out.println("  Arma: " + jugador.getArmaEquipada().getNom_carta() +
                                             " (Dist: " + jugador.getArmaEquipada().getDistanciaArma() + ")");
                        } else {
                            System.out.println("  Arma: Cap (Dist: 1)");
                        }

                        if (!jugador.getEquipaments().isEmpty()) {
                            System.out.print("  Equipaments: ");
                            for (int i = 0; i < jugador.getEquipaments().size(); i++) {
                                if (i > 0) System.out.print(", ");
                                System.out.print(jugador.getEquipaments().get(i).getNom_carta());
                            }
                            System.out.println();
                        } else {
                            System.out.println("  Equipaments: Cap");
                        }

                        System.out.println("  Modificador ofensiu: " + jugador.getModificadorDistanciaOff());
                        System.out.println("  Modificador defensiu: " + jugador.getModificadorDistanciaDef());
                    }
                }

                System.out.println("\n--- JUGADORS ELIMINATS ---");
                boolean eliminats = false;
                for (Jugador jugador : jugadors) {
                    if (jugador.getVidaActual() <= 0) {
                        eliminats = true;
                        System.out.println("- " + jugador.getNom() + " (Rol: " + jugador.getRol().getObjectiu() + ") [ELIMINAT]");
                    }
                }
                if (!eliminats) {
                    System.out.println("Cap jugador eliminat");
                }
            }


            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Partida iniciarPartida(SessionFactory sessionFactory) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            // Crear o obtenir els rols
            Rol rolSheriff = obtenerOCrearRol(session, "Sheriff");
            Rol rolForajido = obtenerOCrearRol(session, "Forajido");
            Rol rolRenegado = obtenerOCrearRol(session, "Renegado");

            // Crear la partida
            Partida partida = new Partida("En curs", new Date(), true);
            session.persist(partida);

            // Crear els 4 jugadors amb rols aleatoris
            List<Rol> rolesDisponibles = new ArrayList<>();
            rolesDisponibles.add(rolSheriff);
            rolesDisponibles.add(rolForajido);
            rolesDisponibles.add(rolForajido);
            rolesDisponibles.add(rolRenegado);
            Collections.shuffle(rolesDisponibles);

            List<Jugador> jugadores = new ArrayList<>();
            String[] nombresJugadores = {"Jugador 1", "Jugador 2", "Jugador 3", "Jugador 4"};

            for (int i = 0; i < 4; i++) {
                Jugador jugador = new Jugador(nombresJugadores[i], 4, 4, rolesDisponibles.get(i));
                session.persist(jugador);
                jugadores.add(jugador);
                partida.getJugadors().add(jugador);
            }

            // Establir distàncies entre jugadors (en cercle)
            for (int i = 0; i < jugadores.size(); i++) {
                for (int j = i + 1; j < jugadores.size(); j++) {
                    int distancia = Math.min(j - i, jugadores.size() - (j - i));
                    DistanciesJugadors dist = new DistanciesJugadors(jugadores.get(i), jugadores.get(j), distancia);
                    session.persist(dist);
                }
            }

            // Crear el mazo de cartes
            List<Carta> mazo = crearMazo(session);

            // Barrejar el mazo
            Collections.shuffle(mazo);

            // Afegir totes les cartes a la pila de robar de la partida
            partida.getPilaRobar().addAll(mazo);

            // Repartir 4 cartes a cada jugador
            int indiceCartaActual = 0;
            for (Jugador jugador : jugadores) {
                for (int i = 0; i < 4; i++) {
                    if (indiceCartaActual < mazo.size()) {
                        Carta carta = mazo.get(indiceCartaActual);
                        carta.setJugadorMa(jugador);
                        jugador.getMa().add(carta);
                        partida.getPilaRobar().remove(carta);
                        indiceCartaActual++;
                    }
                }
            }

            // Establir el jugador inicial, farem que comenci el Sheriff per comoditat
            for (Jugador jugador : jugadores) {
                if (jugador.getRol().getObjectiu().equals("Sheriff")) {
                    partida.setJugadorActual(jugador);
                    break;
                }
            }

            session.merge(partida);
            tx.commit();

            System.out.println("Partida iniciada amb exit! ID: " + partida.getId());
            return partida;

        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    private Rol obtenerOCrearRol(Session session, String objectiu) {
        String hql = "FROM Rol WHERE objectiu = :objectiu";
        Query<Rol> query = session.createQuery(hql, Rol.class);
        query.setParameter("objectiu", objectiu);
        Rol rol = query.uniqueResult();

        if (rol == null) {
            rol = new Rol(objectiu);
            session.persist(rol);
        }

        return rol;
    }

    private List<Carta> crearMazo(Session session) {
        List<Carta> mazo = new ArrayList<>();

        TipusColl[] colls = TipusColl.values();

        // Crear cartes d'ús (BANG, FALLASTE, BIRRA)
        for (int i = 0; i < 25; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaUs cartaBang = new CartaUs("BANG!", "Ataca a un jugador", coll, TipusUs.BANG);
            session.persist(cartaBang);
            mazo.add(cartaBang);
        }

        for (int i = 0; i < 12; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaUs cartaFallaste = new CartaUs("Fallaste!", "Evita un BANG!", coll, TipusUs.FALLASTE);
            session.persist(cartaFallaste);
            mazo.add(cartaFallaste);
        }

        for (int i = 0; i < 6; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaUs cartaBirra = new CartaUs("Birra", "Recupera 1 vida", coll, TipusUs.BIRRA);
            session.persist(cartaBirra);
            mazo.add(cartaBirra);
        }

        // Crear armes
        String[] nombresArmas = {"Volcanic", "Schofield", "Remington", "Rev Carabine", "Winchester"};
        int[] distanciasArmas = {1, 2, 3, 4, 5};

        for (int i = 0; i < nombresArmas.length; i++) {
            for (int j = 0; j < 2; j++) {
                TipusColl coll = colls[(i + j) % colls.length];
                CartaArma arma = new CartaArma(nombresArmas[i], "Arma de distancia " + distanciasArmas[i],
                                               coll, distanciasArmas[i]);
                session.persist(arma);
                mazo.add(arma);
            }
        }

        // Crear equipaments
        for (int i = 0; i < 3; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaEquipament mustang = new CartaEquipament("Mustang", "Augmenta la distancia defensiva",
                                                          coll, TipusEquipament.CAVALL, 1);
            session.persist(mustang);
            mazo.add(mustang);
        }

        for (int i = 0; i < 3; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaEquipament mira = new CartaEquipament("Mira Telescopica", "Augmenta la distancia ofensiva",
                                                       coll, TipusEquipament.MIRA_TELESCOPICA, 1);
            session.persist(mira);
            mazo.add(mira);
        }

        for (int i = 0; i < 3; i++) {
            TipusColl coll = colls[i % colls.length];
            CartaEquipament barril = new CartaEquipament("Barril", "Pot esquivar BANG!",
                                                         coll, TipusEquipament.BARRIL, 0);
            session.persist(barril);
            mazo.add(barril);
        }

        return mazo;
    }

    @Override
    public EstatVictoria comprovarVictoria(SessionFactory sessionFactory, Partida partida) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Partida partidaActualizada = session.get(Partida.class, partida.getId());

            if (partidaActualizada == null) {
                System.out.println("No s'ha trobat la partida");
                tx.rollback();
                return EstatVictoria.EN_CURS;
            }

            List<Jugador> jugadors = partidaActualizada.getJugadors();

            // Comptar jugadors vius per rol
            boolean sheriffViu = false;
            int forajidosVius = 0;
            int renegadosVius = 0;

            for (Jugador jugador : jugadors) {
                if (jugador.getVidaActual() > 0) {
                    String rol = jugador.getRol().getObjectiu();
                    if (rol.equals("Sheriff")) {
                        sheriffViu = true;
                    } else if (rol.equals("Forajido")) {
                        forajidosVius++;
                    } else if (rol.equals("Renegado")) {
                        renegadosVius++;
                    }
                }
            }

            // Comprovar condicions de victòria
            EstatVictoria resultat = EstatVictoria.EN_CURS;

            if (!sheriffViu && forajidosVius > 0) {
                System.out.println("\n=== ELS FORAJIDOS HAN GUANYAT! ===");
                System.out.println("El Sheriff ha mort");
                partidaActualizada.setEstat("Finalitzada - Victoria Forajidos");
                partidaActualizada.setActiu(false);
                resultat = EstatVictoria.VICTORIA_FORAJIDOS;
            } else if (!sheriffViu && forajidosVius == 0 && renegadosVius > 0) {
                System.out.println("\n=== EL RENEGAT HA GUANYAT! ===");
                System.out.println("El Sheriff ha mort i no queden Forajidos");
                partidaActualizada.setEstat("Finalitzada - Victoria Renegat");
                partidaActualizada.setActiu(false);
                resultat = EstatVictoria.VICTORIA_RENEGAT;
            } else if (sheriffViu && forajidosVius == 0 && renegadosVius == 0) {
                System.out.println("\n=== EL SHERIFF HA GUANYAT! ===");
                System.out.println("Tots els Forajidos i Renegats han mort");
                partidaActualizada.setEstat("Finalitzada - Victoria Sheriff");
                partidaActualizada.setActiu(false);
                resultat = EstatVictoria.VICTORIA_SHERIFF;
            } else if (sheriffViu && forajidosVius == 0 && renegadosVius == 1) {
                System.out.println("\n=== EL RENEGAT HA GUANYAT! ===");
                System.out.println("Nomes queden el Sheriff i el Renegat");
                partidaActualizada.setEstat("Finalitzada - Victoria Renegat");
                partidaActualizada.setActiu(false);
                resultat = EstatVictoria.VICTORIA_RENEGAT;
            }

            session.merge(partidaActualizada);
            tx.commit();

            return resultat;

        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public TipusColl mostrarCarta(SessionFactory sessionFactory, int idPartida) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Partida partida = session.get(Partida.class, idPartida);

            if (partida == null) {
                System.out.println("No s'ha trobat la partida");
                tx.rollback();
                return null;
            }

            List<Carta> pilaRobar = partida.getPilaRobar();

            if (pilaRobar.isEmpty()) {
                System.out.println("La pila de robar esta buida");
                tx.rollback();
                return null;
            }

            // Treure la primera carta de la pila de robar i obtindre el seu coll
            Carta carta = pilaRobar.get(0);
            TipusColl coll = carta.getColl();

            // Moure la carta a la pila de descartades
            partida.getPilaRobar().remove(carta);
            partida.getPilaDescartades().add(carta);

            session.merge(partida);
            tx.commit();

            System.out.println("Carta mostrada: " + carta.getNom_carta() + " - Coll: " + coll);
            return coll;

        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Jugador obtenirJugadorActual(SessionFactory sessionFactory, int idPartida) {
        try (Session session = sessionFactory.openSession()) {
            Partida partida = session.get(Partida.class, idPartida);

            if (partida == null) {
                System.out.println("No s'ha trobat la partida");
                return null;
            }

            Jugador jugadorActual = partida.getJugadorActual();

            // Inicialitzar les col·leccions per evitar lazy loading (em dona aquest error si no faig això) fuck hibernate
            // Accedim a cada element per forçar la càrrega
            for (Carta c : jugadorActual.getMa()) {
                c.getId();
            }
            for (CartaEquipament eq : jugadorActual.getEquipaments()) {
                eq.getId();
            }

            return jugadorActual;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

