package com.credits.wallet.desktop;


import com.credits.wallet.desktop.controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.text.*;
import java.util.*;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class App extends Application {
    private static final String ERR_NO_PROPERTIES =
        "The server address could not be determined. Check the presence of the file settings.properties";
    private static final String ERR_NO_API_ADDR =
        "The server address could not be determined. Check the presence of the api.addr parameter in the settings.properties file";
    private static Stage currentStage;

    public static String decSep;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Locale loc=Locale.getDefault();
            DecimalFormatSymbols symbols= new DecimalFormatSymbols(loc);
            char sep=symbols.getDecimalSeparator();
            decSep=Character.toString(sep);

            FileInputStream fis = new FileInputStream("settings.properties");
            Properties property = new Properties();
            property.load(fis);

            String apiAddr = property.getProperty("api.addr");
            if (apiAddr == null || apiAddr.isEmpty()) {
                Utils.showError(ERR_NO_API_ADDR);
            } else {
                if (!apiAddr.endsWith("/")) {
                    apiAddr = apiAddr + "/";
                }
                AppState.apiAddr = apiAddr;
                showForm("/fxml/form0.fxml", "Wallet");
            }
        } catch (Exception e) {
            Utils.showError(ERR_NO_PROPERTIES);
        }
    }

    public static void showForm(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource(fxmlFile));
            AnchorPane pane = (AnchorPane) loader.load();

            final Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene;
            if (currentStage == null) {
                Screen screen = Screen.getPrimary();
                Rectangle2D bounds = screen.getVisualBounds();
                scene = new Scene(pane, bounds.getWidth(), bounds.getHeight() * 0.97);
            } else {
                scene = new Scene(pane, currentStage.getScene().getWidth(), currentStage.getScene().getHeight());
            }

            dialogStage.setScene(scene);
            dialogStage.setResizable(true);

            boolean firstShow = false;
            if (currentStage == null) {
                currentStage = new Stage();
                firstShow = true;
            }
            currentStage.setTitle(title);
            currentStage.setScene(scene);

            if (firstShow) {
                currentStage.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
