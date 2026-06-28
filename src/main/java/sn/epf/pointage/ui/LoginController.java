package sn.epf.pointage.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.service.AuthService;

public class LoginController {

    private final AuthService authService = new AuthService();

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        errorLabel.setText("");

        try {
            Utilisateur utilisateur = authService.connecter(
                    loginField.getText(),
                    passwordField.getText(),
                    "127.0.0.1"
            );

            ouvrirDashboard(utilisateur);
        } catch (RuntimeException exception) {
            errorLabel.setText(exception.getMessage());
        }
    }

    private void ouvrirDashboard(Utilisateur utilisateur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent dashboard = loader.load();

            DashboardController controller = loader.getController();
            controller.afficherUtilisateur(utilisateur);

            Stage stage = (Stage) loginField.getScene().getWindow();
            Scene scene = new Scene(dashboard, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Pointage Professeurs EPF - Tableau de bord");
        } catch (Exception exception) {
            errorLabel.setText("Impossible d'ouvrir le tableau de bord.");
        }
    }
}
