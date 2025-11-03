package com.joelsitto.bang.dao;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.*;

public class JugadorDAOImpl implements IJugadorDAO {

    @Override
    public void mostrarMaJugador(SessionFactory sessionFactory, int idJugador) {
        try (Session session = sessionFactory.openSession()) {
            // Obtenir el jugador
            Jugador jugador = session.get(Jugador.class, idJugador);

            if (jugador == null) {
                System.out.println("No s'ha trobat el jugador amb id: " + idJugador);
                return;
            }

            // Obtenir les cartes de la ma del jugador directament
            List<Carta> cartesMa = jugador.getMa();

            // Mostrar les cartes
            System.out.println("\n=== MA DE " + jugador.getNom() + " ===");

            if (cartesMa.isEmpty()) {
                System.out.println("La ma esta buida!");
            } else {
                for (int i = 0; i < cartesMa.size(); i++) {
                    Carta carta = cartesMa.get(i);
                    System.out.print((i + 1) + ". " + carta.getNom_carta() + " (" + carta.getColl() + ")");

                    // Afegir informació extra segons el tipus de carta
                    if (carta instanceof CartaArma) {
                        CartaArma arma = (CartaArma) carta;
                        System.out.print(" - ARMA (Dist: " + arma.getDistanciaArma() + ")");
                    } else if (carta instanceof CartaEquipament) {
                        CartaEquipament eq = (CartaEquipament) carta;
                        System.out.print(" - EQUIPAMENT (" + eq.getTipus() + ")");
                    } else if (carta instanceof CartaUs) {
                        CartaUs us = (CartaUs) carta;
                        System.out.print(" - " + us.getTipusUs());
                    }

                    System.out.println(" [ID: " + carta.getId() + "]");
                }
                System.out.println("Total: " + cartesMa.size() + " cartes");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void usarBANG(SessionFactory sessionFactory, int idPartida, int idJugadorAtacant, int idJugadorObjectiu) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            // Obtenir la partida i els jugadors
            Partida partida = session.get(Partida.class, idPartida);
            Jugador atacant = session.get(Jugador.class, idJugadorAtacant);
            Jugador objectiu = session.get(Jugador.class, idJugadorObjectiu);

            if (partida == null || atacant == null || objectiu == null) {
                System.out.println("No s'ha trobat la partida o els jugadors");
                tx.rollback();
                return;
            }

            System.out.println("\n========== ATAC BANG! ==========");
            System.out.println(atacant.getNom() + " (Rol: " + atacant.getRol().getObjectiu() + ") ataca a " +
                             objectiu.getNom() + " (Rol: " + objectiu.getRol().getObjectiu() + ")");
            System.out.println("Vida atacant: " + atacant.getVidaActual() + "/" + atacant.getVidaMaxima());
            System.out.println("Vida objectiu: " + objectiu.getVidaActual() + "/" + objectiu.getVidaMaxima());

            // Comprovar que te carta BANG a la ma
            CartaUs cartaBang = null;
            for (Carta carta : atacant.getMa()) {
                if (carta instanceof CartaUs) {
                    CartaUs cartaUs = (CartaUs) carta;
                    if (cartaUs.getTipusUs() == TipusUs.BANG) {
                        cartaBang = cartaUs;
                        break;
                    }
                }
            }

            if (cartaBang == null) {
                System.out.println(atacant.getNom() + " no te cap carta BANG!");
                tx.rollback();
                return;
            }


            // Descartar la carta BANG a la pila de descartades
            cartaBang.setJugadorMa(null);
            atacant.getMa().remove(cartaBang);
            partida.getPilaDescartades().add(cartaBang);
            session.merge(cartaBang);
            session.merge(atacant);
            System.out.println(atacant.getNom() + " descarta BANG!");

            // Comprovar si te BARRIL
            boolean teBarril = false;
            for (CartaEquipament eq : objectiu.getEquipaments()) {
                if (eq.getTipus() == TipusEquipament.BARRIL) {
                    teBarril = true;
                    break;
                }
            }

            if (teBarril) {
                System.out.println(objectiu.getNom() + " te un BARRIL!");

                // Usar mostrarCarta per veure el coll
                IPartidaDAO partidaDAO = new PartidaDAOImpl();
                TipusColl coll = partidaDAO.mostrarCarta(sessionFactory, idPartida);

                // Si es Cors, el barril funciona
                if (coll == TipusColl.CORS) {
                    System.out.println("El BARRIL funciona! (ha sortit " + coll + ")");
                    System.out.println(objectiu.getNom() + " esquiva l'atac!");
                    session.merge(partida);
                    tx.commit();
                    return;
                }
                System.out.println("El BARRIL falla! (ha sortit " + coll + ")");
            }

            // Comprovar si te FALLASTE
            CartaUs cartaFallaste = null;
            for (Carta carta : objectiu.getMa()) {
                if (carta instanceof CartaUs) {
                    CartaUs cartaUs = (CartaUs) carta;
                    if (cartaUs.getTipusUs() == TipusUs.FALLASTE) {
                        cartaFallaste = cartaUs;
                        break;
                    }
                }
            }

            if (cartaFallaste != null) {
                System.out.println(objectiu.getNom() + " juga FALLASTE!");
                cartaFallaste.setJugadorMa(null);
                objectiu.getMa().remove(cartaFallaste);
                partida.getPilaDescartades().add(cartaFallaste);
                session.merge(cartaFallaste);
                session.merge(objectiu);
                session.merge(partida);
                tx.commit();
                System.out.println(objectiu.getNom() + " esquiva l'atac!");
                return;
            }

            // L'atac impacta
            System.out.println("\n>>> L'ATAC IMPACTA! <<<");
            System.out.println(objectiu.getNom() + " rep 1 bala!");
            System.out.println("Vida abans: " + objectiu.getVidaActual());
            objectiu.setVidaActual(objectiu.getVidaActual() - 1);
            System.out.println("Vida despres: " + objectiu.getVidaActual());
            session.merge(objectiu);
            session.merge(partida);
            tx.commit();
            System.out.println("================================\n");

            // Comprovar si ha estat eliminat
            comprovarEliminacio(sessionFactory, idPartida, idJugadorObjectiu);

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void descartarCarta(SessionFactory sessionFactory, int idPartida, int idJugador, int idCarta) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            // Obtenir la partida, el jugador i la carta
            Partida partida = session.get(Partida.class, idPartida);
            Jugador jugador = session.get(Jugador.class, idJugador);
            Carta carta = session.get(Carta.class, idCarta);

            if (partida == null || jugador == null || carta == null) {
                System.out.println("No s'ha trobat la partida, el jugador o la carta");
                tx.rollback();
                return;
            }

            // Comprovar que la carta estigui a la ma del jugador
            if (carta.getJugadorMa() == null || carta.getJugadorMa().getId() != idJugador) {
                System.out.println("Aquesta carta no esta a la ma del jugador!");
                tx.rollback();
                return;
            }

            // Treure la carta de la ma i afegir-la a la pila de descartades
            carta.setJugadorMa(null);
            jugador.getMa().remove(carta);
            partida.getPilaDescartades().add(carta);

            session.merge(carta);
            session.merge(jugador);
            session.merge(partida);
            tx.commit();

            System.out.println(jugador.getNom() + " descarta: " + carta.getNom_carta());
            System.out.println("  -> Cartes a la ma ara: " + (jugador.getMa().size() - 1));
            System.out.println("  -> Carta enviada a la pila de descartades");

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void comprovarEliminacio(SessionFactory sessionFactory, int idPartida, int idJugador) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            // Obtenir la partida i el jugador
            Partida partida = session.get(Partida.class, idPartida);
            Jugador jugador = session.get(Jugador.class, idJugador);

            if (partida == null || jugador == null) {
                System.out.println("No s'ha trobat la partida o el jugador");
                tx.rollback();
                return;
            }

            // Comprovar si el jugador esta eliminat
            if (jugador.getVidaActual() <= 0) {
                System.out.println("\n" + jugador.getNom() + " ha estat ELIMINAT!");
                System.out.println("Rol: " + jugador.getRol().getObjectiu());

                // Descartar totes les cartes de la ma
                List<Carta> cartesMa = new ArrayList<>(jugador.getMa());
                for (Carta carta : cartesMa) {
                    carta.setJugadorMa(null);
                    jugador.getMa().remove(carta);
                    partida.getPilaDescartades().add(carta);
                    session.merge(carta);
                }

                // Descartar tots els equipaments
                List<CartaEquipament> equipaments = new ArrayList<>(jugador.getEquipaments());
                for (CartaEquipament eq : equipaments) {
                    eq.setJugadorEquipament(null);
                    jugador.getEquipaments().remove(eq);
                    partida.getPilaDescartades().add(eq);
                    session.merge(eq);
                }

                // Descartar l'arma equipada
                if (jugador.getArmaEquipada() != null) {
                    CartaArma arma = jugador.getArmaEquipada();
                    jugador.setArmaEquipada(null);
                    partida.getPilaDescartades().add(arma);
                    session.merge(arma);
                }

                session.merge(jugador);
                session.merge(partida);
            }

            tx.commit();

            // Comprovar si la partida ha acabat
            if (jugador.getVidaActual() <= 0) {
                IPartidaDAO partidaDAO = new PartidaDAOImpl();
                partidaDAO.comprovarVictoria(sessionFactory, partida);
            }

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void robarCarta(SessionFactory sessionFactory, int idPartida, int idJugador) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            // Obtenir la partida i el jugador
            Partida partida = session.get(Partida.class, idPartida);
            Jugador jugador = session.get(Jugador.class, idJugador);

            if (partida == null || jugador == null) {
                System.out.println("No s'ha trobat la partida o el jugador");
                tx.rollback();
                return;
            }

            // Comprovar si la pila de robar esta buida
            if (partida.getPilaRobar().isEmpty()) {
                System.out.println("La pila de robar esta buida. Barallant cartes descartades...");

                // Agafar totes les cartes descartades i barrejar-les
                List<Carta> cartesDescartades = new ArrayList<>(partida.getPilaDescartades());
                Collections.shuffle(cartesDescartades);

                // Moure-les a la pila de robar
                partida.getPilaRobar().addAll(cartesDescartades);
                partida.getPilaDescartades().clear();

                session.merge(partida);
            }

            // Comprovar que hi ha cartes per robar, això probablement no passi però per si de cas
            if (partida.getPilaRobar().isEmpty()) {
                System.out.println("No hi ha cartes per robar!");
                tx.rollback();
                return;
            }

            // Robar la primera carta de la pila
            Carta carta = partida.getPilaRobar().remove(0);
            carta.setJugadorMa(jugador);
            jugador.getMa().add(carta);

            session.merge(carta);
            session.merge(jugador);
            session.merge(partida);
            tx.commit();

            System.out.println(jugador.getNom() + " roba: " + carta.getNom_carta() + " (" + carta.getColl() + ")");
            System.out.println("  -> Cartes a la ma ara: " + jugador.getMa().size());
            System.out.println("  -> Cartes restants a la pila: " + partida.getPilaRobar().size());

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void passarTorn(SessionFactory sessionFactory, int idPartida, int idJugador) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            // Obtenir la partida i el jugador
            Partida partida = session.get(Partida.class, idPartida);
            Jugador jugador = session.get(Jugador.class, idJugador);

            if (partida == null || jugador == null) {
                System.out.println("No s'ha trobat la partida o el jugador");
                tx.rollback();
                return;
            }

            // Obtenir les cartes de la ma
            List<Carta> cartesMa = jugador.getMa();
            int limitCartes = 4;

            // Comprovar si te mes cartes del limit
            if (cartesMa.size() > limitCartes) {
                int cartesADescartar = cartesMa.size() - limitCartes;
                System.out.println("\n" + jugador.getNom() + " te " + cartesMa.size() + " cartes!");
                System.out.println("Ha de descartar " + cartesADescartar + " carta/es");

                // Mostrar les cartes
                System.out.println("\n=== CARTES A LA MA ===");
                for (int i = 0; i < cartesMa.size(); i++) {
                    Carta carta = cartesMa.get(i);
                    System.out.println((i + 1) + ". " + carta.getNom_carta() + " (" + carta.getColl() + ")");
                }

                // Demanar a l'usuari quines cartes vol descartar
                Scanner scanner = new Scanner(System.in);
                List<Carta> cartesDescartades = new ArrayList<>();

                for (int i = 0; i < cartesADescartar; i++) {
                    System.out.print("\nEscull la carta " + (i + 1) + " a descartar (1-" + cartesMa.size() + "): ");
                    int opcio = scanner.nextInt();

                    if (opcio >= 1 && opcio <= cartesMa.size()) {
                        Carta cartaDescartada = cartesMa.get(opcio - 1);

                        // Verificar que no s'hagi descartat ja
                        if (!cartesDescartades.contains(cartaDescartada)) {
                            cartesDescartades.add(cartaDescartada);
                            System.out.println("Descartaras: " + cartaDescartada.getNom_carta());
                        } else {
                            System.out.println("Aquesta carta ja esta seleccionada! Torna a escollir.");
                            i--;
                        }
                    } else {
                        System.out.println("Opcio no valida! Torna a escollir.");
                        i--;
                    }
                }

                // Descartar les cartes seleccionades
                for (Carta carta : cartesDescartades) {
                    carta.setJugadorMa(null);
                    jugador.getMa().remove(carta);
                    partida.getPilaDescartades().add(carta);
                    session.merge(carta);
                    System.out.println(jugador.getNom() + " descarta: " + carta.getNom_carta());
                }

                session.merge(jugador);
                session.merge(partida);
            }

            // Passar al següent jugador viu
            List<Jugador> jugadors = partida.getJugadors();
            int indexActual = jugadors.indexOf(jugador);
            int indexSeguent = (indexActual + 1) % jugadors.size();

            // Buscar el següent jugador viu
            int intents = 0;
            while (intents < jugadors.size()) {
                Jugador seguent = jugadors.get(indexSeguent);

                if (seguent.getVidaActual() > 0) {
                    partida.setJugadorActual(seguent);
                    session.merge(partida);
                    System.out.println("\n>>> Torn de " + jugador.getNom() + " finalitzat <<<");
                    System.out.println(">>> Ara es el torn de: " + seguent.getNom() + " (Vida: " + seguent.getVidaActual() + "/" + seguent.getVidaMaxima() + ") <<<");
                    break;
                }

                indexSeguent = (indexSeguent + 1) % jugadors.size();
                intents++;
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void equiparCarta(SessionFactory sessionFactory, int idJugador, int idCarta) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            // Obtenir el jugador i la carta
            Jugador jugador = session.get(Jugador.class, idJugador);
            Carta carta = session.get(Carta.class, idCarta);

            if (jugador == null || carta == null) {
                System.out.println("No s'ha trobat el jugador o la carta");
                tx.rollback();
                return;
            }

            // Comprovar que la carta estigui a la ma del jugador
            if (carta.getJugadorMa() == null || carta.getJugadorMa().getId() != idJugador) {
                System.out.println("El jugador no te aquesta carta a la ma!");
                tx.rollback();
                return;
            }

            // Si es una arma
            if (carta instanceof CartaArma) {
                CartaArma arma = (CartaArma) carta;

                // Si ja te una arma equipada, treure-la i no descartar-la (queda a la ma)
                if (jugador.getArmaEquipada() != null) {
                    CartaArma armaAnterior = jugador.getArmaEquipada();
                    System.out.println("Desequipa: " + armaAnterior.getNom_carta());
                    jugador.setArmaEquipada(null);
                    session.merge(jugador);
                }

                // Equipar la nova arma
                arma.setJugadorMa(null);
                jugador.getMa().remove(arma);
                jugador.setArmaEquipada(arma);
                session.merge(arma);
                session.merge(jugador);

                System.out.println(jugador.getNom() + " equipa arma: " + arma.getNom_carta() +
                                 " (Distancia: " + arma.getDistanciaArma() + ")");
                System.out.println("  -> Ara pot atacar a distancia: " + arma.getDistanciaArma());

            // Si es un equipament
            } else if (carta instanceof CartaEquipament) {
                CartaEquipament equipament = (CartaEquipament) carta;

                // Comprovar que no tingui ja un equipament del mateix tipus
                boolean teEquipamentDelTipus = false;
                for (CartaEquipament eq : jugador.getEquipaments()) {
                    if (eq.getTipus() == equipament.getTipus()) {
                        teEquipamentDelTipus = true;
                        break;
                    }
                }

                if (teEquipamentDelTipus) {
                    System.out.println("Ja te un equipament d'aquest tipus!");
                    tx.rollback();
                    return;
                }

                // Equipar l'equipament
                equipament.setJugadorMa(null);
                jugador.getMa().remove(equipament);
                equipament.setJugadorEquipament(jugador);
                jugador.getEquipaments().add(equipament);

                // Actualitzar modificadors de distancia segons el tipus
                if (equipament.getTipus() == TipusEquipament.MIRA_TELESCOPICA) {
                    jugador.setModificadorDistanciaOff(jugador.getModificadorDistanciaOff() - 1);
                } else if (equipament.getTipus() == TipusEquipament.CAVALL) {
                    jugador.setModificadorDistanciaDef(jugador.getModificadorDistanciaDef() + 1);
                }

                session.merge(equipament);
                session.merge(jugador);

                System.out.println(jugador.getNom() + " equipa: " + equipament.getNom_carta());
                System.out.println("  -> Modificador distancia ofensiva: " + jugador.getModificadorDistanciaOff());
                System.out.println("  -> Modificador distancia defensiva: " + jugador.getModificadorDistanciaDef());
            } else {
                System.out.println("Aquesta carta no es pot equipar!");
                tx.rollback();
                return;
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public int calcularDistancia(SessionFactory sessionFactory, int idJugadorOrigen, int idJugadorDesti) {
        try (Session session = sessionFactory.openSession()) {
            // Obtenir els jugadors
            Jugador origen = session.get(Jugador.class, idJugadorOrigen);
            Jugador desti = session.get(Jugador.class, idJugadorDesti);

            if (origen == null || desti == null) {
                System.out.println("No s'han trobat els jugadors");
                return 0;
            }

            // Obtenir la distancia base de la BD
            // Provar primer amb origen com jugador1
            List<DistanciesJugadors> distancies = session.createQuery(
                    "FROM DistanciesJugadors d WHERE " +
                    "(d.jugador1.id = :id1 AND d.jugador2.id = :id2) OR " +
                    "(d.jugador1.id = :id2 AND d.jugador2.id = :id1)",
                    DistanciesJugadors.class)
                    .setParameter("id1", idJugadorOrigen)
                    .setParameter("id2", idJugadorDesti)
                    .getResultList();

            if (distancies.isEmpty()) {
                System.out.println("No s'ha trobat la distancia entre els jugadors");
                return 0;
            }

            // Obtenir la distancia base
            int distanciaBase = distancies.get(0).getDistancia();

            // Aplicar modificadors
            int distanciaFinal = distanciaBase;
            distanciaFinal += origen.getModificadorDistanciaOff(); // Mira telescopica (resta)
            distanciaFinal += desti.getModificadorDistanciaDef();   // Mustang (suma)

            // La distancia minima es 1
            if (distanciaFinal < 1) {
                distanciaFinal = 1;
            }

            return distanciaFinal;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public boolean comprovarDistanciaAtac(SessionFactory sessionFactory, int idJugadorAtacant, int idJugadorObjectiu) {
        try (Session session = sessionFactory.openSession()) {
            // Obtenir l'atacant
            Jugador atacant = session.get(Jugador.class, idJugadorAtacant);

            if (atacant == null) {
                System.out.println("No s'ha trobat l'atacant");
                return false;
            }

            // Obtenir l'abast de l'arma (per defecte 1 si no te arma)
            int abastArma = 1;
            if (atacant.getArmaEquipada() != null) {
                abastArma = atacant.getArmaEquipada().getDistanciaArma();
            }

            // Calcular la distancia real
            int distancia = calcularDistancia(sessionFactory, idJugadorAtacant, idJugadorObjectiu);

            // Comprovar si pot atacar
            boolean potAtacar = distancia <= abastArma;

            System.out.println("  -> Distancia real: " + distancia);
            System.out.println("  -> Abast arma atacant: " + abastArma);
            System.out.println("  -> Pot atacar: " + (potAtacar ? "SI" : "NO"));

            return potAtacar;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

