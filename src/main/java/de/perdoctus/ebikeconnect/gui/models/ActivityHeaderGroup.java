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


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ActivityHeaderGroup {

    private final LocalDate date;
    private final List<ActivityHeader> activityHeaders;

    public ActivityHeaderGroup(final LocalDate date, final List<ActivityHeader> activityHeaders) {
        this.date = date;
        this.activityHeaders = activityHeaders;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<ActivityHeader> getActivityHeaders() {
        return activityHeaders;
    }

    public Collection<Long> getActivityIds() {
        return activityHeaders.stream().map(ActivityHeader::getActivityId).collect(Collectors.toList());
    }

    public ActivityType getActivityType() {
        return activityHeaders.get(0).getActivityType();
    }

    public LocalDateTime getStartTime() {
        return activityHeaders.stream().map(ActivityHeader::getStartTime).min((o1, o2) -> o1.isAfter(o2) ? 1 : 0).get();
    }

    public double getDistance() {
        return activityHeaders.stream().mapToDouble(ActivityHeader::getDistance).sum();
    }

    public Duration getDrivingTime() {
        return activityHeaders.stream().map(ActivityHeader::getDrivingTime).reduce(Duration::plus).get();
    }
}
