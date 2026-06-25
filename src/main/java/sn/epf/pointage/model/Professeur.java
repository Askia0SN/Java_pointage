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
}
