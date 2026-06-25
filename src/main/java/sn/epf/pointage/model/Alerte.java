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
import sn.epf.pointage.model.enums.TypeAlerte;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertes")
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeAlerte type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private boolean lue = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seance_id")
    private SeancePlanifiee seance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;
}
