package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form5Controller extends Controller implements Initializable {
    private static Logger LOGGER = LoggerFactory.getLogger(Form5Controller.class);

    @FXML
    private Button btnBack;

    @FXML
    private Button btnSaveKeys;

    @FXML
    private Button btnUpload;

    @FXML
    private TextField txKey;

    @FXML
    private TextField txPublic;

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form0.fxml", "Wallet");
    }

    @FXML
    private void handleOpen() {
        open(txPublic.getText(), txKey.getText());
    }

    @FXML
    private void handleSaveKeys() throws WalletDesktopException {
        FileChooser fileChooser = new FileChooser();
        File defaultDirectory = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(defaultDirectory);
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {

            PrintWriter writer = null;
            try {
                writer = new PrintWriter(
                        file.getAbsolutePath(),
                        "UTF-8"
                );
                String json = String.format(
                        "{\"key\":{\"public\":\"%s\",\"private\":\"%s\"}}",
                        Converter.encodeToBASE58(Ed25519.publicKeyToBytes(AppState.publicKey)),
                        Converter.encodeToBASE58(Ed25519.privateKeyToBytes(AppState.privateKey))
                        );
                writer.println(json);
                writer.close();
                Utils.showInfo(String.format("Keys successfully saved in \n\n%s", file.getAbsolutePath()));
            } catch (FileNotFoundException e) {
                throw new WalletDesktopException(e);
            } catch (UnsupportedEncodingException e) {
                throw new WalletDesktopException(e);
            }
        }
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        File defaultDirectory = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(defaultDirectory);
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                InputStream is = new FileInputStream(file);
                BufferedReader buf = new BufferedReader(new InputStreamReader(is));
                String line = buf.readLine();
                StringBuilder sb = new StringBuilder();
                while (line != null) {
                    sb.append(line);
                    line = buf.readLine();
                }
                JsonParser jsonParser = new JsonParser();
                JsonObject jObject = jsonParser.parse(sb.toString()).getAsJsonObject();
                JsonObject key = jObject.getAsJsonObject("key");
                String pubKey = key.get("public").getAsString();
                String privKey = key.get("private").getAsString();

                open(pubKey, privKey);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                Utils.showError("Error reading keys from file " + file.getAbsolutePath() + " " + e.toString());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setVisible(!AppState.newAccount);
        btnSaveKeys.setVisible(AppState.newAccount);
        btnUpload.setVisible(!AppState.newAccount);
        txPublic.setDisable(AppState.newAccount);
        txKey.setDisable(AppState.newAccount);

        if (AppState.newAccount) {
            txKey.setText(Converter.encodeToBASE58(Ed25519.privateKeyToBytes(AppState.privateKey)));
            txPublic.setText(Converter.encodeToBASE58(Ed25519.publicKeyToBytes(AppState.publicKey)));
        }
    }

    private void open(String pubKey, String privKey) {

        AppState.account = pubKey;
        if (AppState.newAccount) {
            // Создание системной транзакции
            try {
                ApiUtils.execSystemTransaction(pubKey);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                Utils.showError("Error creating transaction " + e.toString());
                //return;
            }
        } else {
            try {
                byte[] publicKeyByteArr = Converter.decodeFromBASE58(pubKey);
                byte[] privateKeyByteArr = Converter.decodeFromBASE58(privKey);
                AppState.publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
                AppState.privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
            } catch (Exception e) {
                if (e.getMessage()!=null)
                    Utils.showError(e.getMessage());
                LOGGER.error(e.getMessage(), e);
                //return;
            }
        }

        if (validateKeys(pubKey, privKey))
            App.showForm("/fxml/form6.fxml", "Wallet");
        else
            Utils.showError("Public and private keys pair is not valid");
    }

    private boolean validateKeys(String publicKey, String privateKey) {
        try {
            byte[] publicKeyByteArr = Converter.decodeFromBASE58(publicKey);
            byte[] privateKeyByteArr = Converter.decodeFromBASE58(privateKey);

            if (privateKeyByteArr.length <= 32)
                return false;

            for (int i = 0; i < publicKeyByteArr.length && i < privateKeyByteArr.length - 32; i++) {
                if (publicKeyByteArr[i] != privateKeyByteArr[i + 32])
                    return false;
            }

            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }
}
