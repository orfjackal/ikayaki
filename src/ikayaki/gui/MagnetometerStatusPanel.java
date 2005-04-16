/*
* MagnetometerStatusPanel.java
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

import ikayaki.*;
import ikayaki.squid.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/**
 * Picture of current magnetometer status, with sample holder position and rotation. Status is updated according to
 * MeasurementEvents received by MeasurementControlsPanel. And, manual controls in ManualControlsPanel inner class.
 * Now that I got over myself and painfully merged the two classes.
 *
 * @author Samuli Kaipiainen
 */
public class MagnetometerStatusPanel extends JPanel {

    /**
     * Sample hanlder to read and command current position and rotation from/to.
     */
    private Handler handler;

    /**
     * ManualControlsPanel whose move-radiobuttons to show.
     */
    final ManualControlsPanel manualControlsPanel;

    // handler current position and rotation
    private int position, rotation;

    // handler hard-coded max position and max rotation
    private final int maxposition = 1 << 24, maxrotation = 2000;

    // handler positions, read from Settings, thank you autoboxing!
    private int posLeft;
    private int posHome;
    private int posDemagZ;
    private int posDemagY;
    private int posBG;
    private int posMeasure;
    private int posRight;

    /**
     * Sets magnetometer status to current position.
     */
    public MagnetometerStatusPanel() {
        this.setLayout(new OverlayLayout(this));

        // move-radiobuttons come left to status picture
        this.manualControlsPanel = new ManualControlsPanel();
        add(manualControlsPanel.moveLabel);
        //add(manualControlsPanel.moveLeft);
        add(manualControlsPanel.moveHome);
        add(manualControlsPanel.moveDemagZ);
        add(manualControlsPanel.moveDemagY);
        add(manualControlsPanel.moveBG);
        add(manualControlsPanel.moveMeasure);
        //add(manualControlsPanel.moveRight);

        setPreferredSize(new Dimension(100, 400));
        //setMinimumSize(new Dimension(100, 400));

        // sample handler to read positions and command with move/rotate commands
        readHandler();

        //updateStatus();
        updateStatus(1 << 23, 400); // NOTE: for testing
    }

    /**
     * Reads current sample handler from Squid.instance().getHandler(), saves it to this.handler.
     */
    private void readHandler() {
        try {
            this.handler = Squid.instance().getHandler();
        } catch (IOException ex) { }
    }

    /**
     * Reads handler positions from Settings.
     */
    private void updatePositions() {
        Settings settings = Settings.instance();
        // TODO: what's this?
        //this.posLeft = settings.getHandlerLeftLimit();
        this.posHome = settings.getHandlerSampleLoadPosition();
        this.posDemagZ = settings.getHandlerAxialAFPosition();
        this.posDemagY = settings.getHandlerTransverseYAFPosition();
        this.posBG = settings.getHandlerBackgroundPosition();
        this.posMeasure = settings.getHandlerMeasurementPosition();
        this.posRight = settings.getHandlerRightLimit();
    }

    /**
     * Updates moveButtons' positions.
     */
    private void updateButtonPositions() {
        updateYPosition(manualControlsPanel.moveLabel, 0);
        updateYPosition(manualControlsPanel.moveHome, posHome);
        updateYPosition(manualControlsPanel.moveDemagZ, posDemagZ);
        updateYPosition(manualControlsPanel.moveDemagY, posDemagY);
        updateYPosition(manualControlsPanel.moveBG, posBG);
        updateYPosition(manualControlsPanel.moveMeasure, posMeasure);
        updateYPosition(manualControlsPanel.moveRight, posRight);
    }

    private void updateYPosition(JComponent b, int position) {
        b.setLocation(b.getX(), (int) ((long) getHeight() * position / maxposition));
    }

