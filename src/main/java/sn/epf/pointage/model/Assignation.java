package sn.epf.pointage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignations")
public class Assignation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @Column(nullable = false, length = 20)
    private String anneeAcademique;

    @Column(precision = 8, scale = 2)
    private BigDecimal heuresPrevues;

    @OneToMany(mappedBy = "assignation", fetch = FetchType.LAZY)
    private List<PeriodiciteCours> periodicites = new ArrayList<>();

    @OneToMany(mappedBy = "assignation", fetch = FetchType.LAZY)
    private List<SeancePlanifiee> seances = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Professeur getProfesseur() {
        return professeur;
    }

    public void setProfesseur(Professeur professeur) {
        this.professeur = professeur;
    }

    public Cours getCours() {
        return cours;
    }

    public void setCours(Cours cours) {
        this.cours = cours;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    public String getAnneeAcademique() {
        return anneeAcademique;
    }

    public void setAnneeAcademique(String anneeAcademique) {
        this.anneeAcademique = anneeAcademique;
    }

    public BigDecimal getHeuresPrevues() {
        return heuresPrevues;
    }

    public void setHeuresPrevues(BigDecimal heuresPrevues) {
        this.heuresPrevues = heuresPrevues;
    }

    public List<PeriodiciteCours> getPeriodicites() {
        return periodicites;
    }

    public void setPeriodicites(List<PeriodiciteCours> periodicites) {
        this.periodicites = periodicites;
    }

    public List<SeancePlanifiee> getSeances() {
        return seances;
    }

    public void setSeances(List<SeancePlanifiee> seances) {
        this.seances = seances;
    }
}
