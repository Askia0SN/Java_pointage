package sn.epf.pointage.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import sn.epf.pointage.dao.CoursDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SalleDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Cours;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.Salle;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.FrequenceCours;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.security.SessionContext;
import sn.epf.pointage.service.EnrolementService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class PlanningController {

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final CoursDAO coursDAO = new CoursDAO();
    private final SalleDAO salleDAO = new SalleDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final EnrolementService enrolementService = new EnrolementService();

    @FXML
    private TableView<Cours> coursTable;

    @FXML
    private TableColumn<Cours, String> coursCodeColumn;

    @FXML
    private TableColumn<Cours, String> coursIntituleColumn;

    @FXML
    private TableColumn<Cours, String> coursFiliereColumn;

    @FXML
    private TableColumn<Cours, BigDecimal> coursVolumeColumn;

    @FXML
    private TableView<Salle> sallesTable;

    @FXML
    private TableColumn<Salle, String> salleNomColumn;

    @FXML
    private TableColumn<Salle, Integer> salleCapaciteColumn;

    @FXML
    private TableColumn<Salle, String> salleBatimentColumn;

    @FXML
    private TableColumn<Salle, String> salleEquipementsColumn;

    @FXML
    private ComboBox<Professeur> professeurCombo;

    @FXML
    private ComboBox<Cours> coursCombo;

    @FXML
    private ComboBox<Salle> salleCombo;

    @FXML
    private TextField anneeField;

    @FXML
    private TextField heuresPrevuesField;

    @FXML
    private ComboBox<DayOfWeek> jourCombo;

    @FXML
    private TextField heureDebutField;

    @FXML
    private TextField heureFinField;

    @FXML
    private ComboBox<FrequenceCours> frequenceCombo;

    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<SeancePlanifiee> seancesTable;

    @FXML
    private TableColumn<SeancePlanifiee, LocalDateTime> seanceDateColumn;

    @FXML
    private TableColumn<SeancePlanifiee, Integer> seanceDureeColumn;

    @FXML
    private TableColumn<SeancePlanifiee, StatutSeance> seanceStatutColumn;

    @FXML
    private void initialize() {
        coursCodeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        coursIntituleColumn.setCellValueFactory(new PropertyValueFactory<>("intitule"));
        coursFiliereColumn.setCellValueFactory(new PropertyValueFactory<>("filiere"));
        coursVolumeColumn.setCellValueFactory(new PropertyValueFactory<>("volumeHoraireTotal"));

        salleNomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        salleCapaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        salleBatimentColumn.setCellValueFactory(new PropertyValueFactory<>("batiment"));
        salleEquipementsColumn.setCellValueFactory(new PropertyValueFactory<>("equipements"));

        seanceDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        seanceDureeColumn.setCellValueFactory(new PropertyValueFactory<>("dureeMinutes"));
        seanceStatutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        jourCombo.setItems(FXCollections.observableArrayList(DayOfWeek.values()));
        jourCombo.setValue(DayOfWeek.MONDAY);
        frequenceCombo.setItems(FXCollections.observableArrayList(FrequenceCours.values()));
        frequenceCombo.setValue(FrequenceCours.HEBDOMADAIRE);
        dateDebutPicker.setValue(LocalDate.now());
        dateFinPicker.setValue(LocalDate.now().plusMonths(3));

        configurerConvertisseurs();
        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        coursTable.setItems(FXCollections.observableArrayList(coursDAO.findAll()));
        sallesTable.setItems(FXCollections.observableArrayList(salleDAO.findAll()));
        professeurCombo.setItems(FXCollections.observableArrayList(professeurDAO.findActifs()));
        coursCombo.setItems(FXCollections.observableArrayList(coursDAO.findAll()));
        salleCombo.setItems(FXCollections.observableArrayList(salleDAO.findAll()));
        seancesTable.setItems(FXCollections.observableArrayList(seanceDAO.findAll()));
        messageLabel.setText("Planning actualise.");
    }

    @FXML
    private void handleAddCours() {
        verifierDroitPlanning();

        CoursFormData data = afficherFormulaireCours();

        if (data == null) {
            return;
        }

        try {
            if (coursDAO.findByCode(data.code()).isPresent()) {
                throw new IllegalArgumentException("Ce code de cours existe deja.");
            }

            Cours cours = new Cours();
            cours.setCode(data.code());
            cours.setIntitule(data.intitule());
            cours.setVolumeHoraireTotal(data.volumeHoraireTotal());
            cours.setNiveauEtude(data.niveauEtude());
            cours.setFiliere(data.filiere());
            cours.setSemestre(data.semestre());
            coursDAO.save(cours);

            handleRefresh();
            messageLabel.setText("Cours ajoute.");
        } catch (RuntimeException exception) {
            afficherErreur(exception.getMessage());
        }
    }

    @FXML
    private void handleAddSalle() {
        verifierDroitPlanning();

        SalleFormData data = afficherFormulaireSalle();

        if (data == null) {
            return;
        }

        try {
            if (salleDAO.findByNom(data.nom()).isPresent()) {
                throw new IllegalArgumentException("Cette salle existe deja.");
            }

            Salle salle = new Salle();
            salle.setNom(data.nom());
            salle.setCapacite(data.capacite());
            salle.setBatiment(data.batiment());
            salle.setEquipements(data.equipements());
            salleDAO.save(salle);

            handleRefresh();
            messageLabel.setText("Salle ajoutee.");
        } catch (RuntimeException exception) {
            afficherErreur(exception.getMessage());
        }
    }

    @FXML
    private void handleCreateAssignation() {
        verifierDroitPlanning();

        try {
            Professeur professeur = professeurCombo.getValue();
            Cours cours = coursCombo.getValue();
            Salle salle = salleCombo.getValue();

            if (professeur == null || cours == null || salle == null) {
                throw new IllegalArgumentException("Professeur, cours et salle sont obligatoires.");
            }

            enrolementService.assignerCours(
                    professeur.getId(),
                    cours.getId(),
                    salle.getId(),
                    anneeField.getText(),
                    new BigDecimal(heuresPrevuesField.getText()),
                    jourCombo.getValue(),
                    LocalTime.parse(heureDebutField.getText()),
                    LocalTime.parse(heureFinField.getText()),
                    frequenceCombo.getValue(),
                    dateDebutPicker.getValue(),
                    dateFinPicker.getValue()
            );

            handleRefresh();
            messageLabel.setText("Assignation creee et seances generees.");
        } catch (RuntimeException exception) {
            afficherErreur(exception.getMessage());
        }
    }

    private void configurerConvertisseurs() {
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

        coursCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cours cours) {
                return cours == null ? "" : cours.getCode() + " - " + cours.getIntitule();
            }

            @Override
            public Cours fromString(String value) {
                return null;
            }
        });

        salleCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Salle salle) {
                return salle == null ? "" : salle.getNom();
            }

            @Override
            public Salle fromString(String value) {
                return null;
            }
        });
    }

    private CoursFormData afficherFormulaireCours() {
        Dialog<CoursFormData> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un cours");
        dialog.setHeaderText("Creation d'un nouveau cours");

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField codeField = new TextField();
        TextField intituleField = new TextField();
        TextField volumeField = new TextField("30");
        TextField niveauField = new TextField("L3");
        TextField filiereField = new TextField("CSI");
        TextField semestreField = new TextField("S6");

        GridPane grid = creerGrid();
        grid.addRow(0, new Label("Code"), codeField);
        grid.addRow(1, new Label("Intitule"), intituleField);
        grid.addRow(2, new Label("Volume horaire"), volumeField);
        grid.addRow(3, new Label("Niveau"), niveauField);
        grid.addRow(4, new Label("Filiere"), filiereField);
        grid.addRow(5, new Label("Semestre"), semestreField);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButton) {
                return null;
            }

            return new CoursFormData(
                    codeField.getText(),
                    intituleField.getText(),
                    new BigDecimal(volumeField.getText()),
                    niveauField.getText(),
                    filiereField.getText(),
                    semestreField.getText()
            );
        });

        return dialog.showAndWait().orElse(null);
    }

    private SalleFormData afficherFormulaireSalle() {
        Dialog<SalleFormData> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une salle");
        dialog.setHeaderText("Creation d'une nouvelle salle");

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField nomField = new TextField();
        TextField capaciteField = new TextField("30");
        TextField batimentField = new TextField("Principal");
        TextField equipementsField = new TextField();

        GridPane grid = creerGrid();
        grid.addRow(0, new Label("Nom"), nomField);
        grid.addRow(1, new Label("Capacite"), capaciteField);
        grid.addRow(2, new Label("Batiment"), batimentField);
        grid.addRow(3, new Label("Equipements"), equipementsField);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButton) {
                return null;
            }

            return new SalleFormData(
                    nomField.getText(),
                    Integer.parseInt(capaciteField.getText()),
                    batimentField.getText(),
                    equipementsField.getText()
            );
        });

        return dialog.showAndWait().orElse(null);
    }

    private GridPane creerGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        GridPane.setHgrow(grid, Priority.ALWAYS);
        return grid;
    }

    private void verifierDroitPlanning() {
        SessionContext.getInstance().verifierRole(Role.ADMIN, Role.SCOLARITE);
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Operation impossible");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record CoursFormData(
            String code,
            String intitule,
            BigDecimal volumeHoraireTotal,
            String niveauEtude,
            String filiere,
            String semestre
    ) {
    }

    private record SalleFormData(
            String nom,
            int capacite,
            String batiment,
            String equipements
    ) {
    }
}
