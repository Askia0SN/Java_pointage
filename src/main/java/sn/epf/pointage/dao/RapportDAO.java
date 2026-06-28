package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.enums.StatutRapport;

import java.util.List;
import java.util.Optional;

public class RapportDAO extends AbstractDAO<RapportMensuel, Long> {

    public RapportDAO() {
        super(RapportMensuel.class);
    }

    public Optional<RapportMensuel> findByProfesseurAndPeriode(Long professeurId, int mois, int annee) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            select r
                            from RapportMensuel r
                            join fetch r.professeur
                            where r.professeur.id = :professeurId
                              and r.mois = :mois
                              and r.annee = :annee
                            """,
                            RapportMensuel.class
                    )
                    .setParameter("professeurId", professeurId)
                    .setParameter("mois", mois)
                    .setParameter("annee", annee)
                    .uniqueResultOptional();
        }
    }

    public List<RapportMensuel> findByPeriode(int mois, int annee) {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            select r
                            from RapportMensuel r
                            join fetch r.professeur
                            where r.mois = :mois
                              and r.annee = :annee
                            order by r.professeur.nom, r.professeur.prenom
                            """,
                            RapportMensuel.class
                    )
                    .setParameter("mois", mois)
                    .setParameter("annee", annee)
                    .getResultList();
        }
    }

    public List<RapportMensuel> findNonPayes() {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            select r
                            from RapportMensuel r
                            join fetch r.professeur
                            where r.statut <> :statutPaye
                            order by r.annee desc, r.mois desc
                            """,
                            RapportMensuel.class
                    )
                    .setParameter("statutPaye", StatutRapport.PAYE)
                    .getResultList();
        }
    }

    public List<RapportMensuel> findAllWithProfesseur() {
        try (Session session = openSession()) {
            return session.createQuery(
                            """
                            select r
                            from RapportMensuel r
                            join fetch r.professeur
                            order by r.annee desc, r.mois desc, r.professeur.nom, r.professeur.prenom
                            """,
                            RapportMensuel.class
                    )
                    .getResultList();
        }
    }
}
