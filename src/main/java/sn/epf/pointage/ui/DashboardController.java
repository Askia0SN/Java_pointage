package sn.epf.pointage.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sn.epf.pointage.dao.AlerteDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.RapportDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.security.SessionContext;
import sn.epf.pointage.service.AuthService;

import java.time.LocalDateTime;

public class DashboardController {

    private final AuthService authService = new AuthService();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final AlerteDAO alerteDAO = new AlerteDAO();
    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final RapportDAO rapportDAO = new RapportDAO();

    @FXML
    private BorderPane rootLayout;

    @FXML
    private VBox dashboardContent;

    @FXML
    private Button professeursButton;

    @FXML
    private Button planningButton;

    @FXML
    private Button pointageButton;

    @FXML
    private Button rapportsButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label infoLabel;

    @FXML
    private Label seancesJourLabel;

    @FXML
    private Label presentsLabel;

    @FXML
    private Label absentsLabel;

    @FXML
    private Label alertesLabel;

    @FXML
    private Label globalStatsLabel;

    public void afficherUtilisateur(Utilisateur utilisateur) {
        Role role = utilisateur.getRole();
        welcomeLabel.setText("Bienvenue, " + utilisateur.getLogin());
        roleLabel.setText("Role : " + role);
        infoLabel.setText(messageParRole(role));
        appliquerDroits(role);
        actualiserStatistiques();
    }

    @FXML
    private void handleShowDashboard() {
        rootLayout.setCenter(dashboardContent);
        actualiserStatistiques();
    }

    @FXML
    private void handleShowProfesseurs() throws Exception {
        SessionContext.getInstance().verifierRole(Role.ADMIN, Role.SCOLARITE);
        Parent professeursView = FXMLLoader.load(getClass().getResource("/fxml/professeurs.fxml"));
        rootLayout.setCenter(creerVueScrollable(professeursView));
    }

    @FXML
    private void handleShowPlanning() throws Exception {
        SessionContext.getInstance().verifierRole(Role.ADMIN, Role.SCOLARITE);
        Parent planningView = FXMLLoader.load(getClass().getResource("/fxml/planning.fxml"));
        rootLayout.setCenter(creerVueScrollable(planningView));
    }

    @FXML
    private void handleShowPointage() throws Exception {
        SessionContext.getInstance().verifierRole(Role.PROFESSEUR);
        Parent pointageView = FXMLLoader.load(getClass().getResource("/fxml/pointage.fxml"));
        rootLayout.setCenter(creerVueScrollable(pointageView));
    }

    @FXML
    private void handleShowRapports() throws Exception {
        SessionContext.getInstance().verifierRole(Role.ADMIN, Role.SCOLARITE);
        Parent rapportsView = FXMLLoader.load(getClass().getResource("/fxml/rapports.fxml"));
        rootLayout.setCenter(creerVueScrollable(rapportsView));
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

    private void appliquerDroits(Role role) {
        boolean gestion = role == Role.ADMIN || role == Role.SCOLARITE;
        boolean professeur = role == Role.PROFESSEUR;

        afficherBouton(professeursButton, gestion);
        afficherBouton(planningButton, gestion);
        afficherBouton(rapportsButton, gestion);
        afficherBouton(pointageButton, professeur);
    }

    private void afficherBouton(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }

    private void actualiserStatistiques() {
        var seancesDuJour = seanceDAO.findSeancesDuJour();
        long presents = seancesDuJour.stream()
                .filter(seance -> seance.getStatut() == StatutSeance.REALISEE)
                .count();
        long absentsProbables = seancesDuJour.stream()
                .filter(seance -> seance.getStatut() == StatutSeance.PLANIFIEE)
                .filter(seance -> seance.getDateHeure().plusMinutes(seance.getDureeMinutes()).isBefore(LocalDateTime.now()))
                .count();
        int alertesDuJour = alerteDAO.findAlertesDuJour().size();
        int professeursActifs = professeurDAO.findActifs().size();
        int rapportsNonPayes = rapportDAO.findNonPayes().size();

        seancesJourLabel.setText(String.valueOf(seancesDuJour.size()));
        presentsLabel.setText(String.valueOf(presents));
        absentsLabel.setText(String.valueOf(absentsProbables));
        alertesLabel.setText(String.valueOf(alertesDuJour));
        globalStatsLabel.setText(
                "Professeurs actifs : " + professeursActifs
                        + "  |  Rapports non payes : " + rapportsNonPayes
                        + "  |  Derniere actualisation : " + LocalDateTime.now().withNano(0)
        );
    }

    private ScrollPane creerVueScrollable(Parent content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scrollPane;
    }
}
