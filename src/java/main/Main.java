package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Caminho correto apontando para a pasta dentro de resources
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/template/main.fxml"));

        Scene scene = new Scene(loader.load(), 600, 400);

        stage.setTitle("Cadastro de Linguagens");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}