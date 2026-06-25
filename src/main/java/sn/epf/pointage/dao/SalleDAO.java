package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Salle;

import java.util.List;
import java.util.Optional;

public class SalleDAO extends AbstractDAO<Salle, Long> {

    public SalleDAO() {
        super(Salle.class);
    }

    public Optional<Salle> findByNom(String nom) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Salle s where s.nom = :nom",
                            Salle.class
                    )
                    .setParameter("nom", nom)
                    .uniqueResultOptional();
        }
    }

    public List<Salle> findByCapaciteMinimum(int capaciteMinimum) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Salle s where s.capacite >= :capaciteMinimum order by s.capacite, s.nom",
                            Salle.class
                    )
                    .setParameter("capaciteMinimum", capaciteMinimum)
                    .getResultList();
        }
    }
}
