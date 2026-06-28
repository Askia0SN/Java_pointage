package sn.epf.pointage.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.security.SessionContext;
import sn.epf.pointage.service.AuthService;

public class DashboardController {

    private final AuthService authService = new AuthService();

    @FXML
    private BorderPane rootLayout;

    @FXML
    private VBox dashboardContent;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label infoLabel;

    public void afficherUtilisateur(Utilisateur utilisateur) {
        Role role = utilisateur.getRole();
        welcomeLabel.setText("Bienvenue, " + utilisateur.getLogin());
        roleLabel.setText("Role : " + role);
        infoLabel.setText(messageParRole(role));
    }

    @FXML
    private void handleShowDashboard() {
        rootLayout.setCenter(dashboardContent);
    }

    @FXML
    private void handleShowProfesseurs() throws Exception {
        Parent professeursView = FXMLLoader.load(getClass().getResource("/fxml/professeurs.fxml"));
        rootLayout.setCenter(professeursView);
    }

    @FXML
    private void handleShowPlanning() throws Exception {
        Parent planningView = FXMLLoader.load(getClass().getResource("/fxml/planning.fxml"));
        rootLayout.setCenter(planningView);
    }

    @FXML
    private void handleShowPointage() throws Exception {
        Parent pointageView = FXMLLoader.load(getClass().getResource("/fxml/pointage.fxml"));
        rootLayout.setCenter(pointageView);
    }

    @FXML
    private void handleLogout() throws Exception {
        authService.deconnecter("127.0.0.1");

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Pointage Professeurs EPF");
    }

    private String messageParRole(Role role) {
        if (role == Role.ADMIN) {
            return "Acces administrateur : gestion globale, rapports et pilotage.";
        }

        if (role == Role.SCOLARITE) {
            return "Acces scolarite : planification, enrolement et validation des rapports.";
        }

        if (SessionContext.getInstance().getProfesseurLie().isPresent()) {
            return "Acces professeur : planning personnel, pointage et consultation des rapports.";
        }

        return "Acces professeur : aucun profil professeur n'est encore lie a ce compte.";
    }
}
