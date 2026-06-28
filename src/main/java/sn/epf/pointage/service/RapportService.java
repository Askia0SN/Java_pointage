package sn.epf.pointage.service;

import sn.epf.pointage.dao.PointageDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.RapportDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.StatutRapport;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.model.enums.TypePointage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class RapportService {

    private static final int MINUTES_PAR_QUART_HEURE = 15;
    private static final BigDecimal MINUTES_PAR_HEURE = new BigDecimal("60");

    private final ProfesseurDAO professeurDAO;
    private final SeanceDAO seanceDAO;
    private final PointageDAO pointageDAO;
    private final RapportDAO rapportDAO;

    public RapportService() {
        this(new ProfesseurDAO(), new SeanceDAO(), new PointageDAO(), new RapportDAO());
    }

    public RapportService(
            ProfesseurDAO professeurDAO,
            SeanceDAO seanceDAO,
            PointageDAO pointageDAO,
            RapportDAO rapportDAO
    ) {
        this.professeurDAO = professeurDAO;
        this.seanceDAO = seanceDAO;
        this.pointageDAO = pointageDAO;
        this.rapportDAO = rapportDAO;
    }

    public RapportMensuel genererRapportMensuel(Long professeurId, int mois, int annee) {
        validerPeriode(mois, annee);

        var professeur = professeurDAO.findById(professeurId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable."));

        List<SeancePlanifiee> seancesPlanifiees = seanceDAO.findByProfesseurStatutAndMois(
                professeurId,
                StatutSeance.PLANIFIEE,
                mois,
                annee
        );

        if (!seancesPlanifiees.isEmpty()) {
            throw new IllegalStateException("Generation bloquee : des seances du mois restent PLANIFIEE.");
        }

        List<SeancePlanifiee> seancesRealisees = seanceDAO.findByProfesseurStatutAndMois(
                professeurId,
                StatutSeance.REALISEE,
                mois,
                annee
        );

        int totalMinutes = seancesRealisees.stream()
                .filter(seance -> pointageDAO.findBySeanceProfesseurAndType(
                        seance.getId(),
                        professeurId,
                        TypePointage.DEBUT
                ).isPresent())
                .mapToInt(SeancePlanifiee::getDureeMinutes)
                .sum();

        BigDecimal heuresRealisees = convertirMinutesEnHeuresArrondies(totalMinutes);
        BigDecimal montantXOF = heuresRealisees.multiply(professeur.getTauxHoraireXOF())
                .setScale(2, RoundingMode.HALF_UP);

        RapportMensuel rapport = rapportDAO.findByProfesseurAndPeriode(professeurId, mois, annee)
                .orElseGet(RapportMensuel::new);

        rapport.setProfesseur(professeur);
        rapport.setMois(mois);
        rapport.setAnnee(annee);
        rapport.setHeuresRealisees(heuresRealisees);
        rapport.setMontantXOF(montantXOF);
        rapport.setStatut(StatutRapport.EN_ATTENTE);
        rapport.setDateGeneration(LocalDateTime.now());

        if (rapport.getId() == null) {
            return rapportDAO.save(rapport);
        }

        return rapportDAO.update(rapport);
    }

    public RapportMensuel validerRapport(Long rapportId) {
        RapportMensuel rapport = rapportDAO.findById(rapportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable."));

        rapport.setStatut(StatutRapport.VALIDE);
        return rapportDAO.update(rapport);
    }

    public RapportMensuel marquerCommePaye(Long rapportId) {
        RapportMensuel rapport = rapportDAO.findById(rapportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable."));

        if (rapport.getStatut() != StatutRapport.VALIDE) {
            throw new IllegalStateException("Un rapport doit etre VALIDE avant d'etre marque PAYE.");
        }

        rapport.setStatut(StatutRapport.PAYE);
        return rapportDAO.update(rapport);
    }

    private BigDecimal convertirMinutesEnHeuresArrondies(int totalMinutes) {
        if (totalMinutes <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        int minutesArrondies = ((totalMinutes + MINUTES_PAR_QUART_HEURE - 1) / MINUTES_PAR_QUART_HEURE)
                * MINUTES_PAR_QUART_HEURE;

        return new BigDecimal(minutesArrondies)
                .divide(MINUTES_PAR_HEURE, 2, RoundingMode.HALF_UP);
    }

    private void validerPeriode(int mois, int annee) {
        if (mois < 1 || mois > 12) {
            throw new IllegalArgumentException("Le mois doit etre compris entre 1 et 12.");
        }

        if (annee < 2000) {
            throw new IllegalArgumentException("L'annee est invalide.");
        }
    }
}
