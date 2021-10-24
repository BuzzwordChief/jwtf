package de.buzzwordchief.jwtf;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.List;

public class MainController {
    private final static Clipboard clipboard = Clipboard.getSystemClipboard();
    private AcronymService acronymService;
    @FXML
    private TextField        searchField;
    @FXML
    private ListView<String> abbreviationList;
    @FXML
    private VBox             loadingContainer;
    @FXML
    private MenuItem         forceUpdateButton;

    //
    // FXML Methoden (Handler, ...)
    //

    @FXML
    private void initialize() {
        // Correct the state
        ((VBox) abbreviationList.getParent()).getChildren().remove(abbreviationList);

        new Thread(() -> {
            acronymService = new AcronymService();
            disableLoadingState();
        }).start();
    }

    @FXML
    private void handleSearch(ActionEvent ignored) {
        List<String> searchResult = acronymService.searchFor(searchField.getText());
        abbreviationList.setItems(FXCollections.observableList(searchResult));
        searchField.selectAll();
    }

    @FXML
    private void handleAbout(ActionEvent ignored) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("jWTF");
        alert.setHeaderText("About jWTF");
        alert.setContentText("""
                                     jWTF is a cross platform GUI program to search through the
                                     MirBSD acronym database.
                                                                          
                                     Author: David May <david.may@hey.com>
                                     Version:\040""" + JWTFApplication.VERSION);
        alert.show();
    }

    @FXML
    private void forceUpdate(ActionEvent ignored) {
        enableLoadingState();
        new Thread(() -> {
            acronymService.updateData();
            abbreviationList.setItems(FXCollections.observableList(acronymService.searchFor(searchField.getText())));
            disableLoadingState();
        }).start();
    }

    //
    // Private Methoden
    //

    private void disableLoadingState() {
        VBox parent = (VBox) loadingContainer.getParent();

        Platform.runLater(() -> {
            searchField.setDisable(false);
            searchField.requestFocus();

            forceUpdateButton.setDisable(false);

            parent.getChildren().add(abbreviationList);
            parent.getChildren().remove(loadingContainer);
        });
    }

    private void enableLoadingState() {
        VBox parent = (VBox) abbreviationList.getParent();

        Platform.runLater(() -> {
            searchField.setDisable(true);

            forceUpdateButton.setDisable(true);

            parent.getChildren().remove(abbreviationList);
            parent.getChildren().add(loadingContainer);
        });
    }

    @FXML
    private void handleCopy(MouseEvent ignored) {
        final ClipboardContent content = new ClipboardContent();
        content.putString(abbreviationList.getFocusModel().getFocusedItem());
        clipboard.setContent(content);
    }
}
