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
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.model.enums.TypeContrat;
import sn.epf.pointage.security.SessionContext;
import sn.epf.pointage.service.EnrolementService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProfesseursController {

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final EnrolementService enrolementService = new EnrolementService();

    @FXML
    private TextField searchField;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Professeur> professeursTable;

    @FXML
    private TableColumn<Professeur, String> matriculeColumn;

    @FXML
    private TableColumn<Professeur, String> nomColumn;

    @FXML
    private TableColumn<Professeur, String> prenomColumn;

    @FXML
    private TableColumn<Professeur, String> emailColumn;

    @FXML
    private TableColumn<Professeur, String> telephoneColumn;

    @FXML
    private TableColumn<Professeur, TypeContrat> typeContratColumn;

    @FXML
    private TableColumn<Professeur, BigDecimal> tauxHoraireColumn;

    @FXML
    private TableColumn<Professeur, Boolean> actifColumn;

    @FXML
    private void initialize() {
        matriculeColumn.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        typeContratColumn.setCellValueFactory(new PropertyValueFactory<>("typeContrat"));
        tauxHoraireColumn.setCellValueFactory(new PropertyValueFactory<>("tauxHoraireXOF"));
        actifColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));

        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        chargerProfesseurs(professeurDAO.findAll());
        messageLabel.setText("Liste actualisee.");
    }

    @FXML
    private void handleSearch() {
        String search = searchField.getText();

        if (search == null || search.isBlank()) {
            handleRefresh();
            return;
        }

        chargerProfesseurs(professeurDAO.searchByNom(search.trim()));
        messageLabel.setText("Recherche terminee.");
    }

    @FXML
    private void handleAdd() {
        verifierDroitGestionProfesseurs();

        ProfesseurFormData formData = afficherFormulaireProfesseur(null, true);

        if (formData == null) {
            return;
        }

        try {
            enrolementService.enrollerProfesseur(
                    formData.nom(),
                    formData.prenom(),
                    formData.email(),
                    formData.telephone(),
                    formData.typeContrat(),
                    formData.tauxHoraireXOF(),
                    formData.dateEmbauche(),
                    formData.photo(),
                    formData.motDePasseInitial()
            );

            handleRefresh();
            messageLabel.setText("Professeur enrole avec succes.");
        } catch (RuntimeException exception) {
            afficherErreur(exception.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        verifierDroitGestionProfesseurs();

        Professeur professeur = getProfesseurSelectionne();

        if (professeur == null) {
            afficherErreur("Selectionne d'abord un professeur.");
            return;
        }

        ProfesseurFormData formData = afficherFormulaireProfesseur(professeur, false);

        if (formData == null) {
            return;
        }

        try {
            enrolementService.mettreAJourProfil(
                    professeur.getId(),
                    formData.nom(),
                    formData.prenom(),
                    formData.email(),
                    formData.telephone(),
                    formData.tauxHoraireXOF(),
                    formData.photo()
            );

            handleRefresh();
            messageLabel.setText("Profil professeur mis a jour.");
        } catch (RuntimeException exception) {
            afficherErreur(exception.getMessage());
        }
    }

    @FXML
    private void handleDeactivate() {
        verifierDroitGestionProfesseurs();

        Professeur professeur = getProfesseurSelectionne();

        if (professeur == null) {
            afficherErreur("Selectionne d'abord un professeur.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Desactiver le professeur");
        confirmation.setHeaderText("Desactiver " + professeur.getPrenom() + " " + professeur.getNom() + " ?");
        confirmation.setContentText("Son compte utilisateur sera desactive et ses seances futures planifiees seront annulees.");

        confirmation.showAndWait()
                .filter(buttonType -> buttonType == ButtonType.OK)
                .ifPresent(buttonType -> {
                    enrolementService.desactiverProfesseur(professeur.getId());
                    handleRefresh();
                    messageLabel.setText("Professeur desactive.");
                });
    }

    private void chargerProfesseurs(List<Professeur> professeurs) {
        professeursTable.setItems(FXCollections.observableArrayList(professeurs));
    }

    private Professeur getProfesseurSelectionne() {
        return professeursTable.getSelectionModel().getSelectedItem();
    }

    private void verifierDroitGestionProfesseurs() {
        SessionContext.getInstance().verifierRole(Role.ADMIN, Role.SCOLARITE);
    }

    private ProfesseurFormData afficherFormulaireProfesseur(Professeur professeur, boolean creation) {
        Dialog<ProfesseurFormData> dialog = new Dialog<>();
        dialog.setTitle(creation ? "Ajouter un professeur" : "Modifier un professeur");
        dialog.setHeaderText(creation ? "Enrolement d'un nouveau professeur" : "Modification du profil professeur");

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField nomField = new TextField(professeur != null ? professeur.getNom() : "");
        TextField prenomField = new TextField(professeur != null ? professeur.getPrenom() : "");
        TextField emailField = new TextField(professeur != null ? professeur.getEmail() : "");
        TextField telephoneField = new TextField(professeur != null ? professeur.getTelephone() : "");
        ComboBox<TypeContrat> typeContratBox = new ComboBox<>(FXCollections.observableArrayList(TypeContrat.values()));
        typeContratBox.setValue(professeur != null ? professeur.getTypeContrat() : TypeContrat.VACATAIRE);
        TextField tauxField = new TextField(professeur != null && professeur.getTauxHoraireXOF() != null
                ? professeur.getTauxHoraireXOF().toPlainString()
                : "15000");
        DatePicker dateEmbauchePicker = new DatePicker(professeur != null && professeur.getDateEmbauche() != null
                ? professeur.getDateEmbauche()
                : LocalDate.now());
        TextField photoField = new TextField(professeur != null ? professeur.getPhoto() : "");
        TextField motDePasseField = new TextField(creation ? "Passerelle@123" : "");
        motDePasseField.setDisable(!creation);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.addRow(0, new Label("Nom"), nomField);
        grid.addRow(1, new Label("Prenom"), prenomField);
        grid.addRow(2, new Label("Email"), emailField);
        grid.addRow(3, new Label("Telephone"), telephoneField);
        grid.addRow(4, new Label("Contrat"), typeContratBox);
        grid.addRow(5, new Label("Taux horaire XOF"), tauxField);
        grid.addRow(6, new Label("Date embauche"), dateEmbauchePicker);
        grid.addRow(7, new Label("Photo"), photoField);

        if (creation) {
            grid.addRow(8, new Label("Mot de passe initial"), motDePasseField);
        }

        GridPane.setHgrow(nomField, Priority.ALWAYS);
        GridPane.setHgrow(prenomField, Priority.ALWAYS);
        GridPane.setHgrow(emailField, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButton) {
                return null;
            }

            return new ProfesseurFormData(
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    telephoneField.getText(),
                    typeContratBox.getValue(),
                    new BigDecimal(tauxField.getText()),
                    dateEmbauchePicker.getValue(),
                    photoField.getText(),
                    motDePasseField.getText()
            );
        });

        return dialog.showAndWait().orElse(null);
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Operation impossible");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record ProfesseurFormData(
            String nom,
            String prenom,
            String email,
            String telephone,
            TypeContrat typeContrat,
            BigDecimal tauxHoraireXOF,
            LocalDate dateEmbauche,
            String photo,
            String motDePasseInitial
    ) {
    }
}
