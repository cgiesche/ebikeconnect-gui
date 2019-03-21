package de.perdoctus.ebikeconnect.gui.components.userdetails;

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


import de.perdoctus.ebikeconnect.api.login.EBCUser;
import de.perdoctus.fx.Bundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class UserDetails extends GridPane {

    @Inject
    @Bundle("bundles/General")
    private ResourceBundle rb;

    private ObjectProperty<EBCUser> user = new SimpleObjectProperty<>();

    private int rowCount;

    public UserDetails() {
        setPadding(new Insets(20, 10, 0, 10));
        setHgap(10);
        setPrefWidth(400);

        getColumnConstraints().add(new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, false));
        getColumnConstraints().add(new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.LEFT, true));

        user.addListener((observable, oldValue, newValue) -> {
            getChildren().clear();
            if (newValue != null) {
                fillGridWithUserDetails(newValue);
            }
        });
    }

    private void fillGridWithUserDetails(final EBCUser user) {
        addRow(rb.getString("userdata.lastname"), user.getLastName());
        addRow(rb.getString("userdata.firstname"), user.getFirstName());
        addRow(rb.getString("userdata.gender"),
                user.getGender() != null ? rb.getString("userdata.gender." + user.getGender()) : rb.getString("userdata.gender.unknown"));
        addRow(rb.getString("userdata.email-address"), user.getEmail());
        addRow(rb.getString("userdata.date-of-birth"), user.getDateOfBirth());
        addRow(rb.getString("userdata.height"), String.valueOf(user.getHeight()));
        addRow(rb.getString("userdata.weight"), String.valueOf(user.getWeight()));
        addRow(rb.getString("userdata.activity-level"), String.valueOf(user.getActivityLevel()));
        addRow("");
        addRow(rb.getString("userdata.home"));
        addRow(rb.getString("userdata.home.street"),
                user.getHomeAddress().getStreet().concat(" ").concat(user.getHomeAddress().getNumber()));
        addRow(rb.getString("userdata.home.city"),
                user.getHomeAddress().getZip().concat(" ").concat(user.getHomeAddress().getCity()));
        addRow("");
        addRow(rb.getString("userdata.work"));
        addRow(rb.getString("userdata.work.street"),
                user.getWorkAddress().getStreet().concat(" ").concat(user.getWorkAddress().getNumber()));
        addRow(rb.getString("userdata.work.city"),
                user.getWorkAddress().getZip().concat(" ").concat(user.getWorkAddress().getCity()));
    }

    private void addRow(String key) {
        addRow(key, "");
    }

    private void addRow(String key, String value) {
        final Label keyLabel = new Label(key);
        final Label valueLabel = new Label(value);

        addRow(rowCount, keyLabel, valueLabel);
        rowCount++;
    }

    public EBCUser getUser() {
        return user.get();
    }

    public ObjectProperty<EBCUser> userProperty() {
        return user;
    }

    public void setUser(final EBCUser user) {
        this.user.set(user);
    }
}
