package de.perdoctus.ebikeconnect.gui.services.export;

import de.perdoctus.ebikeconnect.gui.models.ActivityDetails;
import de.perdoctus.ebikeconnect.gui.models.Coordinate;
import de.perdoctus.fx.Bundle;
import de.perdoctus.tcx.*;
import de.perdoctus.tcx.extension.ActivityTrackpointExtensionT;
import de.perdoctus.tcx.extension.CadenceSensorTypeT;
import javafx.concurrent.Task;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TcxExportService extends ExportService {

    @Inject
    @Bundle("bundles/General")
    private ResourceBundle rb;

    private final static ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private final static de.perdoctus.tcx.extension.ObjectFactory OBJECT_FACTORY_EXT = new de.perdoctus.tcx.extension.ObjectFactory();

    @Override
    public String getFileExtension() {
        return "tcx";
    }

    @Override
    public String getFileTypeDescription() {
        return rb.getString("tcx-file");
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {

            int totalDistance = 0;

            @Override
            protected Void call() throws Exception {

                final ApplicationT applicationT = OBJECT_FACTORY.createApplicationT();
                applicationT.setName(rb.getString("application-name") + " " + rb.getString("app-version"));

                final ActivityT activity = OBJECT_FACTORY.createActivityT();
                activity.setSport(SportT.BIKING);
                activity.setCreator(applicationT);
                activity.setId(activityDetails.get().get(0).getActivityHeader().getStartTime());

                for (final ActivityDetails activityDetail : activityDetails.get()) {
                    activity.getLap().add(createLap(activityDetail));
                }

                final ActivityListT activities = OBJECT_FACTORY.createActivityListT();
                activities.getActivity().add(activity);

                final TrainingCenterDatabaseT trainingCenterDatabaseT = OBJECT_FACTORY.createTrainingCenterDatabaseT();
                trainingCenterDatabaseT.setAuthor(applicationT);
                trainingCenterDatabaseT.setActivities(activities);

                saveGpxDocument(OBJECT_FACTORY.createTrainingCenterDatabase(trainingCenterDatabaseT));

                return null;
            }

            private void saveGpxDocument(final JAXBElement<TrainingCenterDatabaseT> tcxDocument) throws JAXBException {
                final JAXBContext jaxbContext = JAXBContext.newInstance("de.perdoctus.tcx:de.perdoctus.tcx.extension");
                final Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new GpxPrefixMapper());
                marshaller.marshal(tcxDocument, file.get());
            }

            private ActivityLapT createLap(final ActivityDetails activityDetail) {
                final LocalDateTime startTime = activityDetail.getActivityHeader().getStartTime();

                final ActivityLapT activityLapT = OBJECT_FACTORY.createActivityLapT();
                activityLapT.setStartTime(startTime);
                activityLapT.setTotalTimeSeconds(activityDetail.getActivityHeader().getDrivingTime().get(ChronoUnit.SECONDS));
                activityLapT.setCalories(activityDetail.getActivityHeader().getCalories());
                activityLapT.setDistanceMeters(activityDetail.getActivityHeader().getDistance());
                activityLapT.setTriggerMethod(TriggerMethodT.MANUAL);
                activityLapT.setIntensity(IntensityT.ACTIVE);

                final List<Short> activityDetailHeartRate = activityDetail.getHeartRate();
                final Optional<Short> heartRateAvg = average(activityDetailHeartRate);
                if (heartRateAvg.isPresent()) {
                    final HeartRateInBeatsPerMinuteT heartRate = OBJECT_FACTORY.createHeartRateInBeatsPerMinuteT();
                    heartRate.setValue(heartRateAvg.get());
                    activityLapT.setAverageHeartRateBpm(heartRate);
                }
                final Optional<Short> heartRateMax = max(activityDetailHeartRate);
                if (heartRateMax.isPresent()) {
                    final HeartRateInBeatsPerMinuteT heartRate = OBJECT_FACTORY.createHeartRateInBeatsPerMinuteT();
                    heartRate.setValue(heartRateMax.get());
                    activityLapT.setMaximumHeartRateBpm(heartRate);
                }

                final OptionalDouble speedMax = maxFloat(activityDetail.getSpeeds());
                if (speedMax.isPresent()) {
                    activityLapT.setMaximumSpeed(speedMax.getAsDouble());
                }

                final Optional<Short> cadenceAvg = average(activityDetail.getCadences());
                if (cadenceAvg.isPresent()) {
                    activityLapT.setCadence(cadenceAvg.get());
                }

                final TrackT track = OBJECT_FACTORY.createTrackT();
                for (int i = 0; i < activityDetail.getTrackPoints().size(); i++) {
                    final Coordinate coordinate = activityDetail.getTrackPoints().get(i);
                    final Short heartRate = activityDetail.getHeartRate().get(i);
                    final Float speed = activityDetail.getSpeeds().get(i);
                    final Short cadence = activityDetail.getCadences().get(i);
                    final Float altitude = activityDetail.getAltitudes().get(i);
                    final Float driverTorque = activityDetail.getDriverTorques().get(i);
                    final Short gaineddistance = activityDetail.getGainedDistances().get(i);
                    final LocalDateTime trackpointTime = startTime.plus(i, ChronoUnit.SECONDS);

                    totalDistance += gaineddistance;
                    track.getTrackpoint().add(createTrackpoint(trackpointTime, coordinate, heartRate, speed, cadence, altitude, driverTorque, totalDistance));
                }
                activityLapT.getTrack().add(track);

                return activityLapT;
            }

            private TrackpointT createTrackpoint(LocalDateTime trackpointTime, Coordinate coordinate, Short heartRate, Float speedKmh, Short cadence, Float altitude, Float driverTorque, int lapDistance) {
                final TrackpointT trackpoint = OBJECT_FACTORY.createTrackpointT();
                trackpoint.setAltitudeMeters(Double.valueOf(altitude));
                trackpoint.setCadence(cadence);
                trackpoint.setDistanceMeters((double) lapDistance);

                if (heartRate != null) {
                    final HeartRateInBeatsPerMinuteT heartRateInBeatsPerMinuteT = OBJECT_FACTORY.createHeartRateInBeatsPerMinuteT();
                    heartRateInBeatsPerMinuteT.setValue(heartRate);
                    trackpoint.setHeartRateBpm(heartRateInBeatsPerMinuteT);
                }

                trackpoint.setTime(trackpointTime);

                if (coordinate.isValid()) {
                    final PositionT positionT = OBJECT_FACTORY.createPositionT();
                    positionT.setLatitudeDegrees(coordinate.getLat());
                    positionT.setLongitudeDegrees(coordinate.getLng());
                    trackpoint.setPosition(positionT);
                }

                final ExtensionsT extensions = OBJECT_FACTORY.createExtensionsT();
                final ActivityTrackpointExtensionT trackpointExtension = OBJECT_FACTORY_EXT.createActivityTrackpointExtensionT();
                trackpointExtension.setCadenceSensor(CadenceSensorTypeT.BIKE);
                if (speedKmh != null) {
                    double speedMeterPerSecond = speedKmh / 3.6;
                    trackpointExtension.setSpeed(speedMeterPerSecond);
                }
                if (driverTorque != null && cadence != null) {
                    final double power = 2 * Math.PI * cadence / 60 * driverTorque;
                    trackpointExtension.setWatts((int) power);
                }

                extensions.getAny().add(OBJECT_FACTORY_EXT.createTPX(trackpointExtension));
                trackpoint.setExtensions(extensions);

                return trackpoint;
            }

            private OptionalDouble maxFloat(List<Float> speeds) {
                return speeds.parallelStream().filter(value -> value != null).mapToDouble(Float::doubleValue).max();
            }

            private Optional<Short> average(List<Short> values) {
                final OptionalDouble average = values.parallelStream().filter(value -> value != null).mapToInt(Short::intValue).average();

                if (average.isPresent()) {
                    return Optional.of((short) average.getAsDouble());
                } else {
                    return Optional.empty();
                }
            }

            private Optional<Short> max(List<Short> values) {
                final OptionalInt max = values.parallelStream().filter(value -> value != null).mapToInt(Short::intValue).max();

                if (max.isPresent()) {
                    return Optional.of((short) max.getAsInt());
                } else {
                    return Optional.empty();
                }
            }
        };
    }
}
