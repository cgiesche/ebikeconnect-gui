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


import de.perdoctus.ebikeconnect.api.login.EBCLocation;

import java.io.Serializable;

public class Coordinate implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final double EARTH_RADIUS = 6371.0;

    private final double lat;
    private final double lng;

    private static final Coordinate INVALID_COORDINATE = new Coordinate(0, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isValid() {
            return false;
        }

    };

    private Coordinate(final double lat, final double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public static Coordinate from(final Double latitude, final Double longitude) {
        if (latitude == null || longitude == null) {
            return INVALID_COORDINATE;
        }
        return new Coordinate(latitude, longitude);
    }

    public static Coordinate from(final EBCLocation ebcLocation) {
        return from(ebcLocation.getLatitude(), ebcLocation.getLongitude());
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public boolean isValid() {
        return true;
    }

    /**
     * @return the distance (in km) to the given other coordinate.
     */
    public double distanceTo(final Coordinate other) {
        double lat1 = getLat();
        double lng1 = getLng();
        double lat2 = other.getLat();
        double lng2 = other.getLng();

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

}
