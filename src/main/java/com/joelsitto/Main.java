package com.joelsitto;


import org.hibernate.Session;
import org.hibernate.Transaction;
import com.joelsitto.bang.util.HibernateUtil;
import com.joelsitto.bang.model.*;

import java.util.Date;

public class Main {

    public static void main(String[] args) {

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        try {
            Rol sheriff = new Rol();
            sheriff.setObjectiu("Proteger el pueblo y eliminar a los bandidos");

            Jugador jugador1 = new Jugador();
            jugador1.setNom("Roberto");
            jugador1.setVidaActual(5);
            jugador1.setVidaMaxima(5);
            jugador1.setModificadorDistanciaDef(0);
            jugador1.setModificadorDistanciaOff(0);
            jugador1.setRol(sheriff);

            Partida partida = new Partida();
            partida.setEstat("En curs");
            partida.setDataInici(new Date());
            partida.getJugadors().add(jugador1);

            // Relación bidireccional (opcional)
            session.persist(sheriff);
            session.persist(jugador1);
            session.persist(partida);

            tx.commit();

            System.out.println("✅ Base de dades creada i dades inicials inserides correctament!");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
            HibernateUtil.shutdown();
        }
    }
}
