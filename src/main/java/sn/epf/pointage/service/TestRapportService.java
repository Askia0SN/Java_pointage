package sn.epf.pointage.service;

import sn.epf.pointage.config.HibernateConfig;
import sn.epf.pointage.dao.AssignationDAO;
import sn.epf.pointage.dao.CoursDAO;
import sn.epf.pointage.dao.PointageDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.RapportDAO;
import sn.epf.pointage.dao.SalleDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Assignation;
import sn.epf.pointage.model.Cours;
import sn.epf.pointage.model.Pointage;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.Salle;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.StatutPointage;
import sn.epf.pointage.model.enums.StatutRapport;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.model.enums.TypeContrat;
import sn.epf.pointage.model.enums.TypePointage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestRapportService {

    public static void main(String[] args) {
        ProfesseurDAO professeurDAO = new ProfesseurDAO();
        CoursDAO coursDAO = new CoursDAO();
        SalleDAO salleDAO = new SalleDAO();
        AssignationDAO assignationDAO = new AssignationDAO();
        SeanceDAO seanceDAO = new SeanceDAO();
        PointageDAO pointageDAO = new PointageDAO();
        RapportDAO rapportDAO = new RapportDAO();
        RapportService rapportService = new RapportService();

        Professeur professeur = null;
        Cours cours = null;
        Salle salle = null;
        Assignation assignation = null;
        SeancePlanifiee seance = null;
        Pointage pointage = null;
        RapportMensuel rapport = null;

        try {
            String suffix = String.valueOf(System.currentTimeMillis());
            LocalDateTime dateSeance = LocalDateTime.now().withDayOfMonth(1).withHour(10).withMinute(0);

            professeur = new Professeur();
            professeur.setMatricule("RPT-" + suffix);
            professeur.setNom("Fall");
            professeur.setPrenom("Mariama");
            professeur.setEmail("mariama.fall." + suffix + "@epf.sn");
            professeur.setTypeContrat(TypeContrat.VACATAIRE);
            professeur.setTauxHoraireXOF(new BigDecimal("10000"));
            professeur.setDateEmbauche(LocalDate.now());
            professeur.setActif(true);
            professeurDAO.save(professeur);

            cours = new Cours();
            cours.setCode("RAP-" + suffix);
            cours.setIntitule("Rapports Java");
            cours.setFiliere("CSI");
            cours.setNiveauEtude("L3");
            cours.setSemestre("S6");
            cours.setVolumeHoraireTotal(new BigDecimal("20"));
            coursDAO.save(cours);

            salle = new Salle();
            salle.setNom("RPT-" + suffix);
            salle.setCapacite(40);
            salle.setBatiment("Principal");
            salleDAO.save(salle);

            assignation = new Assignation();
            assignation.setProfesseur(professeur);
            assignation.setCours(cours);
            assignation.setSalle(salle);
            assignation.setAnneeAcademique("2025-2026");
            assignation.setHeuresPrevues(new BigDecimal("20"));
            assignationDAO.save(assignation);

            seance = new SeancePlanifiee();
            seance.setAssignation(assignation);
            seance.setDateHeure(dateSeance);
            seance.setDureeMinutes(125);
            seance.setStatut(StatutSeance.REALISEE);
            seanceDAO.save(seance);

            pointage = new Pointage();
            pointage.setSeance(seance);
            pointage.setProfesseur(professeur);
            pointage.setHeurePointage(dateSeance.minusMinutes(5));
            pointage.setTypePointage(TypePointage.DEBUT);
            pointage.setStatut(StatutPointage.A_L_HEURE);
            pointageDAO.save(pointage);

            rapport = rapportService.genererRapportMensuel(
                    professeur.getId(),
                    dateSeance.getMonthValue(),
                    dateSeance.getYear()
            );

            if (!new BigDecimal("2.25").equals(rapport.getHeuresRealisees())) {
                throw new IllegalStateException("Heures attendues 2.25, obtenu : " + rapport.getHeuresRealisees());
            }

            if (!new BigDecimal("22500.00").equals(rapport.getMontantXOF())) {
                throw new IllegalStateException("Montant attendu 22500.00, obtenu : " + rapport.getMontantXOF());
            }

            RapportMensuel rapportValide = rapportService.validerRapport(rapport.getId());

            if (rapportValide.getStatut() != StatutRapport.VALIDE) {
                throw new IllegalStateException("Le rapport n'est pas passe en VALIDE.");
            }

            System.out.println("RapportService OK");
            System.out.println("Heures realisees : " + rapport.getHeuresRealisees());
            System.out.println("Montant XOF : " + rapport.getMontantXOF());
        } finally {
            if (rapport != null && rapport.getId() != null) {
                rapportDAO.delete(rapport.getId());
            }

            if (pointage != null && pointage.getId() != null) {
                pointageDAO.delete(pointage.getId());
            }

            if (seance != null && seance.getId() != null) {
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
