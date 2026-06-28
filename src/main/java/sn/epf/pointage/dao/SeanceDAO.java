package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.StatutSeance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class SeanceDAO extends AbstractDAO<SeancePlanifiee, Long> {

    public SeanceDAO() {
        super(SeancePlanifiee.class);
    }

    public List<SeancePlanifiee> findSeancesDuJour() {
        LocalDate today = LocalDate.now();
        return findBetween(today.atStartOfDay(), today.atTime(LocalTime.MAX));
    }

    public List<SeancePlanifiee> findAllWithDetails() {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            select s
                            from SeancePlanifiee s
                            join fetch s.assignation a
                            join fetch a.professeur
                            join fetch a.cours
                            join fetch a.salle
                            order by s.dateHeure
                            """,
                            SeancePlanifiee.class
                    )
                    .getResultList();
        }
    }

    public List<SeancePlanifiee> findSeancesDuJourByProfesseur(Long professeurId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            select s
                            from SeancePlanifiee s
                            join fetch s.assignation a
                            join fetch a.cours
                            join fetch a.professeur
                            where a.professeur.id = :professeurId
                              and s.dateHeure between :start and :end
                            order by s.dateHeure
                            """,
                            SeancePlanifiee.class
                    )
                    .setParameter("professeurId", professeurId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        }
    }

    public List<SeancePlanifiee> findBetween(LocalDateTime start, LocalDateTime end) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from SeancePlanifiee s
                            where s.dateHeure between :start and :end
                            order by s.dateHeure
                            """,
                            SeancePlanifiee.class
                    )
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        }
    }

    public List<SeancePlanifiee> findSansPointage() {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            select s
                            from SeancePlanifiee s
                            where s.pointages is empty
                            order by s.dateHeure
                            """,
                            SeancePlanifiee.class
                    )
                    .getResultList();
        }
    }

    public List<SeancePlanifiee> findFuturePlanifieesByProfesseur(Long professeurId, LocalDateTime fromDateTime) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from SeancePlanifiee s
                            where s.assignation.professeur.id = :professeurId
                              and s.dateHeure >= :fromDateTime
                              and s.statut = :statut
                            order by s.dateHeure
                            """,
                            SeancePlanifiee.class
                    )
                    .setParameter("professeurId", professeurId)
                    .setParameter("fromDateTime", fromDateTime)
                    .setParameter("statut", StatutSeance.PLANIFIEE)
                    .getResultList();
        }
    }

    public List<SeancePlanifiee> findPlanifieesByProfesseurAndMois(Long professeurId, int mois, int annee) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from SeancePlanifiee s
                            where s.assignation.professeur.id = :professeurId
                              and month(s.dateHeure) = :mois
                              and year(s.dateHeure) = :annee
                            order by s.dateHeure
                            """,
                            SeancePlanifiee.class
                    )
                    .setParameter("professeurId", professeurId)
                    .setParameter("mois", mois)
                    .setParameter("annee", annee)
                    .getResultList();
        }
    }

    public List<SeancePlanifiee> findByStatutAndMois(StatutSeance statut, int mois, int annee) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from SeancePlanifiee s
                            where s.statut = :statut
                              and month(s.dateHeure) = :mois
                              and year(s.dateHeure) = :annee
                            order by s.dateHeure
                            """,
                            SeancePlanifiee.class
                    )
                    .setParameter("statut", statut)
                    .setParameter("mois", mois)
                    .setParameter("annee", annee)
                    .getResultList();
        }
    }

    public List<SeancePlanifiee> findByProfesseurStatutAndMois(
            Long professeurId,
            StatutSeance statut,
            int mois,
            int annee
    ) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from SeancePlanifiee s
                            where s.assignation.professeur.id = :professeurId
                              and s.statut = :statut
                              and month(s.dateHeure) = :mois
                              and year(s.dateHeure) = :annee
                            order by s.dateHeure
                            """,
                            SeancePlanifiee.class
                    )
                    .setParameter("professeurId", professeurId)
                    .setParameter("statut", statut)
                    .setParameter("mois", mois)
                    .setParameter("annee", annee)
                    .getResultList();
        }
    }
}
