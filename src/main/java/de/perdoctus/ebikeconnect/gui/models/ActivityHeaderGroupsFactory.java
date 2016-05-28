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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class ActivityHeaderGroupsFactory {

    private static final String TYPE_BIKE_RIDE = "BIKE_RIDE";

    public static List<ActivityHeaderGroup> createFrom(final Collection<EBCActivityHeader> activityHeaders) {
        final List<EBCActivityHeader> bikeActivityHeaders = activityHeaders.stream().filter(header -> header.getType().equalsIgnoreCase(TYPE_BIKE_RIDE)).collect(Collectors.toList());
        final Map<LocalDate, List<EBCActivityHeader>> groupedBikeActivityHeaders = groupByDay(bikeActivityHeaders);

        return groupedBikeActivityHeaders.entrySet().stream().map(localDateListEntry -> ActivityHeaderGroupFactory.createFrom(localDateListEntry.getKey(), localDateListEntry.getValue())).collect(Collectors.toList());
    }

    private static Map<LocalDate, List<EBCActivityHeader>> groupByDay(Collection<EBCActivityHeader> activityHeaders) {
        final HashMap<LocalDate, List<EBCActivityHeader>> result = new HashMap<>(activityHeaders.size());

        for (EBCActivityHeader activityHeader : activityHeaders) {
            final LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(activityHeader.getStartTime()), ZoneId.systemDefault());
            final LocalDate startDate = LocalDate.from(startTime);

            if (!result.containsKey(startDate)) {
                result.put(startDate, new ArrayList<>());
            }
            result.get(startDate).add(activityHeader);
        }

        return result;
    }

}
