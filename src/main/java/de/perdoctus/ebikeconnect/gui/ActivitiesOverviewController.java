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
import de.perdoctus.ebikeconnect.gui.models.ActivityDetails;
import de.perdoctus.ebikeconnect.gui.models.ActivityHeader;
import de.perdoctus.ebikeconnect.gui.models.Coordinate;
import de.perdoctus.ebikeconnect.gui.models.json.LatLng;
import de.perdoctus.ebikeconnect.gui.services.ActivityDetailsService;
import de.perdoctus.ebikeconnect.gui.services.ActivityHeadersService;
import de.perdoctus.ebikeconnect.gui.services.GpxExportService;
import de.perdoctus.ebikeconnect.gui.util.DurationFormatter;
import de.perdoctus.fx.Bundle;
import de.perdoctus.fx.ToggleableSeriesChart;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.StringConverter;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.dialog.ProgressDialog;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ActivitiesOverviewController {

    @Inject
    private Logger logger;
    @Inject
    private ActivityHeadersService activityHeadersService;
    @Inject
    private ActivityDetailsService activityDetailsService;
    @Inject
    private GpxExportService gpxExportService;
    @Inject
    private ObjectMapper objectMapper;
    @Inject
    @Bundle("bundles/General")
    private ResourceBundle rb;

    // ActivitiesTable
    @FXML
    private TableView<ActivityHeader> activitiesTable;
    @FXML
    private TableColumn<ActivityHeader, ImageView> tcType;
    @FXML
    private TableColumn<ActivityHeader, String> tcDistance;
    @FXML
    private TableColumn<ActivityHeader, String> tcDate;

    @FXML
    private TableColumn<ActivityHeader, String> tcDuration;

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
    private ObjectProperty<ActivityDetails> currentActivityDetails = new SimpleObjectProperty<>();

    @FXML
    public void initialize() {
        logger.info("Init!");
        webView.getEngine().load(getClass().getResource("/html/map.html").toExternalForm());

        // Activity Headers
        activityHeadersService.setOnSucceeded(event -> {
            activitiesTable.setItems(FXCollections.observableArrayList(activityHeadersService.getValue()));
        });
        activityHeadersService.setOnFailed(event -> logger.error("Failed to obtain ActivityList!", activityHeadersService.getException()));
        final ProgressDialog activityHeadersProgressDialog = new ProgressDialog(activityHeadersService);
        activityHeadersProgressDialog.initModality(Modality.APPLICATION_MODAL);

        // Activity Details
        activityDetailsService.setOnSucceeded(event -> {
            this.currentActivityDetails.setValue(activityDetailsService.getValue());
        });
        activityDetailsService.setOnFailed(event -> logger.error("Failed to obtain ActivityDetails!", activityHeadersService.getException()));
        final ProgressDialog activityDetailsProgressDialog = new ProgressDialog(activityDetailsService);
        activityDetailsProgressDialog.initModality(Modality.APPLICATION_MODAL);

        // Gpx Export
        gpxExportService.setOnSucceeded(event -> {
            gpxExportFinished();
        });
        gpxExportService.setOnFailed(event -> {
            handleError("Failed to generate GPX File", gpxExportService.getException());
        });

        // -- ActivityList
        tcType.setCellValueFactory(param -> new SimpleObjectProperty<>(new ImageView(new Image(getClass().getResource("/images/" + param.getValue().getActivityType().name() + ".png").toExternalForm()))));
        tcDate.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStartTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))));
        tcDistance.setCellValueFactory(param -> new SimpleStringProperty(Math.floor(param.getValue().getDistance() / 1000) + "km"));
        tcDuration.setCellValueFactory(param -> new SimpleStringProperty(DurationFormatter.formatHhMmSs(param.getValue().getDrivingTime())));

        activitiesTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        activitiesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                final MultipleSelectionModel<ActivityHeader> selectionModel = activitiesTable.getSelectionModel();
                if (!selectionModel.isEmpty()) {
                    loadActivityDetails(selectionModel.getSelectedItem());
                }
            }
        });

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

        // -- Current ActivityDetails
        this.currentActivityDetails.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                activityDetailsChanged(newValue);
            }
        });
    }

    private void handleError(final String message, final Throwable exception) {
        logger.error("Failed to generate GPX file!", exception);
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

    private void activityDetailsChanged(final ActivityDetails activityDetails) {
        final List<Float> speeds = activityDetails.getSpeeds();
        final double timePerPoint = (activityDetails.getActivityHeader().getDrivingTime().getSeconds() / (double) speeds.size());
        logger.info("Time per Point:" + timePerPoint);
        refreshChart(activityDetails);

        refreshMap();

    }

    private void refreshStats() {

    }

    private void refreshMap() {
        final List<Coordinate> trackPoints = getCurrentActivityDetails().getTrackPoints();
        final List<LatLng> latLngs = trackPoints.stream().filter(Coordinate::isValid).map(LatLng::new).collect(Collectors.toList());
        final WebEngine webEngine = webView.getEngine();

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
            webEngine.executeScript("drawPolyline(track);");
            webEngine.executeScript("map.setCenter(bounds.getCenter())");
            webEngine.executeScript("map.setZoom(12)");
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize LatLngs", e);
        }

    }

    private void refreshChart(final ActivityDetails activityDetails) {
        final double drivingTime = activityDetails.getActivityHeader().getDrivingTime().getSeconds();

        chart.getData().clear();
        addChartSeries(rb.getString("altitude"), drivingTime, activityDetails.getAltitudes());
        addChartSeries(rb.getString("speed"), drivingTime, activityDetails.getSpeeds());
        addChartSeries(rb.getString("heart-rate"), drivingTime, activityDetails.getHeartRate());
        addChartSeries(rb.getString("cadence"), drivingTime, activityDetails.getCadences());
        addChartSeries(rb.getString("driver-torque"), drivingTime, activityDetails.getDriverTorques());
        addChartSeries(rb.getString("motor-torque"), drivingTime, activityDetails.getMotorTorques());
        addChartSeries(rb.getString("motor-revolutions"), drivingTime, activityDetails.getMotorRevolutionRates());
        addChartSeries(rb.getString("energy-economy"), drivingTime, activityDetails.getEnergyEconomies());

        chartRangeSlider.setMax(Math.round(drivingTime + 0.5));
        chartRangeSlider.setLowValue(0);
        chartRangeSlider.setHighValue(chartRangeSlider.getMax());
    }

    private void addChartSeries(String title, double drivingTime, List<? extends Number> values) {
        final double timePerPoint = drivingTime / values.size();
        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(title);

        final ObservableList<XYChart.Data<Number, Number>> data = series.getData();
        for (int i = 0; i < values.size(); i += 4) {
            final Number number = values.get(i);
            if (number != null) {
                final XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(i * timePerPoint, number);
                data.add(dataPoint);
            }
        }

        if (data.size() > 0) {
            chart.getData().add(series);
        }
    }

    private void loadActivityDetails(final ActivityHeader selectedItem) {
        if (!activityDetailsService.isRunning()) {
            activityDetailsService.reset();
            activityDetailsService.setActivityId(selectedItem.getActivityId());
            activityDetailsService.start();
        }
    }

    public void reloadHeaders() {
        logger.info("Reloading Headers!");
        if (!activityHeadersService.isRunning()) {
            activityHeadersService.restart();
        }
    }

    public ActivityDetails getCurrentActivityDetails() {
        return currentActivityDetails.get();
    }

    public ObjectProperty<ActivityDetails> currentActivityDetailsProperty() {
        return currentActivityDetails;
    }

    public void setCurrentActivityDetails(ActivityDetails currentActivityDetails) {
        this.currentActivityDetails.set(currentActivityDetails);
    }

    public void exportSelectedActivity() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(rb.getString("gpx-export"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("gpx-file"), "*.gpx"));
        final File file = fileChooser.showSaveDialog(chart.getScene().getWindow());

        if (file != null) {
            gpxExportService.setActivityDetailsProperty(this.currentActivityDetails.get());
            gpxExportService.setFileProperty(file);
            gpxExportService.restart();
        }
    }
}
