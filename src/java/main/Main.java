package main;

import controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.dao.LinguagemDAO;
import service.ILinguagemService;
import service.LinguagemService;
import util.validation.ILinguagemValidador;
import util.validation.LinguagemValidador;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Instancia as dependências concretas
        LinguagemDAO dao = new LinguagemDAO();
        ILinguagemValidador validador = new LinguagemValidador();
        ILinguagemService service = new LinguagemService(dao, validador);

        // 2. Configure o FXMLLoader com uma ControllerFactory
        FXMLLoader loader = new FXMLLoader();
        URL fxmlLocation = getClass().getResource("/com/template/main.fxml");

        if (fxmlLocation == null) {
            System.err.println("Erro: main.fxml não encontrado. Verifique o caminho.");
            return;
        }

        loader.setLocation(fxmlLocation);

        // Fábrica de Controller com verificação de classe 
        loader.setControllerFactory(controllerClass -> {
            if (controllerClass == MainController.class) {
                // Retorna uma nova instância do controller injetando a dependência
                return new MainController(service);
            }
            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Parent root = loader.load(); // Carrega a view usando a fábrica para criar o controller

        Scene scene = new Scene(root, 600, 400);

        stage.setTitle("Cadastro de Linguagens");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}