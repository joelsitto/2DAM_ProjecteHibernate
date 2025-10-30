package com.joelsitto;

import org.hibernate.Session;
import org.hibernate.Transaction;
import com.joelsitto.bang.util.HibernateUtil;
import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.*;

import java.util.Date;

public class Main {

    public static void main(String[] args) {

        System.out.println("=== CREANT BASE DE DADES HIBERNATE BANG ===\n");

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        try {
            // 1. CREAR ROLES
            System.out.println("1. Creant roles...");
            Rol sheriff = new Rol();
            sheriff.setObjectiu("Eliminar a tots els bandits i renegats");

            Rol renegat = new Rol();
            renegat.setObjectiu("Ser l'últim jugador en peu");

            Rol bandit = new Rol();
            bandit.setObjectiu("Eliminar al Sheriff");

            Rol ajudant = new Rol();
            ajudant.setObjectiu("Protegir al Sheriff");

            session.persist(sheriff);
            session.persist(renegat);
            session.persist(bandit);
            session.persist(ajudant);

            System.out.println("   ✓ 4 roles creats");

            // 2. CREAR JUGADORS
            System.out.println("\n2. Creant jugadors...");
            Jugador jugador1 = new Jugador();
            jugador1.setNom("Bart Cassidy");
            jugador1.setVidaActual(4);
            jugador1.setVidaMaxima(4);
            jugador1.setModificadorDistanciaDef(0);
            jugador1.setModificadorDistanciaOff(0);
            jugador1.setRol(sheriff);

            Jugador jugador2 = new Jugador();
            jugador2.setNom("Jesse Jones");
            jugador2.setVidaActual(4);
            jugador2.setVidaMaxima(4);
            jugador2.setModificadorDistanciaDef(0);
            jugador2.setModificadorDistanciaOff(0);
            jugador2.setRol(bandit);

            session.persist(jugador1);
            session.persist(jugador2);

            System.out.println("   ✓ 2 jugadors creats");

            // 3. CREAR CARTES ARMA
            System.out.println("\n3. Creant cartes d'arma...");
            CartaArma colt45 = new CartaArma();
            colt45.setNom_carta("Colt .45");
            colt45.setDescripcio_carta("Arma bàsica amb distància 1");
            colt45.setDistanciaArma(1);

            CartaArma winchester = new CartaArma();
            winchester.setNom_carta("Winchester");
            winchester.setDescripcio_carta("Rifle amb distància 5");
            winchester.setDistanciaArma(5);

            session.persist(colt45);
            session.persist(winchester);

            // Equipar arma al jugador1
            jugador1.setArmaEquipada(colt45);
            session.merge(jugador1);

            System.out.println("   ✓ 2 armes creades");

            // 4. CREAR CARTES D'EQUIPAMENT
            System.out.println("\n4. Creant cartes d'equipament...");
            CartaEquipament cavall = new CartaEquipament();
            cavall.setNom_carta("Mustang");
            cavall.setDescripcio_carta("Augmenta la distància dels altres jugadors");
            cavall.setTipus(TipusEquipament.CAVALL);
            cavall.setModificadorDistancia(1);
            cavall.setJugadorEquipament(jugador1);

            CartaEquipament barril = new CartaEquipament();
            barril.setNom_carta("Barril");
            barril.setDescripcio_carta("Permet esquivar Bang!");
            barril.setTipus(TipusEquipament.BARRIL);
            barril.setModificadorDistancia(0);
            barril.setJugadorEquipament(jugador2);

            session.persist(cavall);
            session.persist(barril);

            System.out.println("   ✓ 2 equipaments creats");

            // 5. CREAR CARTES D'ÚS
            System.out.println("\n5. Creant cartes d'ús...");
            CartaUs bang1 = new CartaUs();
            bang1.setNom_carta("Bang!");
            bang1.setDescripcio_carta("Dispara a un jugador a distància 1");
            bang1.setTipusUs(TipusUs.BANG);
            bang1.setJugadorMa(jugador1);

            CartaUs bang2 = new CartaUs();
            bang2.setNom_carta("Bang!");
            bang2.setDescripcio_carta("Dispara a un jugador a distància 1");
            bang2.setTipusUs(TipusUs.BANG);
            bang2.setJugadorMa(jugador1);

            CartaUs fallaste = new CartaUs();
            fallaste.setNom_carta("Fallaste!");
            fallaste.setDescripcio_carta("Esquiva un Bang!");
            fallaste.setTipusUs(TipusUs.FALLASTE);
            fallaste.setJugadorMa(jugador2);

            CartaUs birra = new CartaUs();
            birra.setNom_carta("Birra");
            birra.setDescripcio_carta("Recupera 1 punt de vida");
            birra.setTipusUs(TipusUs.BIRRA);
            birra.setJugadorMa(jugador2);

            session.persist(bang1);
            session.persist(bang2);
            session.persist(fallaste);
            session.persist(birra);

            System.out.println("   ✓ 4 cartes d'ús creades");

            // 6. CREAR PARTIDA
            System.out.println("\n6. Creant partida...");
            Partida partida = new Partida();
            partida.setEstat("En curs");
            partida.setDataInici(new Date());
            partida.getJugadors().add(jugador1);
            partida.getJugadors().add(jugador2);

            session.persist(partida);

            System.out.println("   ✓ 1 partida creada");

            // 7. CREAR DISTÀNCIES ENTRE JUGADORS
            System.out.println("\n7. Creant distàncies entre jugadors...");
            DistanciesJugadors distancia1 = new DistanciesJugadors();
            distancia1.setJugador1(jugador1);
            distancia1.setJugador2(jugador2);
            distancia1.setDistancia(1);

            DistanciesJugadors distancia2 = new DistanciesJugadors();
            distancia2.setJugador1(jugador2);
            distancia2.setJugador2(jugador1);
            distancia2.setDistancia(1);

            session.persist(distancia1);
            session.persist(distancia2);

            System.out.println("   ✓ Distàncies configurades");

            // COMMIT
            tx.commit();

            System.out.println("\n=== ✓ BASE DE DADES CREADA CORRECTAMENT! ===");
            System.out.println("\nResum:");
            System.out.println("- 4 Roles");
            System.out.println("- 2 Jugadors");
            System.out.println("- 2 Armes");
            System.out.println("- 2 Equipaments");
            System.out.println("- 4 Cartes d'ús");
            System.out.println("- 1 Partida");
            System.out.println("- 2 Relacions de distància");

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
                System.out.println("\n✗ ERROR! Transacció revertida.");
            }
            e.printStackTrace();
        } finally {
            session.close();
            HibernateUtil.shutdown();
        }
    }
}
