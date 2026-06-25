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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import sn.epf.pointage.model.enums.StatutRapport;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "rapports_mensuels",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_rapport_professeur_mois_annee",
                columnNames = {"professeur_id", "mois", "annee"}
        )
)
public class RapportMensuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;

    @Column(nullable = false)
    private int mois;

    @Column(nullable = false)
    private int annee;

    @Column(precision = 8, scale = 2)
    private BigDecimal heuresRealisees;

    @Column(precision = 12, scale = 2)
    private BigDecimal montantXOF;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutRapport statut = StatutRapport.EN_ATTENTE;

    @Column(nullable = false)
    private LocalDateTime dateGeneration;
}
