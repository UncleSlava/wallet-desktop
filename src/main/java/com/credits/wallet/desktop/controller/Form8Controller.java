package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.struct.TransactionTabRow;
import com.credits.wallet.desktop.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.01.2018.
 */
public class Form8Controller extends Controller implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(Form8Controller.class);

    @FXML
    private Label toAddress;

    @FXML
    private Label amountInCs;

    @FXML
    private Label transactionFeeValue;

    @FXML
    private void handleSeeOnMonitor() {
        if (AppState.creditMonitorURL==null) {
            Utils.showError("URL for credit monitor not defined");
        } else {
            try {
                Desktop.getDesktop().browse(new URL(AppState.creditMonitorURL+AppState.account).toURI());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                Utils.showError(e.getMessage());
            }
        }
    }

    @FXML
    private void handleOk() {
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handleView() {
        AppState.selectedTransactionRow = new TransactionTabRow();
        AppState.selectedTransactionRow.setTarget(AppState.toAddress);
        AppState.selectedTransactionRow.setCurrency(AppState.coin);
        AppState.selectedTransactionRow.setAmount(Converter.toString(AppState.amount));
        AppState.detailFromHistory=false;
        App.showForm("/fxml/transaction.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.toAddress.setText(AppState.toAddress);
        this.amountInCs.setText(Converter.toString(AppState.amount) + " "+AppState.coin);
        this.transactionFeeValue.setText(Converter.toString(AppState.transactionFeeValue) + " CS");
    }
}
