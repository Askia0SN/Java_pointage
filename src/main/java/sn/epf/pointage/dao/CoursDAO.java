package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Cours;

import java.util.List;
import java.util.Optional;

public class CoursDAO extends AbstractDAO<Cours, Long> {

    public CoursDAO() {
        super(Cours.class);
    }

    public Optional<Cours> findByCode(String code) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Cours c where c.code = :code",
                            Cours.class
                    )
                    .setParameter("code", code)
                    .uniqueResultOptional();
        }
    }

    public List<Cours> findByFiliere(String filiere) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Cours c where lower(c.filiere) = lower(:filiere) order by c.intitule",
                            Cours.class
                    )
                    .setParameter("filiere", filiere)
                    .getResultList();
        }
    }
}
