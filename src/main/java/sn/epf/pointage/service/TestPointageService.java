package sn.epf.pointage.service;

import sn.epf.pointage.config.HibernateConfig;
import sn.epf.pointage.dao.AssignationDAO;
import sn.epf.pointage.dao.CoursDAO;
import sn.epf.pointage.dao.PointageDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SalleDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Assignation;
import sn.epf.pointage.model.Cours;
import sn.epf.pointage.model.Pointage;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.Salle;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.ResultatPointage;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.model.enums.TypeContrat;
import sn.epf.pointage.model.enums.TypePointage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestPointageService {

    public static void main(String[] args) {
        ProfesseurDAO professeurDAO = new ProfesseurDAO();
        CoursDAO coursDAO = new CoursDAO();
        SalleDAO salleDAO = new SalleDAO();
        AssignationDAO assignationDAO = new AssignationDAO();
        SeanceDAO seanceDAO = new SeanceDAO();
        PointageDAO pointageDAO = new PointageDAO();
        PointageService pointageService = new PointageService();

        Professeur professeur = null;
        Cours cours = null;
        Salle salle = null;
        Assignation assignation = null;
        SeancePlanifiee seance = null;

        try {
            String suffix = String.valueOf(System.currentTimeMillis());

            professeur = new Professeur();
            professeur.setMatricule("PTG-" + suffix);
            professeur.setNom("Ndiaye");
            professeur.setPrenom("Moussa");
            professeur.setEmail("moussa.ndiaye." + suffix + "@epf.sn");
            professeur.setTypeContrat(TypeContrat.VACATAIRE);
            professeur.setTauxHoraireXOF(new BigDecimal("18000"));
            professeur.setDateEmbauche(LocalDate.now());
            professeur.setActif(true);
            professeurDAO.save(professeur);

            cours = new Cours();
            cours.setCode("JAVA-" + suffix);
            cours.setIntitule("Java avance");
            cours.setFiliere("CSI");
            cours.setNiveauEtude("L3");
            cours.setSemestre("S6");
            cours.setVolumeHoraireTotal(new BigDecimal("30"));
            coursDAO.save(cours);

            salle = new Salle();
            salle.setNom("S-" + suffix);
            salle.setCapacite(35);
            salle.setBatiment("Principal");
            salleDAO.save(salle);

            assignation = new Assignation();
            assignation.setProfesseur(professeur);
            assignation.setCours(cours);
            assignation.setSalle(salle);
            assignation.setAnneeAcademique("2025-2026");
            assignation.setHeuresPrevues(new BigDecimal("30"));
            assignationDAO.save(assignation);

            seance = new SeancePlanifiee();
            seance.setAssignation(assignation);
            seance.setDateHeure(LocalDateTime.now().minusMinutes(2));
            seance.setDureeMinutes(120);
            seance.setStatut(StatutSeance.PLANIFIEE);
            seanceDAO.save(seance);

            ResultatPointage resultat = pointageService.pointer(
                    seance.getId(),
                    professeur.getId(),
                    TypePointage.DEBUT
            );

            if (resultat != ResultatPointage.SUCCES) {
                throw new IllegalStateException("Resultat inattendu : " + resultat);
            }

            SeancePlanifiee seanceRelue = seanceDAO.findById(seance.getId())
                    .orElseThrow(() -> new IllegalStateException("Seance introuvable apres pointage."));

            if (seanceRelue.getStatut() != StatutSeance.REALISEE) {
                throw new IllegalStateException("La seance n'est pas passee en REALISEE.");
            }

            System.out.println("PointageService OK");
            System.out.println("Resultat : " + resultat);
            System.out.println("Statut seance : " + seanceRelue.getStatut());
        } finally {
            if (seance != null && seance.getId() != null) {
                pointageDAO.findBySeanceProfesseurAndType(seance.getId(), professeur.getId(), TypePointage.DEBUT)
                        .map(Pointage::getId)
                        .ifPresent(pointageDAO::delete);
                seanceDAO.delete(seance.getId());
            }

            if (assignation != null && assignation.getId() != null) {
                assignationDAO.delete(assignation.getId());
            }

            if (salle != null && salle.getId() != null) {
                salleDAO.delete(salle.getId());
            }

            if (cours != null && cours.getId() != null) {
                coursDAO.delete(cours.getId());
            }

            if (professeur != null && professeur.getId() != null) {
                professeurDAO.delete(professeur.getId());
            }

            HibernateConfig.shutdown();
        }
    }
}
