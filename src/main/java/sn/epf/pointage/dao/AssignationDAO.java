package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Assignation;

import java.util.List;

public class AssignationDAO extends AbstractDAO<Assignation, Long> {

    public AssignationDAO() {
        super(Assignation.class);
    }

    public List<Assignation> findByProfesseur(Long professeurId) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from Assignation a
                            where a.professeur.id = :professeurId
                            order by a.anneeAcademique desc
                            """,
                            Assignation.class
                    )
                    .setParameter("professeurId", professeurId)
                    .getResultList();
        }
    }

    public List<Assignation> findByAnneeAcademique(String anneeAcademique) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Assignation a where a.anneeAcademique = :anneeAcademique",
                            Assignation.class
                    )
                    .setParameter("anneeAcademique", anneeAcademique)
                    .getResultList();
        }
    }
}
