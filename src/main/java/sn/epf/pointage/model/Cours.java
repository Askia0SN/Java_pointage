package sn.epf.pointage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cours")
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String intitule;

    @Column(precision = 8, scale = 2)
    private BigDecimal volumeHoraireTotal;

    @Column(length = 80)
    private String niveauEtude;

    @Column(length = 100)
    private String filiere;

    @Column(length = 30)
    private String semestre;

    @OneToMany(mappedBy = "cours", fetch = FetchType.LAZY)
    private List<Assignation> assignations = new ArrayList<>();
}
