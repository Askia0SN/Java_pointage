package sn.epf.pointage.service;

import sn.epf.pointage.dao.AssignationDAO;
import sn.epf.pointage.dao.CoursDAO;
import sn.epf.pointage.dao.PeriodiciteCoursDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SalleDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.dao.UtilisateurDAO;
import sn.epf.pointage.model.Assignation;
import sn.epf.pointage.model.Cours;
import sn.epf.pointage.model.PeriodiciteCours;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.Salle;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.FrequenceCours;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.model.enums.TypeContrat;
import sn.epf.pointage.security.PasswordService;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.Locale;

public class EnrolementService {

    private final ProfesseurDAO professeurDAO;
    private final UtilisateurDAO utilisateurDAO;
    private final CoursDAO coursDAO;
    private final SalleDAO salleDAO;
    private final AssignationDAO assignationDAO;
    private final PeriodiciteCoursDAO periodiciteCoursDAO;
    private final SeanceDAO seanceDAO;
    private final PasswordService passwordService;

    public EnrolementService() {
        this(
                new ProfesseurDAO(),
                new UtilisateurDAO(),
                new CoursDAO(),
                new SalleDAO(),
                new AssignationDAO(),
                new PeriodiciteCoursDAO(),
                new SeanceDAO(),
                new PasswordService()
        );
    }

    public EnrolementService(
            ProfesseurDAO professeurDAO,
            UtilisateurDAO utilisateurDAO,
            CoursDAO coursDAO,
            SalleDAO salleDAO,
            AssignationDAO assignationDAO,
            PeriodiciteCoursDAO periodiciteCoursDAO,
            SeanceDAO seanceDAO,
            PasswordService passwordService
    ) {
        this.professeurDAO = professeurDAO;
        this.utilisateurDAO = utilisateurDAO;
        this.coursDAO = coursDAO;
        this.salleDAO = salleDAO;
        this.assignationDAO = assignationDAO;
        this.periodiciteCoursDAO = periodiciteCoursDAO;
        this.seanceDAO = seanceDAO;
        this.passwordService = passwordService;
    }

    public Professeur enrollerProfesseur(
            String nom,
            String prenom,
            String email,
            String telephone,
            TypeContrat typeContrat,
            BigDecimal tauxHoraireXOF,
            LocalDate dateEmbauche,
            String photo,
            String motDePasseInitial
    ) {
        validerProfesseur(nom, prenom, email, typeContrat, tauxHoraireXOF, motDePasseInitial);
        verifierEmailDisponible(email, null);

        Professeur professeur = new Professeur();
        professeur.setNom(nom.trim());
        professeur.setPrenom(prenom.trim());
        professeur.setEmail(email.trim().toLowerCase(Locale.ROOT));
        professeur.setTelephone(telephone);
        professeur.setTypeContrat(typeContrat);
        professeur.setTauxHoraireXOF(tauxHoraireXOF);
        professeur.setDateEmbauche(dateEmbauche != null ? dateEmbauche : LocalDate.now());
        professeur.setPhoto(photo);
        professeur.setActif(true);
        professeur.setMatricule(genererMatricule(nom, prenom));

        professeurDAO.save(professeur);

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(professeur.getMatricule());
        utilisateur.setMotDePasseHash(passwordService.hash(motDePasseInitial));
        utilisateur.setRole(Role.PROFESSEUR);
        utilisateur.setActif(true);
        utilisateur.setProfesseurLie(professeur);

        utilisateurDAO.save(utilisateur);

        return professeur;
    }

