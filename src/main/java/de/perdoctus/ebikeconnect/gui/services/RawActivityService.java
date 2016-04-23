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


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.perdoctus.ebikeconnect.EbikeConnectService;
import de.perdoctus.ebikeconnect.gui.cdi.LogPerformance;
import de.perdoctus.ebikeconnect.gui.models.ActivityDetails;
import de.perdoctus.ebikeconnect.gui.models.ActivityDetailsFactory;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;

import javax.inject.Inject;

public class RawActivityService extends Service<ActivityDetails> {

    @Inject
    private EbikeConnectService ebikeConnectService;

    @Inject
    private Logger log;

    private Cache<Long, ActivityDetails> activityDetailsCache;

    private LongProperty activityId = new SimpleLongProperty();

    public RawActivityService() {
        activityDetailsCache = CacheBuilder.newBuilder().maximumSize(40).recordStats().build();
    }

    @Override
    @LogPerformance
    protected Task<ActivityDetails> createTask() {
        return new Task<ActivityDetails>() {
            @Override
            protected ActivityDetails call() throws Exception {
                long startTime = RawActivityService.this.activityId.get();
                final ActivityDetails activityDetails = activityDetailsCache.get(startTime, () -> ActivityDetailsFactory.createFrom(ebikeConnectService.getRawActivity(startTime)));
                log.info(activityDetailsCache.stats().toString());
                return activityDetails;
            }
        };
    }

    public long getActivityId() {
        return activityId.get();
    }

    public LongProperty activityIdProperty() {
        return activityId;
    }

    public void setActivityId(long activityId) {
        this.activityId.set(activityId);
    }
}