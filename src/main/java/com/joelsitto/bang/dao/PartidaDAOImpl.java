package com.joelsitto.bang.dao;

import com.joelsitto.bang.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.*;

public class PartidaDAOImpl implements IPartidaDAO {

    @Override
    public List<Jugador> llistarJugadorsPartida(SessionFactory sessionFactory, int idPartida) {
        List<Jugador> jugadors = new ArrayList<>();

        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT j FROM Partida p JOIN p.jugadors j WHERE p.id = :idPartida";
            Query<Jugador> query = session.createQuery(hql, Jugador.class);
            query.setParameter("idPartida", idPartida);
            jugadors = query.getResultList();

            System.out.println("=== JUGADORS DE LA PARTIDA " + idPartida + " ===");
            for (Jugador j : jugadors) {
                System.out.println("- " + j.getNom());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jugadors;
    }

    @Override
    public void mostrarPartida(SessionFactory sessionFactory, int idPartida) {
        try (Session session = sessionFactory.openSession()) {
            Partida partida = session.get(Partida.class, idPartida);

            if (partida == null) {
                System.out.println("Partida no trobada!");
                return;
            }

            System.out.println("\n========================================");
            System.out.println("    ESTAT DE LA PARTIDA #" + idPartida);
            System.out.println("========================================");
            System.out.println("Estat: " + partida.getEstat());
            System.out.println("\n--- JUGADORS VIUS ---");

            for (Jugador j : partida.getJugadors()) {
                if (j.getVidaActual() > 0) {
                    System.out.println("\n" + j.getNom());
                    System.out.println("  Vida: " + j.getVidaActual() + "/" + j.getVidaMaxima());

                    if (j.getArmaEquipada() != null) {
                        System.out.println("  Arma: " + j.getArmaEquipada().getNom_carta());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Partida iniciarPartida(SessionFactory sessionFactory, List<String> nomsJugadors, List<Rol> roles) {
        Transaction tx = null;
        Partida partida = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            partida = new Partida();
            partida.setEstat("En curs");
            partida.setDataInici(new Date());
            partida.setActiu(true);
            session.persist(partida);

            Collections.shuffle(roles);

            List<Jugador> jugadors = new ArrayList<>();
            for (int i = 0; i < nomsJugadors.size(); i++) {
                Jugador jugador = new Jugador();
                jugador.setNom(nomsJugadors.get(i));
                jugador.setRol(roles.get(i));

                if (roles.get(i).getObjectiu().contains("Eliminar a tots")) {
                    jugador.setVidaMaxima(5);
                    jugador.setVidaActual(5);
                } else {
                    jugador.setVidaMaxima(4);
                    jugador.setVidaActual(4);
                }

                jugador.setModificadorDistanciaDef(0);
                jugador.setModificadorDistanciaOff(0);

                session.persist(jugador);
                jugadors.add(jugador);
                partida.getJugadors().add(jugador);
            }

            int numJugadors = jugadors.size();
            for (int i = 0; i < numJugadors; i++) {
                for (int j = i + 1; j < numJugadors; j++) {
                    int distancia = Math.min(j - i, numJugadors - (j - i));

                    DistanciesJugadors dist1 = new DistanciesJugadors();
                    dist1.setJugador1(jugadors.get(i));
                    dist1.setJugador2(jugadors.get(j));
                    dist1.setDistancia(distancia);
                    session.persist(dist1);

                    DistanciesJugadors dist2 = new DistanciesJugadors();
                    dist2.setJugador1(jugadors.get(j));
                    dist2.setJugador2(jugadors.get(i));
                    dist2.setDistancia(distancia);
                    session.persist(dist2);
                }
            }

            tx.commit();
            System.out.println("Partida iniciada amb " + nomsJugadors.size() + " jugadors");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw e;
        }

        return partida;
    }

    @Override
    public String comprovarVictoria(SessionFactory sessionFactory, int idPartida) {
        String resultat = null;

        try (Session session = sessionFactory.openSession()) {
            Partida partida = session.get(Partida.class, idPartida);

            boolean sheriffViu = false;
            boolean malfactorViu = false;
            boolean renegatViu = false;
            int jugadorsVius = 0;

            for (Jugador j : partida.getJugadors()) {
                if (j.getVidaActual() > 0) {
                    jugadorsVius++;
                    String objectiu = j.getRol().getObjectiu();

                    if (objectiu.contains("Eliminar a tots")) {
                        sheriffViu = true;
                    } else if (objectiu.contains("Eliminar al Sheriff")) {
                        malfactorViu = true;
                    } else if (objectiu.contains("ultim jugador")) {
                        renegatViu = true;
                    }
                }
            }

            if (!sheriffViu && malfactorViu) {
                resultat = "VICTORIA DELS MALFACTORS";
            } else if (sheriffViu && !malfactorViu && !renegatViu) {
                resultat = "VICTORIA DEL SHERIFF";
            } else if (renegatViu && jugadorsVius == 1) {
                resultat = "VICTORIA DEL RENEGAT";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultat;
    }

    @Override
    public String mostrarCarta(SessionFactory sessionFactory, int idPartida) {
        Transaction tx = null;
        String coll = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Partida partida = session.get(Partida.class, idPartida);

            if (partida.getPilaRobar().isEmpty()) {
                List<Carta> descartades = new ArrayList<>(partida.getPilaDescartades());
                Collections.shuffle(descartades);
                partida.getPilaRobar().addAll(descartades);
                partida.getPilaDescartades().clear();
            }

            Carta carta = partida.getPilaRobar().remove(0);
            partida.getPilaDescartades().add(carta);

            String[] colls = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
            String[] pals = {"Cors", "Diamants", "Trebols", "Piques"};
            coll = colls[new Random().nextInt(colls.length)] + " de " +
                   pals[new Random().nextInt(pals.length)];

            session.merge(partida);
            tx.commit();

            System.out.println("Carta: " + coll);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw e;
        }

        return coll;
    }
}