    /**
     * Updates magnetometer status picture; called by MeasurementControlsPanel when it receives MeasurementEvent.
     *
     * @param position sample holder position, from 1 to 16777215.
     * @param rotation sample holder rotation, from 0 (angle 0) to 2000 (angle 360).
     * @deprecated we read position and rotation ourself in updateStatus().
     */
    public void updateStatus(int position, int rotation) {
        this.position = position;
        this.rotation = rotation;
        updatePositions();
        repaint();
    }

    /**
     * Updates magnetometer status picture; called by MeasurementControlsPanel when it receives MeasurementEvent.
     * Reads current handler position and rotation from Handler saved to this.handler.
     */
    public void updateStatus() {
        if (this.handler != null) {
            this.position = this.handler.getPosition();
            this.rotation = this.handler.getRotation();
        }
        updatePositions();
        repaint();
    }

    /**
     * Paints the magnetometer status picture.
     *
     * @param g mursu.
     */
    protected void paintComponent(Graphics g) {
        // must update radiobuttons' positions here, hope it's safe...
        updateButtonPositions();

        // let Swing erase the background
        super.paintComponent(g);

        // use more sophisticated drawing methods
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2));

        // save our width and height to be handly available
        int w = getWidth();
        int h = getHeight();

        // leave some space for move-radiobuttons
        g2.translate(80, 0);
        w -= 80;

        // sample handler base line x position
        int basex = w / 2;

        // magnetometer boxes' y positions and widths
        int box1y = (int) ((long) h * posDemagZ / maxposition);
        int box2y = (int) ((long) h * posBG / maxposition);
        int box1w = w * 3 / 5;
        int box2w = w * 4 / 5;

        // "sample" width, height and depth, rotation arrow length
        int samplew = w / 3;
        int sampleh = w / 4;
        int sampled = h / 12;
        int arrowlength = w / 6;

        // sample y position
        int sampley = (int) ((long) h * position / maxposition);

        // do the drawing...

        // handler base line
        g2.drawLine(basex, 0, basex, box1y);

        // magnetometer boxes
        g2.drawRect(basex - box1w / 2, box1y, box1w, box2y - box1y);
        g2.drawRect(basex - box2w / 2, box2y, box2w, h - box2y - 2);

        // "sample"
        drawFillOval(g2, Color.WHITE, basex - samplew / 2, sampley - sampled, samplew, sampleh);
        drawFillSideRect(g2, Color.WHITE, basex - samplew / 2, sampley - sampled + sampleh / 2, samplew, sampled);
        drawFillOval(g2, Color.WHITE, basex - samplew / 2, sampley, samplew, sampleh);

        // sample rotation arrow
        drawArrow(g2, basex, sampley + sampleh / 2, arrowlength, rotation);

        // restore original Graphics
        g2.dispose();
    }

    /**
     * Draws a filled oval with line.
     */
    private void drawFillOval(Graphics2D g2, Color fill, int x, int y, int width, int height) {
        Color saved = g2.getColor();
        g2.setColor(fill);
        g2.fillOval(x, y, width, height);
        g2.setColor(saved);
        g2.drawOval(x, y, width, height);
    }

    /**
     * Draws a filled rectangle with lines on left and right side.
     */
    private void drawFillSideRect(Graphics2D g2, Color fill, int x, int y, int width, int height) {
        Color saved = g2.getColor();
        g2.setColor(fill);
        g2.fillRect(x, y, width, height);
        g2.setColor(saved);
        g2.drawLine(x, y, x, y + height);
        g2.drawLine(x + width, y, x + width, y + height);
    }

    /**
     * Draws the rotation arrow.
     *
     * @param g2 marsu.
     * @param x x-center.
     * @param y y-center.
     * @param length arrow length; arrow pointing lines' length will be length/4.
     * @param rotation rotation angle as 0..maxrotation (meaning 0..360 degrees).
     */
    private void drawArrow(Graphics2D g2, int x, int y, int length, int rotation) {
        double rot = Math.PI * 2 * rotation / maxrotation;
        g2.translate(x, y);
        g2.rotate(rot);
        g2.drawLine(0, -length / 2, 0, length / 2);
        g2.drawLine(0, -length / 2, -length / 4, -length / 2 + length / 4);
        g2.drawLine(0, -length / 2, length / 4, -length / 2 + length / 4);
        g2.rotate(-rot);
        g2.translate(-x, -y);
    }

    /**
     * Magnetometer manual controls. MeasurementControlsPanel disables these whenever a normal measurement step is going.
     */
    public class ManualControlsPanel extends JPanel {
        /**
         * Currently open project.
         */
        private Project project;

        /**
         * Groups together all sample holder moving RadioButtons (moveXXX).
         */
        private final ButtonGroup moveButtonGroup = new ButtonGroup();

        /**
         * Moves sample holder to left limit position.
         */
        private final JRadioButton moveLeft = new JRadioButton("Left limit");

        /**
         * Moves sample holder to home position.
         */
        private final JRadioButton moveHome = new JRadioButton("Home");

        /**
         * Moves sample holder to demagnetize-Z position.
         */
        private final JRadioButton moveDemagZ = new JRadioButton("Demag Z");

        /**
         * Moves sample holder to demagnetize-Y position.
         */
        private final JRadioButton moveDemagY = new JRadioButton("Demag Y");

        /**
         * Moves sample holder to background position.
         */
        private final JRadioButton moveBG = new JRadioButton("BG");

        /**
         * Moves sample holder to measurement position.
         */
        private final JRadioButton moveMeasure = new JRadioButton("Measure");

        /**
         * Moves sample holder to right limit position.
         */
        private final JRadioButton moveRight = new JRadioButton("Right limit");

        /**
         * Groups together all sample holder rotating RadioButtons (rotateXXX).
         */
        private final ButtonGroup rotateButtonGroup = new ButtonGroup();

        /**
         * Rotates sample holder to angle 0.
         */
        private final JRadioButton rotate0 = new JRadioButton("0�");

        /**
         * Rotates sample holder to angle 90.
         */
        private final JRadioButton rotate90 = new JRadioButton("90�");

        /**
         * Rotates sample holder to angle 180.
         */
        private final JRadioButton rotate180 = new JRadioButton("180�");

        /**
         * Rotates sample holder to angle 270.
         */
        private final JRadioButton rotate270 = new JRadioButton("270�");

        /**
         * Measures X, Y and Z (at current sample holder position) by calling project.doManualMeasure().
         */
        private final JButton measureAllButton = new JButton("Measure All");
        private final ComponentFlasher measureAllButtonFlasher = new ComponentFlasher(measureAllButton);

        /**
         * Resets X, Y and Z by calling project.doManualReset()? Does what?
         */
        private final JButton resetAllButton = new JButton("Reset All?");
        private final ComponentFlasher resetAllButtonFlasher = new ComponentFlasher(resetAllButton);

        /**
         * Demagnetization amplitude in mT, used when demagZ/YButton is clicked.
         */
        private final JTextField demagAmplitudeField = new JTextField();
        private final JLabel demagAmplitudeLabel = new JLabel("mT");
        private final ComponentFlasher demagAmplitudeFieldFlasher = new ComponentFlasher(demagAmplitudeField);

        /**
         * Demagnetizes in Z (at current sample holder position) by calling project.doManualDemagZ(double).
         */
        private final JButton demagZButton = new JButton("Demag in Z");
        private final ComponentFlasher demagZButtonFlasher = new ComponentFlasher(demagZButton);

        /**
         * Demagnetizes in Y (at current sample holder position) by calling project.doManualDemagY(double).
         */
        private final JButton demagYButton = new JButton("Demag in Y");
        private final ComponentFlasher demagYButtonFlasher = new ComponentFlasher(demagYButton);

        // labels for command groups
        private final JLabel moveLabel = new JLabel("Move");
        private final JLabel rotateLabel = new JLabel("Rotate");
        private final JLabel measureLabel = new JLabel("Measure");
        private final JLabel demagLabel = new JLabel("Demagnetize");

        // don't say anything about this... well, it's like this 'cause the components are scattered all over
        private final Component[] components = new Component[] {
            moveLeft, moveHome, moveDemagZ, moveDemagY, moveBG, moveMeasure, moveRight,
            rotate0, rotate90, rotate180, rotate270,
            measureAllButton, resetAllButton, demagAmplitudeField, demagAmplitudeLabel, demagZButton, demagYButton,
            moveLabel, rotateLabel, measureLabel, demagLabel
        };

        /**
         * Creates our stupid ManualControlsPanel.
         */
        public ManualControlsPanel() {
            moveButtonGroup.add(moveHome);
            moveButtonGroup.add(moveDemagZ);
            moveButtonGroup.add(moveDemagY);
            moveButtonGroup.add(moveBG);
            moveButtonGroup.add(moveMeasure);
            moveButtonGroup.add(moveRight);

            rotateButtonGroup.add(rotate0);
            rotateButtonGroup.add(rotate90);
            rotateButtonGroup.add(rotate180);
            rotateButtonGroup.add(rotate270);

            moveLabel.setFont(moveLabel.getFont().deriveFont(Font.BOLD));
            rotateLabel.setFont(rotateLabel.getFont().deriveFont(Font.BOLD));
            measureLabel.setFont(measureLabel.getFont().deriveFont(Font.BOLD));
            demagLabel.setFont(demagLabel.getFont().deriveFont(Font.BOLD));

            moveHome.setMargin(new Insets(0, 0, 0, 0));
            moveDemagZ.setMargin(new Insets(0, 0, 0, 0));
            moveDemagY.setMargin(new Insets(0, 0, 0, 0));
            moveBG.setMargin(new Insets(0, 0, 0, 0));
            moveMeasure.setMargin(new Insets(0, 0, 0, 0));

            measureAllButton.setMargin(new Insets(1, 1, 1, 1));
            resetAllButton.setMargin(new Insets(1, 1, 1, 1));
            demagZButton.setMargin(new Insets(1, 1, 1, 1));
            demagYButton.setMargin(new Insets(1, 1, 1, 1));

            JPanel rotatePanel = new JPanel(new BorderLayout());
            JPanel rotateButtonPanel = new JPanel(new BorderLayout());
            rotate0.setHorizontalAlignment(JRadioButton.CENTER);
            rotate180.setHorizontalAlignment(JRadioButton.CENTER);
            rotateButtonPanel.add(rotate0, BorderLayout.NORTH);
            rotateButtonPanel.add(rotate90, BorderLayout.EAST);
            rotateButtonPanel.add(rotate180, BorderLayout.SOUTH);
            rotateButtonPanel.add(rotate270, BorderLayout.WEST);
            rotatePanel.add(rotateLabel, BorderLayout.NORTH);
            rotatePanel.add(rotateButtonPanel, BorderLayout.CENTER);

            JPanel measurePanel = new JPanel(new BorderLayout());
            JPanel measureButtonPanel = new JPanel(new GridLayout(3, 1, 0, 4));
            measureButtonPanel.add(measureAllButton);
            measureButtonPanel.add(resetAllButton);
            measurePanel.add(measureLabel, BorderLayout.NORTH);
            measurePanel.add(measureButtonPanel, BorderLayout.CENTER);

            JPanel demagPanel = new JPanel(new BorderLayout());
            JPanel demagButtonPanel = new JPanel(new GridLayout(3, 1, 0, 4));
            JPanel demagAmplitudePanel = new JPanel(new BorderLayout(4, 0));
            demagAmplitudePanel.add(demagAmplitudeField, BorderLayout.CENTER);
            demagAmplitudePanel.add(demagAmplitudeLabel, BorderLayout.EAST);
            demagButtonPanel.add(demagAmplitudePanel);
            demagButtonPanel.add(demagZButton);
            demagButtonPanel.add(demagYButton);
            demagPanel.add(demagLabel, BorderLayout.NORTH);
            demagPanel.add(demagButtonPanel, BorderLayout.CENTER);

            setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
            add(rotatePanel);
            add(measurePanel);
            add(demagPanel);

            //setPreferredSize(new Dimension(100, 400));
            //setMaximumSize(new Dimension(100, 400));

            /*
             * Event A: On moveXXX click - call project.doManualMove(int) with clicked position.
             * If false is returned, show small error message. Position values are found from Settings;
             * demagZ is Settings.instance().getAxialAFPosition() and
             * demagY is Settings.instance().getTransverseYAFPosition().
             */

            moveHome.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handler.moveToHome();
                }
            });

            moveDemagZ.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handler.moveToDegausserZ();
                }
            });

            moveDemagY.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handler.moveToDegausserY();
                }
            });

            moveBG.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handler.moveToBackground();
                }
            });

            moveMeasure.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handler.moveToMeasurement();
                }
            });

            /*
             * Event B: On rotateXXX click - call project.doManualRotate(int) with clicked angle. If
             * false is returned, show small error message.
             */

            rotate0.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handler.rotateTo(0);
                }
            });

            rotate90.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handler.rotateTo(90);
                }
            });

            rotate180.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handler.rotateTo(180);
                }
            });

            rotate270.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handler.rotateTo(270);
                }
            });

            /*
             * Event C: On measureAllButton click - call project.doManualMeasure(). If false is returned,
             * show small error message.
             */
            measureAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!project.doManualMeasure()) measureAllButtonFlasher.flash();
                }
            });

            /*
             * Event D: On resetAllButton click - call project.doManualReset()? If false is returned,
             * show small error message.
             */
            resetAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // TODO: what to do?
                    //if (!project.doManualReset()) resetAllButtonFlasher.flash();
                }
            });

            /*
             * Event E: On DemagZButton click - call project.doManualDemagZ(double) with value
             * from demagAmplitudeField. If false is returned, show small error message.
             */
            demagZButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    double amplitude = getDemagAmplitude();
                    if (amplitude < 0) demagAmplitudeFieldError();
                    else if (!project.doManualDemagZ(amplitude)) demagZButtonFlasher.flash();
                }
            });

            /*
             * Event F: On DemagYButton click - call project.doManualDemagY(double) with value
             * from demagAmplitudeField. If false is returned, show small error message.
             */
            demagYButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    double amplitude = getDemagAmplitude();
                    if (amplitude < 0) demagAmplitudeFieldError();
                    else if (!project.doManualDemagY(amplitude)) demagYButtonFlasher.flash();
                }
            });
        }

        /**
         * Reads demag amplitude from demagAmplitudeField.
         *
         * @return double demagAmplitudeField's double-value, or, -1 if not valid.
         */
        private double getDemagAmplitude() {
            double amplitude;
            try {
                amplitude = Double.valueOf(demagAmplitudeField.getText());
            } catch (NumberFormatException ex) {
                amplitude = -1;
            }

            return amplitude;
        }

        /**
         * Notifies of an error in demagAmplitudeField double-value: requests focus and flashes it.
         */
        private void demagAmplitudeFieldError() {
            //demagAmplitudeField.selectAll();
            demagAmplitudeField.requestFocusInWindow();
            demagAmplitudeFieldFlasher.flash();
        }

        /**
         * Enables/disables all our components. If enabled, also sets selected radioboxes to current handler status.
         *
         * @param enabled true==enabled, false==disabled.
         */
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (handler == null) enabled = false;
            for (Component component : components) component.setEnabled(enabled);

            // TODO: if enabled==true set selected radiobexes according to current handler status...
        }

        /**
         * Set active project, enable ourself if it's non-null.
         *
         * @param project active project, or null for none.
         */
        public void setProject(Project project) {
            this.project = project;

            if (this.project == null) setEnabled(false);
            else setEnabled(project.isManualControlEnabled());
        }
    }
}
