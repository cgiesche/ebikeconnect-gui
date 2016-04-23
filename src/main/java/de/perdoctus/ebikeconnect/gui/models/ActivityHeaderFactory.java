package de.perdoctus.ebikeconnect.gui.models;

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


import de.perdoctus.ebikeconnect.api.activities.EBCActivityHeader;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ActivityHeaderFactory {

    public static ActivityHeader createFrom(final EBCActivityHeader activityHeader) {

        final ActivityHeader mappedHeader = new ActivityHeader(activityHeader.getStartTime());

        mappedHeader.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(activityHeader.getStartTime()), ZoneId.systemDefault()));
        mappedHeader.setEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(activityHeader.getEndTime()), ZoneId.systemDefault()));
        mappedHeader.setDrivingTime(Duration.ofMillis(activityHeader.getDrivingTime()));
        mappedHeader.setOperationTime(Duration.ofMillis(activityHeader.getOperationTime()));
        mappedHeader.setActivityType(determineActivityType(activityHeader.getType()));
        mappedHeader.setDistance(activityHeader.getTotalDistance());
        mappedHeader.setCalories(activityHeader.getCalories());

        return mappedHeader;
    }

    private static ActivityType determineActivityType(final String type) {
        try {
            return ActivityType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return ActivityType.UNKNOWN;
        }
    }

}
