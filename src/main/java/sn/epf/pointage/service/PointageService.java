package sn.epf.pointage.service;

import sn.epf.pointage.dao.AlerteDAO;
import sn.epf.pointage.dao.PointageDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Alerte;
import sn.epf.pointage.model.Pointage;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.ResultatPointage;
import sn.epf.pointage.model.enums.StatutPointage;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.model.enums.TypeAlerte;
import sn.epf.pointage.model.enums.TypePointage;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class PointageService {

    private static final long MINUTES_AVANT_AUTORISEES = 15;
    private static final long MINUTES_RETARD_AUTORISEES = 5;

    private final SeanceDAO seanceDAO;
    private final ProfesseurDAO professeurDAO;
    private final PointageDAO pointageDAO;
    private final AlerteDAO alerteDAO;
    private final Clock clock;

    public PointageService() {
        this(
                new SeanceDAO(),
                new ProfesseurDAO(),
                new PointageDAO(),
                new AlerteDAO(),
                Clock.systemDefaultZone()
        );
    }

    public PointageService(
            SeanceDAO seanceDAO,
            ProfesseurDAO professeurDAO,
            PointageDAO pointageDAO,
            AlerteDAO alerteDAO,
            Clock clock
    ) {
        this.seanceDAO = seanceDAO;
        this.professeurDAO = professeurDAO;
        this.pointageDAO = pointageDAO;
        this.alerteDAO = alerteDAO;
        this.clock = clock;
    }

    public ResultatPointage pointer(Long seanceId, Long professeurId, TypePointage typePointage) {
        if (seanceId == null || professeurId == null || typePointage == null) {
            throw new IllegalArgumentException("La seance, le professeur et le type de pointage sont obligatoires.");
        }

        SeancePlanifiee seance = seanceDAO.findById(seanceId).orElse(null);
        Professeur professeur = professeurDAO.findById(professeurId).orElse(null);

        if (seance == null || professeur == null) {
            return ResultatPointage.SEANCE_INTROUVABLE;
        }

        if (!professeur.isActif()) {
            return ResultatPointage.PROF_INACTIF;
        }

        if (pointageDAO.findBySeanceProfesseurAndType(seanceId, professeurId, typePointage).isPresent()) {
            return ResultatPointage.DEJA_POINTE;
        }

        LocalDateTime maintenant = LocalDateTime.now(clock);
        long ecartMinutes = ChronoUnit.MINUTES.between(seance.getDateHeure(), maintenant);

        if (typePointage == TypePointage.DEBUT && ecartMinutes < -MINUTES_AVANT_AUTORISEES) {
            return ResultatPointage.TROP_TOT;
        }

        boolean enRetard = typePointage == TypePointage.DEBUT && ecartMinutes > MINUTES_RETARD_AUTORISEES;

        Pointage pointage = new Pointage();
        pointage.setSeance(seance);
        pointage.setProfesseur(professeur);
        pointage.setHeurePointage(maintenant);
        pointage.setTypePointage(typePointage);
        pointage.setStatut(enRetard ? StatutPointage.EN_RETARD : StatutPointage.A_L_HEURE);
        pointageDAO.save(pointage);

        if (typePointage == TypePointage.DEBUT) {
            seance.setStatut(StatutSeance.REALISEE);
            seanceDAO.update(seance);
        }

        if (enRetard) {
            creerAlerteRetard(seance, professeur, maintenant, ecartMinutes);
            return ResultatPointage.EN_RETARD;
        }

        return ResultatPointage.SUCCES;
    }

    private void creerAlerteRetard(
            SeancePlanifiee seance,
            Professeur professeur,
            LocalDateTime dateCreation,
            long ecartMinutes
    ) {
        Alerte alerte = new Alerte();
        alerte.setType(TypeAlerte.RETARD);
        alerte.setProfesseur(professeur);
        alerte.setSeance(seance);
        alerte.setDateCreation(dateCreation);
        alerte.setLue(false);
        alerte.setMessage(
                "Retard de " + ecartMinutes + " minutes pour le professeur "
                        + professeur.getPrenom() + " " + professeur.getNom()
        );

        alerteDAO.save(alerte);
    }
}
