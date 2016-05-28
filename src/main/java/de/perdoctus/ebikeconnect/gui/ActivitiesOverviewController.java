package de.perdoctus.ebikeconnect.gui;

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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.perdoctus.ebikeconnect.gui.components.table.DurationCellFactory;
import de.perdoctus.ebikeconnect.gui.components.table.LocalDateCellFactory;
import de.perdoctus.ebikeconnect.gui.components.table.NumberCellFactory;
import de.perdoctus.ebikeconnect.gui.models.*;
import de.perdoctus.ebikeconnect.gui.models.json.LatLng;
import de.perdoctus.ebikeconnect.gui.services.ActivityDaysHeaderService;
import de.perdoctus.ebikeconnect.gui.services.ActivityDetailsGroupService;
import de.perdoctus.ebikeconnect.gui.services.GpxExportService;
import de.perdoctus.ebikeconnect.gui.util.DurationFormatter;
import de.perdoctus.fx.Bundle;
import de.perdoctus.fx.ToggleableSeriesChart;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.dialog.ProgressDialog;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class ActivitiesOverviewController {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
    @Inject
    private Logger logger;
    @Inject
    private ActivityDaysHeaderService activityDaysHeaderService;
    @Inject
    private ActivityDetailsGroupService activityDetailsGroupService;
    @Inject
    private GpxExportService gpxExportService;
    @Inject
    @Bundle("bundles/General")
    private ResourceBundle rb;

    // Activities Overview
    @FXML
    private TableView<ActivityHeaderGroup> activitiesTable;
    @FXML
    private TableColumn<ActivityHeaderGroup, Number> tcDistance;

    @FXML
    private TableColumn<ActivityHeaderGroup, LocalDate> tcDate;
    @FXML
    private TableColumn<ActivityHeaderGroup, Duration> tcDuration;

    // Activity Segments
    @FXML
    public CheckListView<ActivityHeader> lstSegments;

    // Webview
    @FXML
    private WebView webView;

    // Chart
    @FXML
    private ToggleableSeriesChart<Number, Number> chart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private RangeSlider chartRangeSlider;


    // Properties
    private ObjectProperty<ActivityDetailsGroup> selectedActivityDetails = new SimpleObjectProperty<>();

    @FXML
    public void initialize() {
        logger.info("Init!");
        webView.getEngine().load(getClass().getResource("/html/map.html").toExternalForm());

        // Activity Headers
        activityDaysHeaderService.setOnSucceeded(event -> {
            activitiesTable.setItems(FXCollections.observableArrayList(activityDaysHeaderService.getValue()));
            activitiesTable.getSortOrder().add(tcDate);
            tcDate.setSortable(true);
        });
        activityDaysHeaderService.setOnFailed(event -> logger.error("Failed to obtain ActivityList!", activityDaysHeaderService.getException()));
        final ProgressDialog activityHeadersProgressDialog = new ProgressDialog(activityDaysHeaderService);
        activityHeadersProgressDialog.initModality(Modality.APPLICATION_MODAL);

        // Activity Details
        activityDetailsGroupService.setOnSucceeded(event -> this.selectedActivityDetails.setValue(activityDetailsGroupService.getValue()));
        activityDetailsGroupService.setOnFailed(event -> logger.error("Failed to obtain ActivityDetails!", activityDaysHeaderService.getException()));
        final ProgressDialog activityDetailsProgressDialog = new ProgressDialog(activityDetailsGroupService);
        activityDetailsProgressDialog.initModality(Modality.APPLICATION_MODAL);

        // Gpx Export
        gpxExportService.setOnSucceeded(event -> {
            gpxExportFinished();
        });
        gpxExportService.setOnFailed(event -> {
            handleError("Failed to generate GPX File", gpxExportService.getException());
        });

        // ActivityTable
        tcDate.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getDate()));
        tcDate.setCellFactory(param -> new LocalDateCellFactory());
        tcDate.setSortType(TableColumn.SortType.DESCENDING);

        tcDistance.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getDistance() / 1000));
        tcDistance.setCellFactory(param -> new NumberCellFactory(1, "km"));

        tcDuration.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getDrivingTime()));
        tcDuration.setCellFactory(param -> new DurationCellFactory());

        activitiesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        activitiesTable.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<ActivityHeaderGroup>) c -> {
                    while (c.next()) {
                        if (c.wasRemoved()) {
                            for (ActivityHeaderGroup activityHeaderGroup : c.getRemoved()) {
                                lstSegments.getItems().removeAll(activityHeaderGroup.getActivityHeaders());
                            }

                        }
                        if (c.wasAdded()) {
                            for (ActivityHeaderGroup activityHeaderGroup : c.getAddedSubList()) {
                                if (activityHeaderGroup != null) { // WTF? Why can this be null!?
                                    lstSegments.getItems().addAll(activityHeaderGroup.getActivityHeaders());
                                }
                            }
                        }

                    }
                    lstSegments.getItems().sort((o1, o2) -> o1.getStartTime().isAfter(o2.getStartTime()) ? 1 : 0);
                }
        );

        activitiesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                lstSegments.getCheckModel().checkAll();
                openSelectedSections();
            }
        });

        // Segment List
        lstSegments.setCellFactory(listView ->
                new CheckBoxListCell<>(item -> lstSegments.getItemBooleanProperty(item), new StringConverter<ActivityHeader>() {

                    @Override
                    public ActivityHeader fromString(String arg0) {
                        return null;
                    }

                    @Override
                    public String toString(ActivityHeader activityHeader) {
                        final String startTime = activityHeader.getStartTime().format(DATE_TIME_FORMATTER);
                        final String endTime = activityHeader.getEndTime().format(TIME_FORMATTER);
                        final double distance = Math.floor(activityHeader.getDistance() / 1000);
                        return startTime + " - " + endTime + " (" + distance + " km)";
                    }

                }));

        // -- Chart
        chartRangeSlider.setLowValue(0);
        chartRangeSlider.setHighValue(chartRangeSlider.getMax());

        xAxis.setAutoRanging(false);
        xAxis.lowerBoundProperty().bind(chartRangeSlider.lowValueProperty());
        xAxis.upperBoundProperty().bind(chartRangeSlider.highValueProperty());
        xAxis.tickUnitProperty().bind(chartRangeSlider.highValueProperty().subtract(chartRangeSlider.lowValueProperty()).divide(20));
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                final Duration duration = Duration.of(object.intValue(), ChronoUnit.SECONDS);
                return String.valueOf(DurationFormatter.formatHhMmSs(duration));
            }

            @Override
            public Number fromString(String string) {
                return null;
            }
        });

        chart.getChart().setOnScroll(event -> {
            final double scrollAmount = event.getDeltaY();
            chartRangeSlider.setLowValue(chartRangeSlider.getLowValue() + scrollAmount);
            chartRangeSlider.setHighValue(chartRangeSlider.getHighValue() - scrollAmount);
        });

        // -- Current ActivityDetails
        this.selectedActivityDetails.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                activityGroupChanged(newValue);
            }
        });
    }

    private void handleError(final String message, final Throwable exception) {
        logger.error(exception.getMessage(), exception);
        final Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage(), ButtonType.OK);
        alert.setTitle(message);
        alert.setHeaderText(rb.getString("error-header"));
        alert.show();
    }

    private void gpxExportFinished() {
        final Alert info = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        info.setTitle(rb.getString("gpx-export-finished"));
        info.setHeaderText(rb.getString("gpx-export-finished"));
        info.show();
    }

    private void activityGroupChanged(final ActivityDetailsGroup activityDetailsGroup) {
        refreshChart(activityDetailsGroup);
        refreshMap(activityDetailsGroup);
    }

    private void refreshStats() {

    }

    private void refreshMap(final ActivityDetailsGroup activityDetailsGroup) {
        final List<ActivityDetails> activityDaySegments = activityDetailsGroup.getActivitySegments();

        final ObjectMapper objectMapper = new ObjectMapper();

        final WebEngine webEngine = webView.getEngine();
        webEngine.executeScript("clearPolylines();");

        for (ActivityDetails activityDaySegment : activityDaySegments) {
            final List<Coordinate> trackPoints = activityDaySegment.getTrackPoints();
            final List<LatLng> latLngs = trackPoints.stream().filter(Coordinate::isValid).map(LatLng::new).collect(toList());


            try {
                webEngine.executeScript("var bounds = new google.maps.LatLngBounds();");
                latLngs.stream().forEach(e -> {
                    try {
                        webEngine.executeScript("bounds.extend(new google.maps.LatLng(" + objectMapper.writeValueAsString(e) + "))");
                    } catch (JsonProcessingException e1) {
                        logger.error("Failed to serialize LatLngs", e);
                    }
                });

                webEngine.executeScript("var track = " + objectMapper.writeValueAsString(latLngs) + ";");
                webEngine.executeScript("addTrackSegment(track);");
                webEngine.executeScript("map.setCenter(bounds.getCenter())");
                webEngine.executeScript("map.setZoom(12)");
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize LatLngs", e);
            }
        }


    }

    private void refreshChart(final ActivityDetailsGroup activityDetailsGroup) {
        final List<ActivityDetails> activityDaySegments = activityDetailsGroup.getActivitySegments();

        chart.getData().clear();
        addChartSeries(rb.getString("altitude"), activityDaySegments.stream().flatMap(ad -> ad.getAltitudes().stream()).collect(toList()));
        addChartSeries(rb.getString("speed"), activityDaySegments.stream().flatMap(ad -> ad.getSpeeds().stream()).collect(toList()));
        addChartSeries(rb.getString("heart-rate"), activityDaySegments.stream().flatMap(ad -> ad.getHeartRate().stream()).collect(toList()));
        addChartSeries(rb.getString("cadence"), activityDaySegments.stream().flatMap(ad -> ad.getCadences().stream()).collect(toList()));
        addChartSeries(rb.getString("driver-torque"), activityDaySegments.stream().flatMap(ad -> ad.getDriverTorques().stream()).collect(toList()));
        addChartSeries(rb.getString("motor-torque"), activityDaySegments.stream().flatMap(ad -> ad.getMotorTorques().stream()).collect(toList()));
        addChartSeries(rb.getString("motor-revolutions"), activityDaySegments.stream().flatMap(ad -> ad.getMotorRevolutionRates().stream()).collect(toList()));
        addChartSeries(rb.getString("energy-economy"), activityDaySegments.stream().flatMap(ad -> ad.getEnergyEconomies().stream()).collect(toList()));
        chartRangeSlider.setLowValue(0);
        chartRangeSlider.setHighValue(chartRangeSlider.getMax());
    }

    private void addChartSeries(final String title, final List<? extends Number> samples) {
        logger.info(title + ": " + samples.size() + " samples.");

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(title);

        final ObservableList<XYChart.Data<Number, Number>> data = series.getData();

        for (int i = 0; i < samples.size(); i += 4) {
            final Number number = samples.get(i);
            if (number != null) {
                final XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(i, number);
                data.add(dataPoint);
            }
        }

        chartRangeSlider.setMax(samples.size());

        if (data.size() > 0) {
            chart.getData().add(series);
        }
    }

    @FXML
    private void openSelectedSections() {
        if (activityDetailsGroupService.isRunning()) {
            activityDetailsGroupService.cancel();
        }
        activityDetailsGroupService.reset();
        activityDetailsGroupService.setActivityIds(lstSegments.getCheckModel().getCheckedItems().stream().filter(activityHeader -> activityHeader != null).map(ActivityHeader::getActivityId).collect(Collectors.toList()));
        activityDetailsGroupService.start();
    }

    public void reloadHeaders() {
        logger.info("Reloading Headers!");
        if (!activityDaysHeaderService.isRunning()) {
            activityDaysHeaderService.restart();
        }
    }

    public ActivityDetailsGroup getSelectedActivityDetails() {
        return selectedActivityDetails.get();
    }

    public ObjectProperty<ActivityDetailsGroup> selectedActivityDetailsProperty() {
        return selectedActivityDetails;
    }

    public void setSelectedActivityDetails(ActivityDetailsGroup currentActivityDetailsGroup) {
        this.selectedActivityDetails.set(currentActivityDetailsGroup);
    }

    public void exportSelectedActivity() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(rb.getString("gpx-export"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("gpx-file"), "*.gpx"));
        final File file = fileChooser.showSaveDialog(chart.getScene().getWindow());

        if (file != null) {
            gpxExportService.setActivityDetailsProperty(this.selectedActivityDetails.get().getActivitySegments());
            gpxExportService.setFileProperty(file);
            gpxExportService.restart();
        }
    }

}
