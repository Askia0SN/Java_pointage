package sn.epf.pointage.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.ResultatPointage;
import sn.epf.pointage.model.enums.TypePointage;
import sn.epf.pointage.security.SessionContext;
import sn.epf.pointage.service.PointageService;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PointageController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final PointageService pointageService = new PointageService();

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<SeancePlanifiee> seancesTable;

    @FXML
    private TableColumn<SeancePlanifiee, String> coursColumn;

    @FXML
    private TableColumn<SeancePlanifiee, String> salleColumn;

    @FXML
    private TableColumn<SeancePlanifiee, String> dateColumn;

    @FXML
    private TableColumn<SeancePlanifiee, Number> dureeColumn;

    @FXML
    private TableColumn<SeancePlanifiee, String> statutColumn;

    @FXML
    private Button pointerDebutButton;

    @FXML
    private Button pointerFinButton;

    @FXML
    private void initialize() {
        coursColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getAssignation().getCours().getIntitule()
        ));
        salleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getAssignation().getSalle().getNom()
        ));
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDateHeure().format(DATE_TIME_FORMATTER)
        ));
        dureeColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                cellData.getValue().getDureeMinutes()
        ));
        statutColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatut().name()
        ));
        pointerDebutButton.disableProperty().bind(seancesTable.getSelectionModel().selectedItemProperty().isNull());
        pointerFinButton.disableProperty().bind(seancesTable.getSelectionModel().selectedItemProperty().isNull());

        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        Optional<Professeur> professeurConnecte = SessionContext.getInstance().getProfesseurLie();

        if (professeurConnecte.isEmpty()) {
            seancesTable.setItems(FXCollections.emptyObservableList());
            subtitleLabel.setText("Aucun profil professeur n'est lie au compte connecte.");
            messageLabel.setText("Connecte-toi avec un compte professeur pour pointer.");
            return;
        }

        Professeur professeur = professeurConnecte.get();
        subtitleLabel.setText("Seances du jour de " + professeur.getPrenom() + " " + professeur.getNom());
        seancesTable.setItems(FXCollections.observableArrayList(
                seanceDAO.findSeancesDuJourByProfesseur(professeur.getId())
        ));
        if (seancesTable.getItems().isEmpty()) {
            messageLabel.setText("Aucune seance a pointer aujourd'hui.");
        } else {
            messageLabel.setText("Selectionne une seance dans la table, puis clique sur le type de pointage.");
        }
    }

    @FXML
    private void handlePointerDebut() {
        pointer(TypePointage.DEBUT);
    }

    @FXML
    private void handlePointerFin() {
        pointer(TypePointage.FIN);
    }

    private void pointer(TypePointage typePointage) {
        Optional<Professeur> professeurConnecte = SessionContext.getInstance().getProfesseurLie();
        SeancePlanifiee seance = seancesTable.getSelectionModel().getSelectedItem();

        if (professeurConnecte.isEmpty()) {
            messageLabel.setText("Aucun professeur connecte.");
            return;
        }

        if (seance == null) {
            messageLabel.setText("Selectionne une seance.");
            return;
        }

        ResultatPointage resultat = pointageService.pointer(
                seance.getId(),
                professeurConnecte.get().getId(),
                typePointage
        );

        messageLabel.setText(messagePour(resultat));
        handleRefresh();
    }

    private String messagePour(ResultatPointage resultat) {
        return switch (resultat) {
            case SUCCES -> "Pointage enregistre avec succes.";
            case EN_RETARD -> "Pointage en retard enregistre, une alerte a ete creee.";
            case TROP_TOT -> "Pointage refuse : la seance est encore trop loin.";
            case PROF_INACTIF -> "Pointage refuse : professeur inactif.";
            case DEJA_POINTE -> "Pointage refuse : ce pointage existe deja.";
            case SEANCE_INTROUVABLE -> "Pointage refuse : seance ou professeur introuvable.";
        };
    }
}
