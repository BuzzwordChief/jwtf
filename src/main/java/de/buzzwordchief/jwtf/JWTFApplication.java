package de.buzzwordchief.jwtf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class JWTFApplication extends Application {

    static final SemanticVersion VERSION = new SemanticVersion(0, 9, 0);

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(JWTFApplication.class.getResource("main-view.fxml"));
        Scene      scene      = new Scene(fxmlLoader.load(), 320, 240);

        stage.setScene(scene);
        stage.setTitle("jWTF");
        stage.setResizable(false);
        stage.setWidth(450);
        stage.setHeight(500);

        stage.show();
    }
}
