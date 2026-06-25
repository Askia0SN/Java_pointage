package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;

import java.util.List;
import java.util.Optional;

public class UtilisateurDAO extends AbstractDAO<Utilisateur, Long> {

    public UtilisateurDAO() {
        super(Utilisateur.class);
    }

    public Optional<Utilisateur> findByLogin(String login) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Utilisateur u where u.login = :login",
                            Utilisateur.class
                    )
                    .setParameter("login", login)
                    .uniqueResultOptional();
        }
    }

    public List<Utilisateur> findByRole(Role role) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "from Utilisateur u where u.role = :role order by u.login",
                            Utilisateur.class
                    )
                    .setParameter("role", role)
                    .getResultList();
        }
    }
}
