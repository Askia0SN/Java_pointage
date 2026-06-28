package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Professeur;

import java.util.List;
import java.util.Optional;

public class ProfesseurDAO extends AbstractDAO<Professeur, Long> {

    public ProfesseurDAO() {
        super(Professeur.class);
    }

    public Optional<Professeur> findByMatricule(String matricule) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Professeur p where p.matricule = :matricule",
                            Professeur.class
                    )
                    .setParameter("matricule", matricule)
                    .uniqueResultOptional();
        }
    }

    public Optional<Professeur> findByEmail(String email) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Professeur p where lower(p.email) = lower(:email)",
                            Professeur.class
                    )
                    .setParameter("email", email)
                    .uniqueResultOptional();
        }
    }

    public long countByMatriculePrefix(String matriculePrefix) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "select count(p) from Professeur p where p.matricule like :prefix",
                            Long.class
                    )
                    .setParameter("prefix", matriculePrefix + "%")
                    .getSingleResult();
        }
    }

    public List<Professeur> findActifs() {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Professeur p where p.actif = true order by p.nom, p.prenom",
                            Professeur.class
                    )
                    .getResultList();
        }
    }

    public List<Professeur> searchByNom(String nom) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from Professeur p
                            where lower(p.nom) like lower(:nom)
                               or lower(p.prenom) like lower(:nom)
                            order by p.nom, p.prenom
                            """,
                            Professeur.class
                    )
                    .setParameter("nom", "%" + nom + "%")
                    .getResultList();
        }
    }

    public List<Professeur> findByFiliere(String filiere) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            select distinct p
                            from Professeur p
                            join p.assignations a
                            join a.cours c
                            where lower(c.filiere) = lower(:filiere)
                            order by p.nom, p.prenom
                            """,
                            Professeur.class
                    )
                    .setParameter("filiere", filiere)
                    .getResultList();
        }
    }
}
