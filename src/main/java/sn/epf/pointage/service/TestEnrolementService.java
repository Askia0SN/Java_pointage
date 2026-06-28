package sn.epf.pointage.service;

import sn.epf.pointage.config.HibernateConfig;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.UtilisateurDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.model.enums.TypeContrat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestEnrolementService {

    public static void main(String[] args) {
        EnrolementService enrolementService = new EnrolementService();
        ProfesseurDAO professeurDAO = new ProfesseurDAO();
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Professeur professeur = null;

        try {
            String suffix = String.valueOf(System.currentTimeMillis());

            professeur = enrolementService.enrollerProfesseur(
                    "Diop",
                    "Awa",
                    "awa.diop." + suffix + "@epf.sn",
                    "+221771234567",
                    TypeContrat.VACATAIRE,
                    new BigDecimal("20000"),
                    LocalDate.now(),
                    null,
                    "Passerelle@123"
            );

            Utilisateur utilisateur = utilisateurDAO.findByProfesseurId(professeur.getId())
                    .orElseThrow(() -> new IllegalStateException("Compte utilisateur non cree."));

            if (utilisateur.getRole() != Role.PROFESSEUR) {
                throw new IllegalStateException("Role utilisateur incorrect.");
            }

            System.out.println("EnrolementService OK");
            System.out.println("Matricule genere : " + professeur.getMatricule());
            System.out.println("Login cree : " + utilisateur.getLogin());
        } finally {
            if (professeur != null && professeur.getId() != null) {
                utilisateurDAO.findByProfesseurId(professeur.getId())
                        .ifPresent(utilisateur -> utilisateurDAO.delete(utilisateur.getId()));
                professeurDAO.delete(professeur.getId());
            }

            HibernateConfig.shutdown();
        }
    }
}
