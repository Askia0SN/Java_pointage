package sn.epf.pointage.dao;

import sn.epf.pointage.config.HibernateConfig;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.enums.TypeContrat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestProfesseurDAO {

    public static void main(String[] args) {
        ProfesseurDAO professeurDAO = new ProfesseurDAO();
        String suffix = String.valueOf(System.currentTimeMillis());
        String matricule = "TEST-" + suffix;

        Professeur professeur = new Professeur();
        professeur.setMatricule(matricule);
        professeur.setNom("Test");
        professeur.setPrenom("DAO");
        professeur.setEmail("test.dao." + suffix + "@epf.sn");
        professeur.setTelephone("+221770000000");
        professeur.setTypeContrat(TypeContrat.VACATAIRE);
        professeur.setTauxHoraireXOF(new BigDecimal("15000"));
        professeur.setDateEmbauche(LocalDate.now());
        professeur.setActif(true);

        try {
            professeurDAO.save(professeur);

            Professeur professeurRelu = professeurDAO.findById(professeur.getId())
                    .orElseThrow(() -> new IllegalStateException("Professeur introuvable apres sauvegarde"));

            Professeur professeurParMatricule = professeurDAO.findByMatricule(matricule)
                    .orElseThrow(() -> new IllegalStateException("Recherche par matricule echouee"));

            System.out.println("DAO Professeur OK");
            System.out.println("ID cree : " + professeurRelu.getId());
            System.out.println("Matricule : " + professeurParMatricule.getMatricule());
        } finally {
            if (professeur.getId() != null) {
                professeurDAO.delete(professeur.getId());
            }

            HibernateConfig.shutdown();
        }
    }
}
