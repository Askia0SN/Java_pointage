package sn.epf.pointage.service;

import sn.epf.pointage.config.HibernateConfig;
import sn.epf.pointage.dao.JournalConnexionDAO;
import sn.epf.pointage.dao.UtilisateurDAO;
import sn.epf.pointage.model.JournalConnexion;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.ActionConnexion;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.security.PasswordService;
import sn.epf.pointage.security.SessionContext;

import java.util.List;

public class TestAuthService {

    public static void main(String[] args) {
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        JournalConnexionDAO journalConnexionDAO = new JournalConnexionDAO();
        PasswordService passwordService = new PasswordService();
        AuthService authService = new AuthService();
        Utilisateur utilisateur = null;

        try {
            String suffix = String.valueOf(System.currentTimeMillis());

            utilisateur = new Utilisateur();
            utilisateur.setLogin("admin.test." + suffix);
            utilisateur.setMotDePasseHash(passwordService.hash("Admin@123"));
            utilisateur.setRole(Role.ADMIN);
            utilisateur.setActif(true);
            utilisateurDAO.save(utilisateur);

            Utilisateur utilisateurConnecte = authService.connecter(
                    utilisateur.getLogin(),
                    "Admin@123",
                    "127.0.0.1"
            );

            if (!authService.estConnecte()) {
                throw new IllegalStateException("Session non ouverte apres connexion.");
            }

            if (utilisateurConnecte.getRole() != Role.ADMIN) {
                throw new IllegalStateException("Role incorrect apres connexion.");
            }

            authService.verifierAcces(Role.ADMIN);
            authService.deconnecter("127.0.0.1");

            if (SessionContext.getInstance().isConnecte()) {
                throw new IllegalStateException("Session encore ouverte apres deconnexion.");
            }

            List<JournalConnexion> journaux = journalConnexionDAO.findByUtilisateurId(utilisateur.getId());

            boolean connexionJournalisee = journaux.stream()
                    .anyMatch(journal -> journal.getAction() == ActionConnexion.CONNEXION);
            boolean deconnexionJournalisee = journaux.stream()
                    .anyMatch(journal -> journal.getAction() == ActionConnexion.DECONNEXION);

            if (!connexionJournalisee || !deconnexionJournalisee) {
                throw new IllegalStateException("Connexion/deconnexion non journalisee.");
            }

            System.out.println("AuthService OK");
            System.out.println("Login teste : " + utilisateur.getLogin());
            System.out.println("Journaux crees : " + journaux.size());
        } finally {
            if (utilisateur != null && utilisateur.getId() != null) {
                journalConnexionDAO.findByUtilisateurId(utilisateur.getId())
                        .stream()
                        .map(JournalConnexion::getId)
                        .forEach(journalConnexionDAO::delete);

                utilisateurDAO.delete(utilisateur.getId());
            }

            HibernateConfig.shutdown();
        }
    }
}
