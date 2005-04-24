/*
 * IntensityPlot.java
 *
 * Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
 *
 * This file is part of Ikayaki.
 *
 * Ikayaki is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Ikayaki is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ikayaki; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ikayaki.gui;

import ikayaki.MeasurementStep;

import java.awt.*;
import java.util.Vector;

/**
 * Implements intensity graph plot.
 *
 * @author
 */
public class IntensityPlot extends AbstractPlot {

    /**
     * Contains all the data that is shown in this graph.
     */
    private Vector<MeasurementStep> measurements = null;

    public void add(MeasurementStep measurement) {
        measurements.add(measurement);
    }

    public void reset() {
        measurements.clear();
    }

    public int getNumMeasurements() {
        return measurements.size();
    }

    /**
     * Draws the contents of the plot
     *
     * @param w
     * @param h
     * @param g2
     */
    public void render(int w, int h, Graphics2D g2) {
        // marigin
        int m = 10;
        // arrow width
        int aw = 4;
        // arrow length
        int al = 8;
        // maximum value of y-axis
        double yMax = 1.1;
        // maximum value of x-axis
        double xMax = 10;
        // font for texts
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        FontMetrics metrics = g2.getFontMetrics();

        // y-axis
        g2.drawLine(m + aw, m, m + aw, (h - m) - aw);
        // x-axis
        g2.drawLine(m + aw, (h - m) - aw, w - m, (h - m) - aw);
        // y-arrow
        g2.drawLine(m, m + al, m + aw, m);
        g2.drawLine(m + (2 * aw), m + al, m + aw, m);
        // x-arrow
        g2.drawLine((w - m) - al, (h - m) - (2 * aw), w - m, (h - m) - aw);
        g2.drawLine((w - m) - al, h - m, w - m, (h - m) - aw);

        // y-axis ticks
        // x-axis ticks
        // y-axis unit
        g2.drawString("J/Jo", m + 30, m + 10);
        // x-axis unit
        g2.drawString("H(mT)", (w - m) - 30, h - (m + 30));
        // origo 0


    }
}