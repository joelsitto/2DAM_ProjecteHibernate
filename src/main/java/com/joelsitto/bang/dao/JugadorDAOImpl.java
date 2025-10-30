package com.joelsitto.bang.dao;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.*;

public class JugadorDAOImpl implements IJugadorDAO {

    @Override
    public List<Carta> mostrarMaJugador(SessionFactory sessionFactory, int idJugador) {
        List<Carta> cartes = new ArrayList<>();

        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Carta c WHERE c.jugadorMa.id = :idJugador";
            Query<Carta> query = session.createQuery(hql, Carta.class);
            query.setParameter("idJugador", idJugador);
            cartes = query.getResultList();

            Jugador jugador = session.get(Jugador.class, idJugador);

            if (cartes.isEmpty()) {
                System.out.println("La ma esta buida!");
            } else {
                System.out.println("\n=== MA DE " + jugador.getNom() + " ===");
                for (int i = 0; i < cartes.size(); i++) {
                    Carta c = cartes.get(i);
                    System.out.println((i + 1) + ". " + c.getNom_carta());
                }
            }
            System.out.println("Total: " + cartes.size() + " cartes");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cartes;
    }

    @Override
    public boolean usarBANG(SessionFactory sessionFactory, int idPartida, int idJugadorAtacant, int idJugadorObjectiu) {
        Transaction tx = null;
        boolean success = false;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Jugador atacant = session.get(Jugador.class, idJugadorAtacant);
            Jugador objectiu = session.get(Jugador.class, idJugadorObjectiu);

            System.out.println("\n" + atacant.getNom() + " ataca a " + objectiu.getNom());

            // Comprovar que te carta BANG
            String hql = "FROM CartaUs c WHERE c.jugadorMa.id = :idJugador AND c.tipusUs = :tipus";
            List<CartaUs> bangs = session.createQuery(hql, CartaUs.class)
                    .setParameter("idJugador", idJugadorAtacant)
                    .setParameter("tipus", TipusUs.BANG)
                    .getResultList();

            if (bangs.isEmpty()) {
                System.out.println("No te cap carta BANG!");
                tx.rollback();
                return false;
            }

            // Verificar distancia
            if (!comprovarDistanciaAtac(sessionFactory, idJugadorAtacant, idJugadorObjectiu)) {
                System.out.println("Esta massa lluny!");
                tx.rollback();
                return false;
            }

            // Descartar BANG
            CartaUs bang = bangs.get(0);
            bang.setJugadorMa(null);
            session.merge(bang);
            System.out.println("Descarta BANG!");

            tx.commit();

            // Comprovar BARRIL
            String hqlBarril = "FROM CartaEquipament c WHERE c.jugadorEquipament.id = :idJugador AND c.tipus = :tipus";
            Session session2 = sessionFactory.openSession();
            List<CartaEquipament> barrils = session2.createQuery(hqlBarril, CartaEquipament.class)
                    .setParameter("idJugador", idJugadorObjectiu)
                    .setParameter("tipus", TipusEquipament.BARRIL)
                    .getResultList();
            session2.close();

            if (!barrils.isEmpty()) {
                System.out.println(objectiu.getNom() + " te un BARRIL!");

                // Usar mostrarCarta per veure el coll
                IPartidaDAO partidaDAO = new PartidaDAOImpl();
                String coll = partidaDAO.mostrarCarta(sessionFactory, idPartida);

                // Si es Cors, el barril funciona
                if (coll.contains("Cors")) {
                    System.out.println("El BARRIL funciona! (ha sortit " + coll + ")");
                    return true;
                }
                System.out.println("El BARRIL falla! (ha sortit " + coll + ")");
            }

            // Comprovar FALLASTE
            tx = null;
            try (Session session3 = sessionFactory.openSession()) {
                tx = session3.beginTransaction();

                String hqlFallaste = "FROM CartaUs c WHERE c.jugadorMa.id = :idJugador AND c.tipusUs = :tipus";
                List<CartaUs> fallastes = session3.createQuery(hqlFallaste, CartaUs.class)
                        .setParameter("idJugador", idJugadorObjectiu)
                        .setParameter("tipus", TipusUs.FALLASTE)
                        .getResultList();

                if (!fallastes.isEmpty()) {
                    System.out.println(objectiu.getNom() + " juga FALLASTE!");
                    CartaUs fallaste = fallastes.get(0);
                    fallaste.setJugadorMa(null);
                    session3.merge(fallaste);
                    tx.commit();
                    return true;
                }

                // L'atac impacta
                Jugador obj = session3.get(Jugador.class, idJugadorObjectiu);
                System.out.println(obj.getNom() + " rep 1 bala!");
                obj.setVidaActual(obj.getVidaActual() - 1);
                session3.merge(obj);

                tx.commit();
                success = true;

            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }

            // Comprovar eliminacio
            comprovarEliminacio(sessionFactory, idPartida, idJugadorObjectiu);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw e;
        }

