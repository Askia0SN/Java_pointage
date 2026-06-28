package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.JournalConnexion;

import java.util.List;

public class JournalConnexionDAO extends AbstractDAO<JournalConnexion, Long> {

    public JournalConnexionDAO() {
        super(JournalConnexion.class);
    }

    public List<JournalConnexion> findByUtilisateurId(Long utilisateurId) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            from JournalConnexion j
                            where j.utilisateur.id = :utilisateurId
                            order by j.horodatage desc
                            """,
                            JournalConnexion.class
                    )
                    .setParameter("utilisateurId", utilisateurId)
                    .getResultList();
        }
    }
}
