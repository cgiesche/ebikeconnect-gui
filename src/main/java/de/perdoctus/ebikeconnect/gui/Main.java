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


import de.perdoctus.fx.Bundle;
import de.perdoctus.fx.FxWeldApplication;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.util.logging.LogManager;

public class Main extends FxWeldApplication {

    private final FXMLLoader fxmlLoader;

    @Inject
    public Main(@Bundle("bundles/General") FXMLLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
    }

    @Override
    public void init() throws Exception {
        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/logging.properties"));
    }

    public void start(Stage stage, Application.Parameters parameters) throws Exception {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/app-icon.png")));

        final Parent mainWindow = fxmlLoader.load(getClass().getResourceAsStream("/fxml/MainWindow.fxml"));
        final Scene scene = new Scene(mainWindow);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

}
