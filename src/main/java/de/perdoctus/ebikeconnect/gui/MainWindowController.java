package de.perdoctus.ebikeconnect.gui;

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


import de.perdoctus.ebikeconnect.EbikeConnectService;
import de.perdoctus.ebikeconnect.LoginFailedException;
import de.perdoctus.ebikeconnect.api.login.EBCLoginResponse;
import de.perdoctus.ebikeconnect.api.login.EBCUser;
import de.perdoctus.ebikeconnect.gui.components.userdetails.UserDetails;
import de.perdoctus.ebikeconnect.gui.dialogs.Credentials;
import de.perdoctus.ebikeconnect.gui.dialogs.LoginDialog;
import de.perdoctus.fx.Bundle;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController {

    @Inject
    private UserDetails userDetails;
    @Inject
    private LoginDialog loginDialog;
    @Inject
    private EbikeConnectService ebikeConnectService;
    @Inject
    private Logger logger;
    @Inject
    @Bundle("bundles/General")
    private FXMLLoader fxmlLoader;
    @Inject
    @Bundle("bundles/General")
    private ResourceBundle rb;
    @FXML
    public TabPane tabPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu mnuExport;

    private ActivitiesOverviewController activitiesOverviewController;

    private ObjectProperty<EBCLoginResponse> loginResponse = new SimpleObjectProperty<>();

    public void initialize() throws Exception {
        initUserTab();
        initActivitiesTab();

        Platform.runLater(this::requestLogin);

        if( System.getProperty("os.name","UNKNOWN").toLowerCase().startsWith("mac")) {
            menuBar.setUseSystemMenuBar(true);
        }
    }

    private void initActivitiesTab() throws Exception {
        final Parent parent = fxmlLoader.load(getClass().getResourceAsStream("/fxml/ActivitiesOverview.fxml"));
        this.activitiesOverviewController = fxmlLoader.getController();

        final Tab tab = new Tab(rb.getString("activities"));
        tab.setContent(parent);
        tabPane.getTabs().add(tab);

        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) {
                activitiesOverviewController.reloadHeaders();
            }
        });

        mnuExport.disableProperty().bind(activitiesOverviewController.currentActivityDetailsGroupProperty().isNull());
    }

    private void initUserTab() {
        final ScrollPane userScrollPane = new ScrollPane(userDetails);

        final Tab tab = new Tab(rb.getString("user-details"));
        tab.setContent(userScrollPane);
        tabPane.getTabs().add(tab);

        loginResponse.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                userDetails.setUser(newValue.getUser());
            } else {
                userDetails.setUser(null);
            }
        });
    }

    private void requestLogin() {
        final Optional<Credentials> credentialsOptional = loginDialog.showAndWait();

        if (credentialsOptional.isPresent()) {
            final Credentials credentials = credentialsOptional.get();
            try {
                if (!credentials.getUsername().equals("fake")) { // TODO: Remove me ;)
                    this.loginResponse.setValue(ebikeConnectService.login(credentials.getUsername(), credentials.getPassword()));
                } else {
                    final EBCLoginResponse fakeLoginResponse = new EBCLoginResponse();
                    fakeLoginResponse.setUser(new EBCUser());
                    this.loginResponse.setValue(fakeLoginResponse);
                }
            } catch (LoginFailedException e) {
                logger.warn("Login failed.", e);
                requestLogin();
            }
        } else {
            exitApplication();
        }
    }

    public void exportCurrentActivityAsGPX() {
        activitiesOverviewController.exportCurrentActivityAsGPX();
    }

    public void exportCurrentActivityAsTCX(ActionEvent actionEvent) {
        activitiesOverviewController.exportCurrentActivityAsTCX();
    }

    public void exitApplication() {
        Platform.exit();
    }

    public void showAbout() throws IOException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(tabPane.getScene().getWindow());
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setWidth(640);
        alert.setTitle(rb.getString("application-name"));
        alert.setHeaderText(rb.getString("application-name") + " Version " + rb.getString("app-version"));

        final String aboutInfo = IOUtils.toString(getClass().getResourceAsStream("/about-info.txt"), "UTF-8");
        alert.setContentText(aboutInfo);

        final String licenseInfo = IOUtils.toString(getClass().getResourceAsStream("/license-info.txt"), "UTF-8");

        final Label label = new Label(rb.getString("licenses"));
        final TextArea licenses = new TextArea(licenseInfo);
        licenses.setMaxWidth(Double.MAX_VALUE);
        licenses.setEditable(false);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(licenses, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.show();

    }

    public void openGithubUrl() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create(rb.getString("github-url")));
            } catch (IOException e) {
                logger.error("Failed to open browser.", e);
            }
        }
    }
}
