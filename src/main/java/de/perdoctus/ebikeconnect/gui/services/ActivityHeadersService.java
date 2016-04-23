package de.perdoctus.ebikeconnect.gui.services;

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
import de.perdoctus.ebikeconnect.gui.models.ActivityHeader;
import de.perdoctus.ebikeconnect.gui.models.ActivityHeaderFactory;
import de.perdoctus.fx.Bundle;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.inject.Inject;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ActivityHeadersService extends Service<List<ActivityHeader>> {

    private static final String TYPE_BIKE_RIDE = "BIKE_RIDE";

    private final EbikeConnectService ebikeConnectService;
    private final ResourceBundle rb;

    @Inject
    public ActivityHeadersService(final EbikeConnectService ebikeConnectService, @Bundle("bundles/General") final ResourceBundle rb) {
        this.ebikeConnectService = ebikeConnectService;
        this.rb = rb;
    }

    @Override
    protected Task<List<ActivityHeader>> createTask() {
        return new Task<List<ActivityHeader>>() {
            @Override
            protected List<ActivityHeader> call() throws Exception {
                updateMessage(rb.getString("loading-activity-headers"));
                return ebikeConnectService.getAllActivityHeaders().getActivityList().stream()
                        .filter(header -> header.getType().equalsIgnoreCase(TYPE_BIKE_RIDE))
                        .map(ActivityHeaderFactory::createFrom)
                        .collect(Collectors.toList());
            }
        };
    }

}
