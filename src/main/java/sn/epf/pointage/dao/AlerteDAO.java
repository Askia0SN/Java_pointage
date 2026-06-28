package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Alerte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AlerteDAO extends AbstractDAO<Alerte, Long> {

    public AlerteDAO() {
        super(Alerte.class);
    }

    public List<Alerte> findNonLues() {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Alerte a where a.lue = false order by a.dateCreation desc",
                            Alerte.class
                    )
                    .getResultList();
        }
    }

    public List<Alerte> findAlertesDuJour() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from Alerte a
                            where a.dateCreation between :start and :end
                            order by a.dateCreation desc
                            """,
                            Alerte.class
                    )
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        }
    }
}
