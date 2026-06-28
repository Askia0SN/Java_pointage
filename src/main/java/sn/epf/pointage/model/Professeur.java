package sn.epf.pointage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import sn.epf.pointage.model.enums.TypeContrat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "professeurs")
public class Professeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String matricule;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeContrat typeContrat;

    @Column(precision = 12, scale = 2)
    private BigDecimal tauxHoraireXOF;

    private LocalDate dateEmbauche;

    private String photo;

    @Column(nullable = false)
    private boolean actif = true;

    @OneToMany(mappedBy = "professeur", fetch = FetchType.LAZY)
    private List<Assignation> assignations = new ArrayList<>();

    @OneToMany(mappedBy = "professeur", fetch = FetchType.LAZY)
    private List<Pointage> pointages = new ArrayList<>();

    @OneToMany(mappedBy = "professeur", fetch = FetchType.LAZY)
    private List<RapportMensuel> rapports = new ArrayList<>();

    @OneToMany(mappedBy = "professeur", fetch = FetchType.LAZY)
    private List<Alerte> alertes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public TypeContrat getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(TypeContrat typeContrat) {
        this.typeContrat = typeContrat;
    }

    public BigDecimal getTauxHoraireXOF() {
        return tauxHoraireXOF;
    }

    public void setTauxHoraireXOF(BigDecimal tauxHoraireXOF) {
        this.tauxHoraireXOF = tauxHoraireXOF;
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public List<Assignation> getAssignations() {
        return assignations;
    }

    public void setAssignations(List<Assignation> assignations) {
        this.assignations = assignations;
    }

    public List<Pointage> getPointages() {
        return pointages;
    }

    public void setPointages(List<Pointage> pointages) {
        this.pointages = pointages;
    }

    public List<RapportMensuel> getRapports() {
        return rapports;
    }

    public void setRapports(List<RapportMensuel> rapports) {
        this.rapports = rapports;
    }

    public List<Alerte> getAlertes() {
        return alertes;
    }

    public void setAlertes(List<Alerte> alertes) {
        this.alertes = alertes;
    }
}
