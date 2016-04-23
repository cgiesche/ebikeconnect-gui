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


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.perdoctus.ebikeconnect.EbikeConnectService;
import de.perdoctus.ebikeconnect.UnauthenticatedException;
import de.perdoctus.ebikeconnect.gui.models.ActivityDetails;
import de.perdoctus.ebikeconnect.gui.models.ActivityDetailsFactory;
import de.perdoctus.fx.Bundle;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.*;
import java.util.ResourceBundle;

public class ActivityDetailsService extends Service<ActivityDetails> {

    private final Logger log;
    private final LoadingCache<Long, ActivityDetails> detailsResponseCache;
    private final ResourceBundle rb;
    private LongProperty activityId = new SimpleLongProperty();

    @Inject
    public ActivityDetailsService(@Bundle("bundles/General") final ResourceBundle rb, final PersistentActivityDetailsCacheLoader cacheLoader, final Logger log) {
        this.rb = rb;
        this.log = log;
        detailsResponseCache = CacheBuilder.newBuilder().maximumSize(40).recordStats().build(cacheLoader);
    }

    @Override
    protected Task<ActivityDetails> createTask() {
        return new Task<ActivityDetails>() {
            @Override
            protected ActivityDetails call() throws Exception {
                updateMessage(rb.getString("loading-activity-details"));
                long startTime = ActivityDetailsService.this.activityId.get();
                final ActivityDetails activityDetails = detailsResponseCache.get(startTime);
                log.info(detailsResponseCache.stats().toString());
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

    public static class PersistentActivityDetailsCacheLoader extends CacheLoader<Long, ActivityDetails> {

        static final String CACHE_FOLDER = System.getProperty("user.home") + File.separatorChar + "ebikeconnect-gui" + File.separatorChar + "cache";

        @Inject
        private EbikeConnectService ebikeConnectService;
        @Inject
        private Logger logger;

        @Override
        public ActivityDetails load(final Long key) throws Exception {
            final File cacheFolder = new File(CACHE_FOLDER);
            if (!cacheFolder.exists()) {
                final boolean cacheFolderCreated = cacheFolder.mkdirs();
                if (!cacheFolderCreated) {
                    logger.warn("Failed to create folder " + cacheFolder.getAbsolutePath() + " to persist ActivityDetails.");
                }
            }

            final File cacheFile = new File(CACHE_FOLDER + File.separatorChar + key);
            if (cacheFile.exists()) {
                final ActivityDetails activityDetails = loadPersisted(cacheFile);
                if (activityDetails != null) {
                    return activityDetails;
                }
            }

            return loadAndPersist(key, cacheFile);
        }

        private ActivityDetails loadPersisted(final File cacheFile) {
            try (final ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(cacheFile))) {
                return (ActivityDetails) objectInputStream.readObject();
            } catch (ClassNotFoundException | IOException e) {
                logger.error("Failed to load persisted ActivityDetails from " + cacheFile.getAbsolutePath() + ". Deleting and reloading.", e);
                cacheFile.delete();
                return null;
            }
        }

        private ActivityDetails loadAndPersist(final Long key, final File cacheFile) throws UnauthenticatedException {
            final ActivityDetails activityDetails = ActivityDetailsFactory.createFrom(ebikeConnectService.getRawActivity(key));
            try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
                os.writeObject(activityDetails);
            } catch (IOException e) {
                logger.error("Failed to persist loaded ActivityDetails to " + cacheFile.getAbsolutePath() + ".", e);
            }
            return activityDetails;
        }


    }
}