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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;

import java.io.File;
import java.util.List;

public abstract class ExportService extends Service<Void> {

    public ObjectProperty<File> file = new SimpleObjectProperty<>();
    public ObjectProperty<List<ActivityDetails>> activityDetails = new SimpleObjectProperty<>();

    public File getFile() {
        return file.get();
    }

    public ObjectProperty<File> fileProperty() {
        return file;
    }

    public void setFile(final File file) {
        this.file.set(file);
    }

    public List<ActivityDetails> getActivityDetails() {
        return activityDetails.get();
    }

    public ObjectProperty<List<ActivityDetails>> activityDetailsProperty() {
        return activityDetails;
    }

    public void setActivityDetails(List<ActivityDetails> activityDetails) {
        this.activityDetails.set(activityDetails);
    }

    /**
     * @return The file extension of the file that is generated by the exporter.
     */
    public abstract String getFileExtension();

    /**
     * @return A short description of the file type that is generated by the exporter.
     */
    public abstract String getFileTypeDescription();

}