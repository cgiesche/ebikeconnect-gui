package de.perdoctus.ebikeconnect.gui.dialogs;

/*
 * #%L
 * ebikeconnect-gui
 * %%
 * Copyright (C) 2016 Christoph Giesche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import de.perdoctus.fx.Bundle;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.commons.configuration2.Configuration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ResourceBundle;

public class LoginDialog extends Dialog<Credentials> {

    public static final String CFG_PASSWORD = "password";
    public static final String CFG_SAVE_PASSWORD = "savePassword";
    public static final String CFG_USERNAME = "username";

    @Inject
    @Bundle("bundles/LoginDialog")
    private ResourceBundle rb;

    @Inject
    private Configuration config;

    @PostConstruct
    public void init() {
        final ImageView graphic = new ImageView(new Image(getClass().getResource("/app-icon.png").toExternalForm()));
        graphic.setPreserveRatio(true);
        graphic.setFitHeight(64);
        setGraphic(graphic);
        setResizable(true);
        setWidth(400);
        setResizable(false);

        setTitle(rb.getString("dialogTitle"));
        setHeaderText(rb.getString("dialogMessage"));

        final ButtonType loginButtonType = new ButtonType(rb.getString("loginButton"), ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        grid.setPrefWidth(getWidth());
        grid.getColumnConstraints().add(new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, true));
        grid.getColumnConstraints().add(new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.LEFT, true));

        final String rbUsername = rb.getString(CFG_USERNAME);
        final TextField txtUsername = new TextField();
        txtUsername.setPromptText(rbUsername);
        txtUsername.setText(config.getString(CFG_USERNAME, ""));

        final Label lblUsername = new Label(rbUsername);
        lblUsername.setLabelFor(txtUsername);
        grid.add(lblUsername, 0, 0);
        grid.add(txtUsername, 1, 0);

        final String rbPassword = rb.getString(CFG_PASSWORD);
        final PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText(rbPassword);
        if (config.getBoolean(CFG_SAVE_PASSWORD, false)) {
            txtPassword.setText(config.getString(CFG_PASSWORD, ""));
        }

        final Label lblPassword = new Label(rbPassword);
        lblPassword.setLabelFor(txtPassword);
        grid.add(lblPassword, 0, 1);
        grid.add(txtPassword, 1, 1);

        final CheckBox cbSavePassword = new CheckBox(rb.getString("save-password"));
        cbSavePassword.setSelected(config.getBoolean(CFG_SAVE_PASSWORD, false));
        grid.add(cbSavePassword, 1, 2);

        getDialogPane().setContent(grid);

        // Enable/Disable login button depending on whether a username was entered.
        final Node loginButton = getDialogPane().lookupButton(loginButtonType);
        loginButton.disableProperty().bind(txtUsername.textProperty().isEmpty().or(txtPassword.textProperty().isEmpty()));

        setResultConverter(
                buttonType -> {
                    if (buttonType == loginButtonType) {
                        config.setProperty(CFG_USERNAME, txtUsername.getText());
                        config.setProperty(CFG_SAVE_PASSWORD, cbSavePassword.isSelected());
                        if (cbSavePassword.isSelected()) {
                            config.setProperty(CFG_PASSWORD, txtPassword.getText());
                            config.setProperty(CFG_PASSWORD, txtPassword.getText());
                        } else {
                            config.clearProperty(CFG_PASSWORD);
                        }
                        return new Credentials(txtUsername.getText(), txtPassword.getText());
                    } else {
                        return null;
                    }
                }
        );

        txtUsername.requestFocus();
    }
}
