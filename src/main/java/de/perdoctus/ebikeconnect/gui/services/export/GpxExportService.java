package de.perdoctus.ebikeconnect.gui.services.export;

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


import de.perdoctus.ebikeconnect.gui.models.ActivityDetails;
import de.perdoctus.ebikeconnect.gui.models.Coordinate;
import de.perdoctus.ebikeconnect.jaxb.GpxPrefixMapper;
import de.perdoctus.fx.Bundle;
import de.perdoctus.gpx.*;
import javafx.concurrent.Task;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class GpxExportService extends ExportService {

    @Inject
    @Bundle("bundles/General")
    private ResourceBundle rb;

    private final static ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    @Override
    public String getFileExtension() {
        return "gpx";
    }

    @Override
    public String getFileTypeDescription() {
        return rb.getString("gpx-file");
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (activityDetails.isNotNull().and(file.isNotNull()).get()) {
                    final JAXBElement<GpxType> gpxDocument = createGpxDocument(activityDetails.get());
                    saveGpxDocument(gpxDocument);
                } else {
                    throw new IllegalArgumentException("No activityDetails given.");
                }
                return null;
            }

            private JAXBElement<GpxType> createGpxDocument(final List<ActivityDetails> activityDetailsList) {

                final TrkType track = new TrkType();

                for (final ActivityDetails activityDetails : activityDetailsList) {
                    final TrksegType trackSegment = createTrackSegment(activityDetails);
                    track.getTrkseg().add(trackSegment);
                }

                final MetadataType metadata = createMetadata(activityDetailsList);

                final GpxType gpxType = new GpxType();
                gpxType.setVersion("1.1");
                gpxType.setCreator(rb.getString("application-name") + " " + rb.getString("app-version"));
                gpxType.setMetadata(metadata);
                gpxType.getTrk().add(track);

                return OBJECT_FACTORY.createGpx(gpxType);
            }

            private TrksegType createTrackSegment(final ActivityDetails activityDetails) {
                final List<Coordinate> trackPoints = activityDetails.getTrackPoints();
                int trackpointCount = trackPoints.size();

                final LocalDateTime startTime = activityDetails.getActivityHeader().getStartTime();

                final TrksegType trackSegment = new TrksegType();
                for (int trackPointNr = 0; trackPointNr < trackpointCount; trackPointNr++) {
                    final Coordinate coordinate = trackPoints.get(trackPointNr);

                    if (coordinate.isValid()) {
                        final TrackPointExtensionT pointExtensionT = new TrackPointExtensionT();
                        pointExtensionT.setCad(getValueMatchingValueForTrackpoint(trackPointNr, trackpointCount, activityDetails.getCadences()));
                        pointExtensionT.setHr(getValueMatchingValueForTrackpoint(trackPointNr, trackpointCount, activityDetails.getHeartRate()));

                        final ExtensionsType extensionsType = new ExtensionsType();
                        extensionsType.getAny().add(OBJECT_FACTORY.createTrackPointExtension(pointExtensionT));

                        final LocalDateTime trackpointTime = startTime.plus(trackPointNr, ChronoUnit.SECONDS);

                        final WptType trackpoint = new WptType();
                        trackpoint.setLat(BigDecimal.valueOf(coordinate.getLat()));
                        trackpoint.setLon(BigDecimal.valueOf(coordinate.getLng()));
                        trackpoint.setTime(trackpointTime);
                        final Float matchingValueForTrackpoint = getValueMatchingValueForTrackpoint(trackPointNr, trackpointCount, activityDetails.getAltitudes());
                        if (matchingValueForTrackpoint != null) {
                            trackpoint.setEle(BigDecimal.valueOf(matchingValueForTrackpoint));
                        }
                        trackpoint.setExtensions(extensionsType);

                        trackSegment.getTrkpt().add(trackpoint);
                    }
                }
                return trackSegment;
            }

            private void saveGpxDocument(final JAXBElement<GpxType> gpxDocument) throws JAXBException {
                final JAXBContext jaxbContext = JAXBContext.newInstance("de.perdoctus.gpx");
                final Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new GpxPrefixMapper());
                marshaller.marshal(gpxDocument, file.get());
            }

            private MetadataType createMetadata(List<ActivityDetails> activityDetailsList) {
                final MetadataType metadata = new MetadataType();

                final LocalDateTime startTime = activityDetailsList.stream().map(activityDetails -> activityDetails.getActivityHeader().getStartTime()).min((o1, o2) -> o1.isAfter(o2) ? 1 : 0).get();
                final LocalDateTime endTime = activityDetailsList.stream().map(activityDetails -> activityDetails.getActivityHeader().getEndTime()).max((o1, o2) -> o1.isAfter(o2) ? 1 : 0).get();

                metadata.setName(rb.getString("activity") + " " + startTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)));
                metadata.setDesc(rb.getString("activity") + ": " + startTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)) +
                        " - " + endTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
                metadata.setTime(startTime);
                return metadata;
            }

            private <A> A getValueMatchingValueForTrackpoint(int trackPointNr, int trackPointCount, List<A> values) {
                final float valuesPerTrackpoint = values.size() / (float) trackPointCount;
                int matchingValueIndex = getMatchingIndex(trackPointNr, valuesPerTrackpoint);
                return values.get(matchingValueIndex);
            }

            private int getMatchingIndex(int trackPointNr, float valuesPerTrackpoint) {
                float nearestHeightInfo = trackPointNr * valuesPerTrackpoint;
                return (int) Math.floor(nearestHeightInfo);
            }

        };
    }

}
