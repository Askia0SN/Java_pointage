package sn.epf.pointage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import sn.epf.pointage.model.enums.StatutSeance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seances_planifiees")
public class SeancePlanifiee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignation_id", nullable = false)
    private Assignation assignation;

    @Column(nullable = false)
    private LocalDateTime dateHeure;

    @Column(nullable = false)
    private int dureeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutSeance statut = StatutSeance.PLANIFIEE;

    @OneToMany(mappedBy = "seance", fetch = FetchType.LAZY)
    private List<Pointage> pointages = new ArrayList<>();

    @OneToMany(mappedBy = "seance", fetch = FetchType.LAZY)
    private List<Alerte> alertes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Assignation getAssignation() {
        return assignation;
    }

    public void setAssignation(Assignation assignation) {
        this.assignation = assignation;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public int getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public StatutSeance getStatut() {
        return statut;
    }

    public void setStatut(StatutSeance statut) {
        this.statut = statut;
    }

    public List<Pointage> getPointages() {
        return pointages;
    }

    public void setPointages(List<Pointage> pointages) {
        this.pointages = pointages;
    }

    public List<Alerte> getAlertes() {
        return alertes;
    }

    public void setAlertes(List<Alerte> alertes) {
        this.alertes = alertes;
    }
}