    public Professeur mettreAJourProfil(
            Long professeurId,
            String nom,
            String prenom,
            String email,
            String telephone,
            BigDecimal tauxHoraireXOF,
            String photo
    ) {
        Professeur professeur = professeurDAO.findById(professeurId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable."));

        verifierEmailDisponible(email, professeurId);

        professeur.setNom(nom.trim());
        professeur.setPrenom(prenom.trim());
        professeur.setEmail(email.trim().toLowerCase(Locale.ROOT));
        professeur.setTelephone(telephone);
        professeur.setTauxHoraireXOF(tauxHoraireXOF);
        professeur.setPhoto(photo);

        return professeurDAO.update(professeur);
    }

    public void desactiverProfesseur(Long professeurId) {
        Professeur professeur = professeurDAO.findById(professeurId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable."));

        professeur.setActif(false);
        professeurDAO.update(professeur);

        utilisateurDAO.findByProfesseurId(professeurId).ifPresent(utilisateur -> {
            utilisateur.setActif(false);
            utilisateurDAO.update(utilisateur);
        });

        List<SeancePlanifiee> seancesFutures = seanceDAO.findFuturePlanifieesByProfesseur(
                professeurId,
                LocalDateTime.now()
        );

        for (SeancePlanifiee seance : seancesFutures) {
            seance.setStatut(StatutSeance.ANNULEE);
            seanceDAO.update(seance);
        }
    }

    public Assignation assignerCours(
            Long professeurId,
            Long coursId,
            Long salleId,
            String anneeAcademique,
            BigDecimal heuresPrevues,
            DayOfWeek jourSemaine,
            LocalTime heureDebut,
            LocalTime heureFin,
            FrequenceCours frequence,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit etre apres la date de debut.");
        }

        if (!heureFin.isAfter(heureDebut)) {
            throw new IllegalArgumentException("L'heure de fin doit etre apres l'heure de debut.");
        }

        Professeur professeur = professeurDAO.findById(professeurId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable."));
        Cours cours = coursDAO.findById(coursId)
                .orElseThrow(() -> new IllegalArgumentException("Cours introuvable."));
        Salle salle = salleDAO.findById(salleId)
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable."));

        Assignation assignation = new Assignation();
        assignation.setProfesseur(professeur);
        assignation.setCours(cours);
        assignation.setSalle(salle);
        assignation.setAnneeAcademique(anneeAcademique);
        assignation.setHeuresPrevues(heuresPrevues);
        assignationDAO.save(assignation);

        PeriodiciteCours periodicite = new PeriodiciteCours();
        periodicite.setAssignation(assignation);
        periodicite.setJourSemaine(jourSemaine);
        periodicite.setHeureDebut(heureDebut);
        periodicite.setHeureFin(heureFin);
        periodicite.setFrequence(frequence);
        periodicite.setDateDebut(dateDebut);
        periodicite.setDateFin(dateFin);
        periodiciteCoursDAO.save(periodicite);

        genererSeances(assignation, jourSemaine, heureDebut, heureFin, frequence, dateDebut, dateFin);

        return assignation;
    }

    private void genererSeances(
            Assignation assignation,
            DayOfWeek jourSemaine,
            LocalTime heureDebut,
            LocalTime heureFin,
            FrequenceCours frequence,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {
        LocalDate dateCourante = dateDebut;

        while (dateCourante.getDayOfWeek() != jourSemaine) {
            dateCourante = dateCourante.plusDays(1);
        }

        long incrementJours = frequence == FrequenceCours.BIMENSUELLE ? 14 : 7;
        int dureeMinutes = (int) Duration.between(heureDebut, heureFin).toMinutes();

        while (!dateCourante.isAfter(dateFin)) {
            SeancePlanifiee seance = new SeancePlanifiee();
            seance.setAssignation(assignation);
            seance.setDateHeure(LocalDateTime.of(dateCourante, heureDebut));
            seance.setDureeMinutes(dureeMinutes);
            seance.setStatut(StatutSeance.PLANIFIEE);
            seanceDAO.save(seance);

            dateCourante = dateCourante.plusDays(incrementJours);
        }
    }

    private String genererMatricule(String nom, String prenom) {
        int annee = Year.now().getValue();
        String initiales = normaliserInitiale(prenom) + normaliserInitiale(nom);
        String prefix = "EPF-" + annee + "-" + initiales + "-";
        long prochainNumero = professeurDAO.countByMatriculePrefix(prefix) + 1;

        return prefix + String.format("%03d", prochainNumero);
    }

    private String normaliserInitiale(String valeur) {
        String normalisee = Normalizer.normalize(valeur.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);

        return normalisee.substring(0, 1);
    }

    private void validerProfesseur(
            String nom,
            String prenom,
            String email,
            TypeContrat typeContrat,
            BigDecimal tauxHoraireXOF,
            String motDePasseInitial
    ) {
        if (isBlank(nom) || isBlank(prenom) || isBlank(email) || typeContrat == null) {
            throw new IllegalArgumentException("Nom, prenom, email et type de contrat sont obligatoires.");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("L'adresse email est invalide.");
        }

        if (tauxHoraireXOF == null || tauxHoraireXOF.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le taux horaire doit etre positif.");
        }

        if (isBlank(motDePasseInitial)) {
            throw new IllegalArgumentException("Le mot de passe initial est obligatoire.");
        }
    }

    private void verifierEmailDisponible(String email, Long professeurIdAutorise) {
        professeurDAO.findByEmail(email).ifPresent(professeurExistant -> {
            if (!professeurExistant.getId().equals(professeurIdAutorise)) {
                throw new IllegalArgumentException("Cette adresse email est deja utilisee.");
            }
        });
    }

    private boolean isBlank(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
