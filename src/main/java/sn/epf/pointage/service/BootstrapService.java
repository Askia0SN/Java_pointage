package sn.epf.pointage.service;

import sn.epf.pointage.dao.UtilisateurDAO;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.security.PasswordService;

public class BootstrapService {

    public static final String LOGIN_ADMIN_DEFAUT = "admin";
    public static final String MOT_DE_PASSE_ADMIN_DEFAUT = "admin123";

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
        if (utilisateurDAO.findByLogin(LOGIN_ADMIN_DEFAUT).isPresent()) {
            return;
        }

        Utilisateur admin = new Utilisateur();
        admin.setLogin(LOGIN_ADMIN_DEFAUT);
        admin.setMotDePasseHash(passwordService.hash(MOT_DE_PASSE_ADMIN_DEFAUT));
        admin.setRole(Role.ADMIN);
        admin.setActif(true);

        utilisateurDAO.save(admin);
    }
}
