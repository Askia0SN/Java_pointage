package sn.epf.pointage.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.RapportDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.security.SessionContext;
import sn.epf.pointage.service.RapportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class RapportsController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final RapportDAO rapportDAO = new RapportDAO();
    private final RapportService rapportService = new RapportService();

    @FXML
    private ComboBox<Professeur> professeurCombo;

    @FXML
    private ComboBox<Integer> moisCombo;

    @FXML
    private TextField anneeField;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<RapportMensuel> rapportsTable;

    @FXML
    private TableColumn<RapportMensuel, String> professeurColumn;

    @FXML
    private TableColumn<RapportMensuel, String> periodeColumn;

    @FXML
    private TableColumn<RapportMensuel, BigDecimal> heuresColumn;

    @FXML
    private TableColumn<RapportMensuel, BigDecimal> montantColumn;

    @FXML
    private TableColumn<RapportMensuel, String> statutColumn;

    @FXML
    private TableColumn<RapportMensuel, String> dateGenerationColumn;

    @FXML
    private void initialize() {
        professeurCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Professeur professeur) {
                if (professeur == null) {
                    return "";
                }

                return professeur.getMatricule() + " - " + professeur.getPrenom() + " " + professeur.getNom();
            }

            @Override
            public Professeur fromString(String value) {
                return null;
            }
        });

        moisCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer mois) {
                if (mois == null) {
                    return "";
                }

                return LocalDate.of(2026, mois, 1)
                        .getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.FRENCH);
            }

            @Override
            public Integer fromString(String value) {
                return null;
            }
        });

        professeurColumn.setCellValueFactory(cellData -> {
            Professeur professeur = cellData.getValue().getProfesseur();
            return new SimpleStringProperty(
                    professeur.getMatricule() + " - " + professeur.getPrenom() + " " + professeur.getNom()
            );
        });
        periodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("%02d/%d", cellData.getValue().getMois(), cellData.getValue().getAnnee())
        ));
        heuresColumn.setCellValueFactory(new PropertyValueFactory<>("heuresRealisees"));
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montantXOF"));
        statutColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut().name()));
        dateGenerationColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateGeneration = cellData.getValue().getDateGeneration();
            return new SimpleStringProperty(dateGeneration == null ? "" : dateGeneration.format(DATE_TIME_FORMATTER));
        });

        moisCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        moisCombo.setValue(LocalDate.now().getMonthValue());
        anneeField.setText(String.valueOf(LocalDate.now().getYear()));

        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        professeurCombo.setItems(FXCollections.observableArrayList(professeurDAO.findActifs()));
        rapportsTable.setItems(FXCollections.observableArrayList(rapportDAO.findAllWithProfesseur()));
        messageLabel.setText("Rapports actualises.");
    }

    @FXML
    private void handleGenerate() {
        verifierDroitRapports();

        try {
            Professeur professeur = professeurCombo.getValue();

            if (professeur == null) {
                throw new IllegalArgumentException("Selectionne un professeur.");
            }

            RapportMensuel rapport = rapportService.genererRapportMensuel(
                    professeur.getId(),
                    moisCombo.getValue(),
                    Integer.parseInt(anneeField.getText())
            );

            handleRefresh();
            rapportsTable.getSelectionModel().select(rapport);
            messageLabel.setText("Rapport genere : " + rapport.getHeuresRealisees() + " h / " + rapport.getMontantXOF() + " XOF.");
        } catch (RuntimeException exception) {
            afficherErreur(exception.getMessage());
        }
    }

    @FXML
    private void handleValidate() {
        verifierDroitRapports();
        RapportMensuel rapport = getRapportSelectionne();

        if (rapport == null) {
            afficherErreur("Selectionne un rapport.");
            return;
        }

        try {
            rapportService.validerRapport(rapport.getId());
            handleRefresh();
            messageLabel.setText("Rapport valide.");
        } catch (RuntimeException exception) {
            afficherErreur(exception.getMessage());
        }
    }

    @FXML
    private void handleMarkPaid() {
        verifierDroitRapports();
        RapportMensuel rapport = getRapportSelectionne();

        if (rapport == null) {
            afficherErreur("Selectionne un rapport.");
            return;
        }

        try {
            rapportService.marquerCommePaye(rapport.getId());
            handleRefresh();
            messageLabel.setText("Rapport marque PAYE.");
        } catch (RuntimeException exception) {
            afficherErreur(exception.getMessage());
        }
    }

    private RapportMensuel getRapportSelectionne() {
        return rapportsTable.getSelectionModel().getSelectedItem();
    }

    private void verifierDroitRapports() {
        SessionContext.getInstance().verifierRole(Role.ADMIN, Role.SCOLARITE);
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Operation impossible");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
