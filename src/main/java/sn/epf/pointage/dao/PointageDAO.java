package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Pointage;
import sn.epf.pointage.model.enums.TypePointage;

import java.util.List;
import java.util.Optional;

public class PointageDAO extends AbstractDAO<Pointage, Long> {

    public PointageDAO() {
        super(Pointage.class);
    }

    public Optional<Pointage> findBySeanceAndType(Long seanceId, TypePointage typePointage) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from Pointage p
                            where p.seance.id = :seanceId
                              and p.typePointage = :typePointage
                            """,
                            Pointage.class
                    )
                    .setParameter("seanceId", seanceId)
                    .setParameter("typePointage", typePointage)
                    .uniqueResultOptional();
        }
    }

    public Optional<Pointage> findBySeanceProfesseurAndType(Long seanceId, Long professeurId, TypePointage typePointage) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from Pointage p
                            where p.seance.id = :seanceId
                              and p.professeur.id = :professeurId
                              and p.typePointage = :typePointage
                            """,
                            Pointage.class
                    )
                    .setParameter("seanceId", seanceId)
                    .setParameter("professeurId", professeurId)
                    .setParameter("typePointage", typePointage)
                    .uniqueResultOptional();
        }
    }

    public long countByProfesseurAndMois(Long professeurId, int mois, int annee) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            select count(p)
                            from Pointage p
                            where p.professeur.id = :professeurId
                              and month(p.heurePointage) = :mois
                              and year(p.heurePointage) = :annee
                            """,
                            Long.class
                    )
                    .setParameter("professeurId", professeurId)
                    .setParameter("mois", mois)
                    .setParameter("annee", annee)
                    .getSingleResult();
        }
    }

    public List<Pointage> findByProfesseurAndMois(Long professeurId, int mois, int annee) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from Pointage p
                            where p.professeur.id = :professeurId
                              and month(p.heurePointage) = :mois
                              and year(p.heurePointage) = :annee
                            order by p.heurePointage
                            """,
                            Pointage.class
                    )
                    .setParameter("professeurId", professeurId)
                    .setParameter("mois", mois)
                    .setParameter("annee", annee)
                    .getResultList();
        }
    }
}
