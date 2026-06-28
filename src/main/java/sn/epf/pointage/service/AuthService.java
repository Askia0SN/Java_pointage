package sn.epf.pointage.service;

import sn.epf.pointage.dao.JournalConnexionDAO;
import sn.epf.pointage.dao.UtilisateurDAO;
import sn.epf.pointage.model.JournalConnexion;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.ActionConnexion;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.security.PasswordService;
import sn.epf.pointage.security.SessionContext;

import java.time.LocalDateTime;

public class AuthService {

    private final UtilisateurDAO utilisateurDAO;
    private final JournalConnexionDAO journalConnexionDAO;
    private final PasswordService passwordService;
    private final SessionContext sessionContext;

    public AuthService() {
        this(new UtilisateurDAO(), new JournalConnexionDAO(), new PasswordService(), SessionContext.getInstance());
    }

    public AuthService(
            UtilisateurDAO utilisateurDAO,
            JournalConnexionDAO journalConnexionDAO,
            PasswordService passwordService,
            SessionContext sessionContext
    ) {
        this.utilisateurDAO = utilisateurDAO;
        this.journalConnexionDAO = journalConnexionDAO;
        this.passwordService = passwordService;
        this.sessionContext = sessionContext;
    }

    public Utilisateur connecter(String login, String motDePasse, String adresseIP) {
        if (isBlank(login) || isBlank(motDePasse)) {
            throw new IllegalArgumentException("Login et mot de passe sont obligatoires.");
        }

        Utilisateur utilisateur = utilisateurDAO.findByLogin(login.trim())
                .orElseThrow(() -> new SecurityException("Login ou mot de passe incorrect."));

        if (!utilisateur.isActif()) {
            journaliser(utilisateur, ActionConnexion.ECHEC_CONNEXION, adresseIP);
            throw new SecurityException("Compte utilisateur inactif.");
        }

        if (!passwordService.matches(motDePasse, utilisateur.getMotDePasseHash())) {
            journaliser(utilisateur, ActionConnexion.ECHEC_CONNEXION, adresseIP);
            throw new SecurityException("Login ou mot de passe incorrect.");
        }

        journaliser(utilisateur, ActionConnexion.CONNEXION, adresseIP);
        sessionContext.ouvrirSession(utilisateur);

        return utilisateur;
    }

    public void deconnecter(String adresseIP) {
        sessionContext.getUtilisateurConnecte()
                .ifPresent(utilisateur -> journaliser(utilisateur, ActionConnexion.DECONNEXION, adresseIP));

        sessionContext.fermerSession();
    }

    public boolean estConnecte() {
        return sessionContext.isConnecte();
    }

    public void verifierAcces(Role... rolesAutorises) {
        sessionContext.verifierRole(rolesAutorises);
    }

    private void journaliser(Utilisateur utilisateur, ActionConnexion action, String adresseIP) {
        JournalConnexion journal = new JournalConnexion();
        journal.setUtilisateur(utilisateur);
        journal.setAction(action);
        journal.setHorodatage(LocalDateTime.now());
        journal.setAdresseIP(adresseIP);

        journalConnexionDAO.save(journal);
    }

    private boolean isBlank(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
