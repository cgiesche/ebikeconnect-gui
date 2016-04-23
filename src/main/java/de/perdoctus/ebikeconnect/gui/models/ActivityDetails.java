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


import java.io.Serializable;
import java.util.List;

public class ActivityDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private ActivityHeader activityHeader;

    private List<Short> heartRate;
    private List<Float> altitudes;
    private List<Float> speeds;
    private List<Short> cadences;

    private List<Coordinate> trackPoints;
    private List<Float> driverTorques;
    private List<Float> motorTorques;
    private List<Short> motorRevolutionRates;
    private List<Float> energyEconomies;

    public ActivityHeader getActivityHeader() {
        return activityHeader;
    }

    public void setActivityHeader(ActivityHeader activityHeader) {
        this.activityHeader = activityHeader;
    }

    /**
     * @return All recorded heart-rates. Values are evenly distributed over driving time.
     */
    public List<Short> getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(List<Short> heartRate) {
        this.heartRate = heartRate;
    }

    /**
     * @return All recorded altitudes. Values are evenly distributed over driving time.
     */
    public List<Float> getAltitudes() {
        return altitudes;
    }

    public void setAltitudes(List<Float> altitudes) {
        this.altitudes = altitudes;
    }

    /**
     * @return All recorded speeds. Values are evenly distributed over driving time.
     */
    public List<Float> getSpeeds() {
        return speeds;
    }

    public void setSpeeds(List<Float> speeds) {
        this.speeds = speeds;
    }

    /**
     * @return All recorded cadences. Values are evenly distributed over driving time.
     */
    public List<Short> getCadences() {
        return cadences;
    }

    public void setCadences(List<Short> cadences) {
        this.cadences = cadences;
    }

    /**
     * @return All recorded trackpoints. Values are evenly distributed over driving time.
     */
    public List<Coordinate> getTrackPoints() {
        return trackPoints;
    }

    public void setTrackPoints(List<Coordinate> trackPoints) {
        this.trackPoints = trackPoints;
    }

    /**
     * @return All recorded driver torques. Values are evenly distributed over driving time.
     */
    public List<Float> getDriverTorques() {
        return driverTorques;
    }

    public void setDriverTorques(List<Float> driverTorque) {
        this.driverTorques = driverTorque;
    }

    /**
     * @return All recorded motor torques. Values are evenly distributed over driving time.
     */
    public List<Float> getMotorTorques() {
        return motorTorques;
    }

    public void setMotorTorques(List<Float> motorTorques) {
        this.motorTorques = motorTorques;
    }

    /**
     * @return All recorded motor revolution rates. Values are evenly distributed over driving time.
     */
    public List<Short> getMotorRevolutionRates() {
        return motorRevolutionRates;
    }

    public void setMotorRevolutionRates(List<Short> motorRevolutionRates) {
        this.motorRevolutionRates = motorRevolutionRates;
    }

    /**
     * @return All recorded energy economy values. Values are evenly distributed over driving time.
     */
    public List<Float> getEnergyEconomies() {
        return energyEconomies;
    }

    public void setEnergyEconomies(List<Float> energyEconomies) {
        this.energyEconomies = energyEconomies;
    }
}
