package de.perdoctus.ebikeconnect.gui.models;

/*
 * #%L
 * ebikeconnect-api
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


import de.perdoctus.ebikeconnect.api.activities.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ActivityDetailsFactory {

    public static ActivityDetails createFrom(final EBCActivityDetailsResponse activityDetailsResponse) {
        final ActivityDetails activityDetails = new ActivityDetails();
        activityDetails.setActivityHeader(ActivityHeaderFactory.createFrom(activityDetailsResponse.getActivityHeader()));
        activityDetails.setTrackPoints(createCoordinateListFrom(activityDetailsResponse.getCoordinates()));
        activityDetails.setAltitudes(toSingleList(activityDetailsResponse.getPortalAltitudes()));
        activityDetails.setCadences(toSingleList(activityDetailsResponse.getCadence()));
        activityDetails.setHeartRate(toSingleList(activityDetailsResponse.getHeartRate()));
        activityDetails.setSpeeds(toSingleList(activityDetailsResponse.getSpeed()));

        return activityDetails;
    }

    public static ActivityDetails createFrom(final EBCRawActivity rawActivity) {
        final ActivityDetails activityDetails = new ActivityDetails();
        activityDetails.setActivityHeader(ActivityHeaderFactory.createFrom(rawActivity.getActivityHeader()));
        activityDetails.setAltitudes(rawActivity.getAltitudes());

        for (final EBCRawActivityData activityData : rawActivity.getActivityData()) {
            if (activityData instanceof EBCRawActivityData1s) {
                final EBCRawActivityData1s activityData1 = (EBCRawActivityData1s) activityData;
                activityDetails.setCadences(activityData1.getCadence());
                activityDetails.setHeartRate(activityData1.getHeartRate());
                activityDetails.setSpeeds(activityData1.getSpeeds());
                activityDetails.setDriverTorques(activityData1.getDriverTorque());
                activityDetails.setMotorTorques(activityData1.getMotorTorque());
                activityDetails.setMotorRevolutionRates(activityData1.getMotorRevolutionRate());
                activityDetails.setEnergyEconomies(activityData1.getEnergyEconomy());

            }
            if (activityData instanceof EBCRawActivityData600s) {
                final EBCRawActivityData600s activityData600 = (EBCRawActivityData600s) activityData;
                final List<Double> latitudes = activityData600.getLatitudes();
                final List<Double> longitudes = activityData600.getLongitudes();
                final int coordinateCount = Math.min(latitudes.size(), longitudes.size());

                final List<Coordinate> coordinates = new ArrayList<>(coordinateCount);
                for (int i = 0; i < coordinateCount; i++) {
                    coordinates.add(Coordinate.from(latitudes.get(i), longitudes.get(i)));
                }

                activityDetails.setTrackPoints(coordinates);
            }
        }

        return activityDetails;
    }

    private static <E> List<E> toSingleList(List<List<E>> listList) {
        return listList.parallelStream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private static List<Coordinate> createCoordinateListFrom(final List<List<List<Double>>> coordsList) {
        return toSingleList(coordsList).parallelStream().map(latLon -> Coordinate.from(latLon.get(0), latLon.get(1))).collect(Collectors.toList());
    }

}
