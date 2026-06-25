package sn.epf.pointage.config;

import org.hibernate.Session;

public class TestHibernate {

    public static void main(String[] args) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            System.out.println("Connexion Hibernate OK");
        } finally {
            HibernateConfig.shutdown();
        }
    }
}
