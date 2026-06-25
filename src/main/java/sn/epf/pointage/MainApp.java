package sn.epf.pointage;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("Système de gestion de pointage des professeurs");
        StackPane root = new StackPane(title);
        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("Pointage Professeurs EPF");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
