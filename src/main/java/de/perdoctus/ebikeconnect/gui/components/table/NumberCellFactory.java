package de.perdoctus.ebikeconnect.gui.components.table;

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


import de.perdoctus.ebikeconnect.gui.models.ActivityHeaderGroup;
import javafx.scene.control.TableCell;

import java.text.NumberFormat;

public class NumberCellFactory extends TableCell<ActivityHeaderGroup, Number> {

    private final int fractionDigits;
    private final String unit;

    public NumberCellFactory(final int fractionDigits, final String unit) {
        this.fractionDigits = fractionDigits;
        this.unit = unit;
    }

    @Override
    protected void updateItem(Number item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
        } else {
            final NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(fractionDigits);
            numberFormat.setMinimumFractionDigits(fractionDigits);
            setText(numberFormat.format(item) + " " + unit);
        }
    }
}
