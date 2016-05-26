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
import de.perdoctus.ebikeconnect.gui.models.ActivityDayHeader;
import de.perdoctus.ebikeconnect.gui.models.ActivityDayHeadersFactory;
import de.perdoctus.fx.Bundle;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.inject.Inject;
import java.util.List;
import java.util.ResourceBundle;

public class ActivityDaysHeaderService extends Service<List<ActivityDayHeader>> {

    private final EbikeConnectService ebikeConnectService;
    private final ResourceBundle rb;

    @Inject
    public ActivityDaysHeaderService(final EbikeConnectService ebikeConnectService, @Bundle("bundles/General") final ResourceBundle rb) {
        this.ebikeConnectService = ebikeConnectService;
        this.rb = rb;
    }

    @Override
    protected Task<List<ActivityDayHeader>> createTask() {
        return new Task<List<ActivityDayHeader>>() {
            @Override
            protected List<ActivityDayHeader> call() throws Exception {
                updateMessage(rb.getString("loading-activity-headers"));
                return ActivityDayHeadersFactory.createFrom(ebikeConnectService.getAllActivityHeaders().getActivityList());
            }
        };
    }

}
