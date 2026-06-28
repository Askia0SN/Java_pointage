package sn.epf.pointage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sn.epf.pointage.config.HibernateConfig;
import sn.epf.pointage.service.BootstrapService;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        new BootstrapService().creerAdminParDefautSiAbsent();

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        stage.setTitle("Pointage Professeurs EPF");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        HibernateConfig.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