        return success;
    }

    @Override
    public void descartarCarta(SessionFactory sessionFactory, int idPartida, int idJugador, int idCarta) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Carta carta = session.get(Carta.class, idCarta);

            if (carta != null && carta.getJugadorMa() != null &&
                carta.getJugadorMa().getId() == idJugador) {

                carta.setJugadorMa(null);
                session.merge(carta);

                Partida partida = session.get(Partida.class, idPartida);
                if (partida != null) {
                    partida.getPilaDescartades().add(carta);
                    session.merge(partida);
                }

                System.out.println("Carta descartada: " + carta.getNom_carta());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void comprovarEliminacio(SessionFactory sessionFactory, int idPartida, int idJugador) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Jugador jugador = session.get(Jugador.class, idJugador);

            if (jugador.getVidaActual() <= 0) {
                System.out.println("\n" + jugador.getNom() + " ha estat ELIMINAT!");

                // Descartar cartes de la ma
                String hql = "FROM Carta c WHERE c.jugadorMa.id = :idJugador";
                List<Carta> cartes = session.createQuery(hql, Carta.class)
                        .setParameter("idJugador", idJugador)
                        .getResultList();

                for (Carta c : cartes) {
                    c.setJugadorMa(null);
                    session.merge(c);
                }

                // Descartar equipament
                String hqlEquip = "FROM CartaEquipament c WHERE c.jugadorEquipament.id = :idJugador";
                List<CartaEquipament> equipaments = session.createQuery(hqlEquip, CartaEquipament.class)
                        .setParameter("idJugador", idJugador)
                        .getResultList();

                for (CartaEquipament eq : equipaments) {
                    eq.setJugadorEquipament(null);
                    session.merge(eq);
                }

                if (jugador.getArmaEquipada() != null) {
                    jugador.setArmaEquipada(null);
                    session.merge(jugador);
                }
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void robarCarta(SessionFactory sessionFactory, int idPartida, int idJugador) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Partida partida = session.get(Partida.class, idPartida);
            Jugador jugador = session.get(Jugador.class, idJugador);

            if (partida == null) {
                System.out.println("Partida no trobada!");
                tx.rollback();
                return;
            }

            if (partida.getPilaRobar().isEmpty()) {
                System.out.println("Barallant cartes...");
                List<Carta> descartades = new ArrayList<>(partida.getPilaDescartades());
                Collections.shuffle(descartades);
                partida.getPilaRobar().addAll(descartades);
                partida.getPilaDescartades().clear();
            }

            if (!partida.getPilaRobar().isEmpty()) {
                Carta carta = partida.getPilaRobar().remove(0);
                carta.setJugadorMa(jugador);
                session.merge(carta);
                session.merge(partida);

                System.out.println(jugador.getNom() + " roba: " + carta.getNom_carta());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void passarTorn(SessionFactory sessionFactory, int idPartida, int idJugador) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Jugador jugador = session.get(Jugador.class, idJugador);

            String hql = "FROM Carta c WHERE c.jugadorMa.id = :idJugador";
            List<Carta> cartes = session.createQuery(hql, Carta.class)
                    .setParameter("idJugador", idJugador)
                    .getResultList();

            int limit = jugador.getVidaActual();

            if (cartes.size() > limit) {
                System.out.println(jugador.getNom() + " te massa cartes! Ha de descartar " +
                                 (cartes.size() - limit));

                Collections.shuffle(cartes);
                for (int i = 0; i < cartes.size() - limit; i++) {
                    cartes.get(i).setJugadorMa(null);
                    session.merge(cartes.get(i));
                }
            }

            System.out.println("Torn finalitzat");

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void equiparCarta(SessionFactory sessionFactory, int idJugador, int idCarta) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Jugador jugador = session.get(Jugador.class, idJugador);
            Carta carta = session.get(Carta.class, idCarta);

            // Refrescar para obtener el estado actual de la BD
            session.refresh(carta);

            if (carta.getJugadorMa() == null || carta.getJugadorMa().getId() != idJugador) {
                System.out.println("El jugador no te aquesta carta!");
                tx.rollback();
                return;
            }

            if (carta instanceof CartaArma) {
                CartaArma arma = (CartaArma) carta;

                // Si ya tiene un arma, primero la quitamos completamente
                if (jugador.getArmaEquipada() != null) {
                    CartaArma armaAnterior = jugador.getArmaEquipada();
                    jugador.setArmaEquipada(null);
                    session.merge(jugador);
                    session.flush(); // Forzar el update antes de asignar la nueva
                }

                // Ahora asignamos la nueva arma
                jugador.setArmaEquipada(arma);
                arma.setJugadorMa(null);
                session.merge(jugador);
                session.merge(arma);

                System.out.println(jugador.getNom() + " equipa: " + arma.getNom_carta());

            } else if (carta instanceof CartaEquipament) {
                CartaEquipament equipament = (CartaEquipament) carta;

                String hql = "FROM CartaEquipament c WHERE c.jugadorEquipament.id = :idJugador AND c.tipus = :tipus";
                List<CartaEquipament> existents = session.createQuery(hql, CartaEquipament.class)
                        .setParameter("idJugador", idJugador)
                        .setParameter("tipus", equipament.getTipus())
                        .getResultList();

                if (!existents.isEmpty()) {
                    System.out.println("Ja te un equipament d'aquest tipus!");
                    tx.rollback();
                    return;
                }

                equipament.setJugadorEquipament(jugador);
                equipament.setJugadorMa(null);

                if (equipament.getTipus() == TipusEquipament.MIRA_TELESCOPICA) {
                    jugador.setModificadorDistanciaOff(jugador.getModificadorDistanciaOff() - 1);
                } else if (equipament.getTipus() == TipusEquipament.CAVALL) {
                    jugador.setModificadorDistanciaDef(jugador.getModificadorDistanciaDef() + 1);
                }

                session.merge(equipament);
                session.merge(jugador);

                System.out.println(jugador.getNom() + " equipa: " + equipament.getNom_carta());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public int calcularDistancia(SessionFactory sessionFactory, int idJugadorOrigen, int idJugadorDesti) {
        int distanciaFinal = 0;

        try (Session session = sessionFactory.openSession()) {
            Jugador origen = session.get(Jugador.class, idJugadorOrigen);
            Jugador desti = session.get(Jugador.class, idJugadorDesti);

            String hql = "FROM DistanciesJugadors d WHERE d.jugador1.id = :id1 AND d.jugador2.id = :id2";
            DistanciesJugadors dist = session.createQuery(hql, DistanciesJugadors.class)
                    .setParameter("id1", idJugadorOrigen)
                    .setParameter("id2", idJugadorDesti)
                    .uniqueResult();

            if (dist != null) {
                distanciaFinal = dist.getDistancia();
                distanciaFinal += origen.getModificadorDistanciaOff();
                distanciaFinal += desti.getModificadorDistanciaDef();

                if (distanciaFinal < 1) distanciaFinal = 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return distanciaFinal;
    }

    @Override
    public boolean comprovarDistanciaAtac(SessionFactory sessionFactory, int idJugadorAtacant, int idJugadorObjectiu) {
        boolean potAtacar = false;

        try (Session session = sessionFactory.openSession()) {
            Jugador atacant = session.get(Jugador.class, idJugadorAtacant);

            int abastArma = 1;
            if (atacant.getArmaEquipada() != null) {
                abastArma = atacant.getArmaEquipada().getDistanciaArma();
            }

            int distancia = calcularDistancia(sessionFactory, idJugadorAtacant, idJugadorObjectiu);

            potAtacar = distancia <= abastArma;

            System.out.println("Distancia: " + distancia + " | Abast arma: " + abastArma);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return potAtacar;
    }
}

