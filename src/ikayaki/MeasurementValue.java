/*
 * MeasurementValue.java
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

package ikayaki;

import static ikayaki.MeasurementResult.Type.*;
import static java.lang.Math.atan2;
import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Algorithms for calculating values from the measurements. A MeasurementValue object will be passed to the getValue()
 * method of a project to retrieve the desired value.
 *
 * @author Esko Luontola
 */
public abstract class MeasurementValue <T> {

    /**
     * Calculates the average of all X components in geographic coordinates.
     */
    public static final MeasurementValue<Double> GEOGRAPHIC_X =
            new MeasurementValue<Double>("X'", "Am\u00B2", "Average X (geographic coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = 0;
                    for (int i = 0; i < step.getResults(); i++) {
                        MeasurementResult r = step.getResult(i);
                        if (r.getType() != SAMPLE) {
                            continue;
                        }
                        sum += r.getGeographicX();
                        count++;
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the average of all Y components in geographic coordinates.
     */
    public static final MeasurementValue<Double> GEOGRAPHIC_Y =
            new MeasurementValue<Double>("Y'", "Am\u00B2", "Average Y (geographic coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = 0;
                    for (int i = 0; i < step.getResults(); i++) {
                        MeasurementResult r = step.getResult(i);
                        if (r.getType() != SAMPLE) {
                            continue;
                        }
                        sum += r.getGeographicY();
                        count++;
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the average of all Z components in geographic coordinates.
     */
    public static final MeasurementValue<Double> GEOGRAPHIC_Z =
            new MeasurementValue<Double>("Z'", "Am\u00B2", "Average Z (geographic coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = 0;
                    for (int i = 0; i < step.getResults(); i++) {
                        MeasurementResult r = step.getResult(i);
                        if (r.getType() != SAMPLE) {
                            continue;
                        }
                        sum += r.getGeographicZ();
                        count++;
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the average of all X components in sample coordinates.
     */
    public static final MeasurementValue<Double> SAMPLE_X =
            new MeasurementValue<Double>("X", "Am\u00B2", "Average X (sample coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = 0;
                    for (int i = 0; i < step.getResults(); i++) {
                        MeasurementResult r = step.getResult(i);
                        if (r.getType() != SAMPLE) {
                            continue;
                        }
                        sum += r.getSampleX();
                        count++;
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the average of all Y components in sample coordinates.
     */
    public static final MeasurementValue<Double> SAMPLE_Y =
            new MeasurementValue<Double>("Y", "Am\u00B2", "Average Y (sample coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = 0;
                    for (int i = 0; i < step.getResults(); i++) {
                        MeasurementResult r = step.getResult(i);
                        if (r.getType() != SAMPLE) {
                            continue;
                        }
                        sum += r.getSampleY();
                        count++;
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the average of all Z components in sample coordinates.
     */
    public static final MeasurementValue<Double> SAMPLE_Z =
            new MeasurementValue<Double>("Z", "Am\u00B2", "Average Z (sample coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = 0;
                    for (int i = 0; i < step.getResults(); i++) {
                        MeasurementResult r = step.getResult(i);
                        if (r.getType() != SAMPLE) {
                            continue;
                        }
                        sum += r.getSampleZ();
                        count++;
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the normalized average of all X components in geographic coordinates.
     */
    public static final MeasurementValue<Double> GEOGRAPHIC_X_NORMALIZED =
            new NormalizedValue("X'", "Normalized average X (geographic coordinates)") {
                protected Double getValue0(MeasurementStep step) {
                    return GEOGRAPHIC_X.getValue(step);
                }
            };

    /**
     * Calculates the normalized average of all Y components in geographic coordinates.
     */
    public static final MeasurementValue<Double> GEOGRAPHIC_Y_NORMALIZED =
            new NormalizedValue("Y'", "Normalized average Y (geographic coordinates)") {
                protected Double getValue0(MeasurementStep step) {
                    return GEOGRAPHIC_Y.getValue(step);
                }
            };

    /**
     * Calculates the normalized average of all Z components in geographic coordinates.
     */
    public static final MeasurementValue<Double> GEOGRAPHIC_Z_NORMALIZED =
            new NormalizedValue("Z'", "Normalized average Z (geographic coordinates)") {
                protected Double getValue0(MeasurementStep step) {
                    return GEOGRAPHIC_Z.getValue(step);
                }
            };

    /**
     * Calculates the normalized average of all X components in sample coordinates.
     */
    public static final MeasurementValue<Double> SAMPLE_X_NORMALIZED =
            new NormalizedValue("X", "Normalized average X (sample coordinates)") {
                protected Double getValue0(MeasurementStep step) {
                    return SAMPLE_X.getValue(step);
                }
            };

    /**
     * Calculates the normalized average of all Y components in sample coordinates.
     */
    public static final MeasurementValue<Double> SAMPLE_Y_NORMALIZED =
            new NormalizedValue("Y", "Normalized average Y (sample coordinates)") {
                protected Double getValue0(MeasurementStep step) {
                    return SAMPLE_Y.getValue(step);
                }
            };

    /**
     * Calculates the normalized average of all Z components in sample coordinates.
     */
    public static final MeasurementValue<Double> SAMPLE_Z_NORMALIZED =
            new NormalizedValue("Z", "Normalized average Z (sample coordinates)") {
                protected Double getValue0(MeasurementStep step) {
                    return SAMPLE_Z.getValue(step);
                }
            };

    /**
     * Calculates the declination from the component averages in geographic coordinates.
     */
    public static final MeasurementValue<Double> DECLINATION =
            new MeasurementValue<Double>("D", "\u00b0", "Geographic declination") {
                public Double getValue(MeasurementStep step) {
                    Double x = GEOGRAPHIC_X.getValue(step);
                    Double y = GEOGRAPHIC_Y.getValue(step);
                    if (x == null || y == null) {
                        return null;
                    } else {
                        double d = atan2(y, x);
                        if (d < 0.0) {
                            d += PI * 2;
                        }
                        return Math.toDegrees(d);
                    }
                }
            };

    /**
     * Calculates the inclination from the component averages in geographic coordinates.
     */
    public static final MeasurementValue<Double> INCLINATION =
            new MeasurementValue<Double>("I", "\u00b0", "Geographic inclination") {
                public Double getValue(MeasurementStep step) {
                    Double x = GEOGRAPHIC_X.getValue(step);
                    Double y = GEOGRAPHIC_Y.getValue(step);
                    Double z = GEOGRAPHIC_Z.getValue(step);
                    if (x == null || y == null || z == null) {
                        return null;
                    } else {
                        if (x == 0.0) {
                            x = 0.000000000001;
                        }
                        if (y == 0.0) {
                            y = 0.000000000001;
                        }
                        double d = atan(z / sqrt(pow(x, 2) + pow(y, 2)));
                        return Math.toDegrees(d);
                    }
                }
            };

    /**
     * Calculates the length of the vector from the component averages.
     */
    public static final MeasurementValue<Double> MOMENT =
            new MeasurementValue<Double>("M", "Am\u00B2", "Magnetic moment") {
                public Double getValue(MeasurementStep step) {
                    Double x = SAMPLE_X.getValue(step);
                    Double y = SAMPLE_Y.getValue(step);
                    Double z = SAMPLE_Z.getValue(step);
                    if (x == null || y == null || z == null) {
                        return null;
                    } else {
                        return sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2));
                    }
                }
            };

    /**
     * Calculates the magnetic intensity (or remanence) from the moment and the sample's volume or mass (depending on
     * the selected normalization).
     */
    public static final MeasurementValue<Double> MAGNETIZATION =
            new NormalizedValue("J", "Magnetic intensity") { // J=M/volume or J=M/mass
                protected Double getValue0(MeasurementStep step) {
                    return MOMENT.getValue(step);
                }
            };

    /**
     * Calculates the magnetic intensity (or remanence) relative to the first measurement's magnetic intensity.
     */
    public static final MeasurementValue<Double> RELATIVE_MAGNETIZATION =
            new MeasurementValue<Double>("J/Jo", "", "Relative magnetic intensity") {
                public Double getValue(MeasurementStep step) {
                    Project project = step.getProject();
                    if (project == null) {
                        return null;
                    }
                    Double j = MAGNETIZATION.getValue(step);
                    Double j0 = MAGNETIZATION.getValue(project.getStep(0));     // there is at least one step in the project
                    if (j == null || j0 == null) {
                        return null;
                    } else {
                        return j.doubleValue() / j0.doubleValue();
                    }
                }
            };

    /**
     * Calculates the angular standard deviation (Theta 63) from the measurement result set.
     */
    public static final MeasurementValue<Double> THETA63 =
            new MeasurementValue<Double>("\u03b863", "\u00b0", "Angular standard deviation") {
                public Double getValue(MeasurementStep step) {
                    int count = 0;
                    double sumX = 0.0;
                    double sumY = 0.0;
                    double sumZ = 0.0;
                    double sumLength = 0.0;

                    for (int i = 0; i < step.getResults(); i++) {
                        MeasurementResult r = step.getResult(i);
                        if (r.getType() != SAMPLE) {
                            continue;
                        }
                        // TODO: should there be the full vectors, or normalized vectors (length=1)?
                        sumX += r.getSampleX();
                        sumY += r.getSampleY();
                        sumZ += r.getSampleZ();
                        sumLength += r.getSampleVector().length();
                        count++;
                    }
                    if (count < 2) {
                        return null;
                    }
                    double avgLength = sumLength / count;

                    double N = sumLength;
                    double R = sqrt((sumX * sumX) + (sumY * sumY) + (sumZ * sumZ));
                    double k = (N - avgLength) / (N - R);
                    return 81.0 / sqrt(k);
                }
            };

    /**
     * TODO: enter description
     */
    public static final MeasurementValue<Double> SIGNAL_TO_NOISE =
            new MeasurementValue<Double>("caption", "unit", "description") {
                public Double getValue(MeasurementStep step) {
                    if (step == null) {
                        return null;
                    }

                    // compute the average length of the vectors
                    int count = 0;
                    double lengthSum = 0.0;
                    for (MeasurementResult r : step) {
                        lengthSum += r.getSampleVector().length();
                        count++;
                    }
                    if (count < 2) {
                        return null;
                    }
                    double lengthAvg = lengthSum / count;

                    // compute the standard deviation
                    double squareSum = 0.0;
                    for (MeasurementResult r : step) {
                        double d = r.getSampleVector().length() - lengthAvg;
                        squareSum += (d * d);
                    }
                    double stdev = Math.sqrt(squareSum / (count - 1));

                    return lengthAvg / stdev;
                }
            };

    /**
     * TODO: enter description
     */
    public static final MeasurementValue<Double> SIGNAL_TO_DRIFT =
            new MeasurementValue<Double>("caption", "unit", "description") {
                public Double getValue(MeasurementStep step) {
                    if (step == null) {
                        return null;
                    }
                    Double signal = MOMENT.getValue(step);
                    if (signal == null) {
                        return null;
                    }
                    double d = step.getNoise().length();
                    if (d == 0.0) {
                        return null;
                    }
                    return signal.doubleValue() / d;
                }
            };

    /**
     * TODO: enter description
     */
    public static final MeasurementValue<Double> SIGNAL_TO_HOLDER =
            new MeasurementValue<Double>("caption", "unit", "description") {
                public Double getValue(MeasurementStep step) {
                    if (step == null) {
                        return null;
                    }
                    Double signal = MOMENT.getValue(step);
                    if (signal == null) {
                        return null;
                    }
                    double d = step.getHolder().length();
                    if (d == 0.0) {
                        return null;
                    }
                    return signal.doubleValue() / d;
                }
            };


    /*    Begin instance declaration    */

    /**
     * A short name for the value.
     */
    private final String caption;

    /**
     * The unit of the value.
     */
    private final String unit;

    /**
     * A long description of the value.
     */
    private final String description;

    /**
     * Creates a new measurement value.
     *
     * @param caption     a short name for the value.
     * @param unit        the unit of the value.
     * @param description a long description of the value.
     * @throws NullPointerException if any of the arguments is null.
     */
    public MeasurementValue(String caption, String unit, String description) {
        if (caption == null || unit == null || description == null) {
            throw new NullPointerException();
        }
        this.caption = caption;
        this.unit = unit;
        this.description = description;
    }

    /**
     * Calculates a specific value from a measurement step.
     *
     * @param step the step from which the value will be calculated.
     * @return the calculated value, or null if it was not possible to calculate it.
     * @throws NullPointerException if step is null.
     */
    public abstract T getValue(MeasurementStep step);

    /**
     * Returns a short name for the value.
     *
     * @param project the currently active project, or null if no project is active. Used for returning a different text
     *                depending on the project.
     */
    public String getCaption(Project project) {
        return caption;
    }

    /**
     * Returns the unit of the value.
     *
     * @param project the currently active project, or null if no project is active. Used for returning a different text
     *                depending on the project.
     */
    public String getUnit(Project project) {
        return unit;
    }

    /**
     * Returns a long description of the value.
     *
     * @param project the currently active project, or null if no project is active. Used for returning a different text
     *                depending on the project.
     */
    public String getDescription(Project project) {
        return description;
    }
}

/**
 * Specializes the MeasurementValue class with automatic value normalization.
 *
 * @author Esko Luontola
 */
abstract class NormalizedValue extends MeasurementValue<Double> {

    public NormalizedValue(String caption, String description) {
        super(caption, "", description);
    }

    /**
     * Calculates a specific normalized value from a measurement step.
     *
     * @param step the step from which the value will be calculated.
     * @return the calculated value, or null if it was not possible to calculate it. The unit is mA/m or Am^2/kg
     *         depenging on the normalization that was used.
     * @throws NullPointerException if step is null.
     */
    public final Double getValue(MeasurementStep step) {
        return normalize(step, getValue0(step));
    }

    /**
     * Calculates a specific NOT NORMALIZED value from a measurement step.
     *
     * @param step the step from which the value will be calculated.
     * @return the calculated value, or null if it was not possible to calculate it. The unit is Am^2.
     * @throws NullPointerException if step is null.
     */
    protected abstract Double getValue0(MeasurementStep step);

    /**
     * Returns mA/m or Am^2/kg depending on the normalization that the project uses.
     */
    @Override public String getUnit(Project project) {
        if (project == null) {
            return "mA/m";
        } else if (project.getNormalization() == Project.Normalization.VOLUME) {
            return "mA/m";
        } else if (project.getNormalization() == Project.Normalization.MASS) {
            return "Am\u00B2/kg";
        } else {
            assert false;
            return "";
        }
    }

    /**
     * Normalizes a value by mass or volume, as specified in the project or step.
     *
     * @param step  the step whose value is being normalized.
     * @param value the value to be normalised. The unit is Am^2.
     * @return the normalized value if the mass/volume was specified, otherwise null. The unit is mAm^2 divided by m^3
     *         or kg depending on the used normalization.
     */
    private static Double normalize(MeasurementStep step, Double value) {
        Project project = step.getProject();
        if (project == null) {
            return null;
        }

        double normalizer;
        if (project.getNormalization() == Project.Normalization.VOLUME) {
            normalizer = step.getVolume();
            if (normalizer < 0.0) {
                normalizer = project.getVolume();
            }
            normalizer = normalizer / 1000000.0;    // convert cm^3 to m^3

        } else if (project.getNormalization() == Project.Normalization.MASS) {
            normalizer = step.getMass();
            if (normalizer < 0.0) {
                normalizer = project.getMass();
            }
            normalizer = normalizer / 1000.0;       // convert g to kg

        } else {
            assert false;
            return null;
        }
        if (normalizer <= 0.0) {
            return null;
        }

        if (value == null) {
            return null;
        } else {
            return (value * 1000.0) / normalizer;   // convert Am^2 to mAm^2
        }
    }
}