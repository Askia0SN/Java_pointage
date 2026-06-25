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
import sn.epf.pointage.model.enums.StatutPointage;
import sn.epf.pointage.model.enums.TypePointage;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "pointages",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pointage_seance_professeur_type",
                columnNames = {"seance_id", "professeur_id", "typePointage"}
        )
)
public class Pointage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seance_id", nullable = false)
    private SeancePlanifiee seance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;

    @Column(nullable = false)
    private LocalDateTime heurePointage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypePointage typePointage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutPointage statut;

    private String observations;
}
