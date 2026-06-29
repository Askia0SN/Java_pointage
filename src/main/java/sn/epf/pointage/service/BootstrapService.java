package sn.epf.pointage.service;

import sn.epf.pointage.dao.UtilisateurDAO;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.security.PasswordService;

public class BootstrapService {

    public static final String LOGIN_ADMIN_DEFAUT = "admin";
    public static final String MOT_DE_PASSE_ADMIN_DEFAUT = "admin123";
    public static final String LOGIN_SCOLARITE_DEFAUT = "scolarite";
    public static final String MOT_DE_PASSE_SCOLARITE_DEFAUT = "scolarite123";

    private final UtilisateurDAO utilisateurDAO;
    private final PasswordService passwordService;

    public BootstrapService() {
        this(new UtilisateurDAO(), new PasswordService());
    }

    public BootstrapService(UtilisateurDAO utilisateurDAO, PasswordService passwordService) {
        this.utilisateurDAO = utilisateurDAO;
        this.passwordService = passwordService;
    }

    public void creerAdminParDefautSiAbsent() {
        creerUtilisateurParDefautSiAbsent(LOGIN_ADMIN_DEFAUT, MOT_DE_PASSE_ADMIN_DEFAUT, Role.ADMIN);
        creerUtilisateurParDefautSiAbsent(LOGIN_SCOLARITE_DEFAUT, MOT_DE_PASSE_SCOLARITE_DEFAUT, Role.SCOLARITE);
    }

    private void creerUtilisateurParDefautSiAbsent(String login, String motDePasse, Role role) {
        if (utilisateurDAO.findByLogin(login).isPresent()) {
            return;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(login);
        utilisateur.setMotDePasseHash(passwordService.hash(motDePasse));
        utilisateur.setRole(role);
        utilisateur.setActif(true);

        utilisateurDAO.save(utilisateur);
    }
}
