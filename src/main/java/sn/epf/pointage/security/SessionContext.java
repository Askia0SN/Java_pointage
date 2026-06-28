package sn.epf.pointage.security;

import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

public final class SessionContext {

    private static final SessionContext INSTANCE = new SessionContext();

    private Utilisateur utilisateurConnecte;
    private LocalDateTime dateConnexion;

    private SessionContext() {
    }

    public static SessionContext getInstance() {
        return INSTANCE;
    }

    public void ouvrirSession(Utilisateur utilisateur) {
        if (utilisateur == null) {
            throw new IllegalArgumentException("L'utilisateur ne peut pas etre null.");
        }

        this.utilisateurConnecte = utilisateur;
        this.dateConnexion = LocalDateTime.now();
    }

    public void fermerSession() {
        this.utilisateurConnecte = null;
        this.dateConnexion = null;
    }

    public boolean isConnecte() {
        return utilisateurConnecte != null;
    }

    public Optional<Utilisateur> getUtilisateurConnecte() {
        return Optional.ofNullable(utilisateurConnecte);
    }

    public Optional<Role> getRole() {
        return getUtilisateurConnecte().map(Utilisateur::getRole);
    }

    public Optional<Professeur> getProfesseurLie() {
        return getUtilisateurConnecte().map(Utilisateur::getProfesseurLie);
    }

    public Optional<LocalDateTime> getDateConnexion() {
        return Optional.ofNullable(dateConnexion);
    }

    public boolean hasRole(Role... rolesAutorises) {
        return getRole()
                .map(role -> Arrays.asList(rolesAutorises).contains(role))
                .orElse(false);
    }

    public void verifierRole(Role... rolesAutorises) {
        if (!hasRole(rolesAutorises)) {
            throw new SecurityException("Acces refuse pour le role courant.");
        }
    }
}
