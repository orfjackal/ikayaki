/*
 * Magnetometer.java
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

package ikayaki.squid;

import ikayaki.Settings;

import java.util.Stack;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Offers an interface for controlling the magnetometer."
 *
 * @author Aki Korpua
 */
public class Magnetometer implements SerialIOListener {

    /**
     * Buffer for incoming messages, readed when needed.
     */
    private Stack<String> messageBuffer;

    /**
     * Synchronous queue for waiting result message from magnetometer
     */
    private SynchronousQueue<String> queue;
    private int pollTimeout = 60;

    /**
     * COM port for communication.
     */
    protected SerialIO serialIO;

    private boolean waitingForMessage = false;

    private boolean measuring = false;


    /**
     * Creates a new magnetometer interface. Opens connection to Magnetometer COM port (if its not open already) and
     * reads settings from the Setting class.
     *
     * @throws SerialIOException
     */
    public Magnetometer() throws SerialIOException {
        this.serialIO = SerialIO.openPort(new SerialParameters(Settings.getMagnetometerPort(), 1200, 0, 0, 8, 1, 0));
        serialIO.addSerialIOListener(this);
        messageBuffer = new Stack<String>();
        queue = new SynchronousQueue<String>();
        try {
            //Original sets range and filter to 1x and disable fast-slew, TODO: check if right, do we need status confirm?
            serialIO.writeMessage("XCR1\r");
            serialIO.writeMessage("XCF1\r");
            serialIO.writeMessage("XCSE\r");
            serialIO.writeMessage("YCR1\r");
            serialIO.writeMessage("YCF1\r");
            serialIO.writeMessage("YCSE\r");
            serialIO.writeMessage("ZCR1\r");
            serialIO.writeMessage("ZCF1\r");
            serialIO.writeMessage("ZCSE\r");
            //and original resets all
            configure('A', 'L', 'P');
            configure('A', 'L', 'C');
            resetCounter('A');

        } catch (SerialIOException e) {
            System.err.println("Error using port in degausser:");
            e.printStackTrace();
        }
    }

    /**
     * Checks which settings have changed and updates the magnetometer interface. This method will be called by the
     * Squid class.
     */
    public void updateSettings() {
        //no settings for this.. really. Only COM port.
    }

    /**
     * Reset settings for axis.
     *
     * @param axis 'X', 'Y', 'Z' or 'A' (all). All characters are in upper case.
     */
    protected void reset(char axis) {
        if (axis != 'X' && axis != 'Y' && axis != 'Z' && axis != 'A') {
            throw new IllegalArgumentException("axis = " + axis);
        }
        try {
            this.serialIO.writeMessage(axis + "R\r");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset counter for axis.
     *
     * @param axis 'X', 'Y', 'Z' or 'A' (all). All characters are in upper case.
     */
    protected void resetCounter(char axis) {
        if (axis != 'X' && axis != 'Y' && axis != 'Z' && axis != 'A') {
            throw new IllegalArgumentException("axis = " + axis);
        }
        try {
            this.serialIO.writeMessage(axis + "RC\r");
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Used for configuring Magnetometer parameters. See subcommand for usages.
     *
     * @param axis       'X', 'Y', 'Z' or 'A' (all). All characters are in upper case.
     * @param subcommand The CONFIGURE subcommands follow: <br/>"F" Set filter configuration. The data subfield sets the
     *                   filter to the indicated range. The four possible data values are: "1" One Hertz Filter; 1 Hz
     *                   "T" Ten Hertz Filter; 10 Hz "H" One hundred Hertz Filter; 100 Hz "W" Wide band filter; WB
     *                   <br/>"R" Set DC SQUID electronic range. The data subfield selects the range desired. The four
     *                   possible data values are: "1" One time range; 1x "T" Ten times range; 10x "H" One hundred times
     *                   range; 100x "E" Extended range; 1000x <br/>"S" Set\reset the fast-slew option. Two data values
     *                   are possible: "E" Enable the fast-slew; turn it on. "D" Disable the fast-slew; turn it off.
     *                   <br/>"L" This subcommand opens or closes the SQUID feedback loop or resets the analog signal to
     *                   +/- 1/2 flux quantum about zero. The three possible data values are: "O" Open the feedback
     *                   loop. (This command also zeros the flux counter) "C" Close the feedback loop. "P" Pulse-reset
     *                   (open then close) the feedback loop. (This command also zeros the flux counter)
     * @param option     see data values from subcommands.
     */
    protected void configure(char axis, char subcommand, char option) {
        if (axis != 'X' && axis != 'Y' && axis != 'Z' && axis != 'A') {
            throw new IllegalArgumentException("axis = " + axis);
        }
        try {
            this.serialIO.writeMessage(axis + "C" + subcommand + option + "\r");
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }
    }

    /**
     * @param axis 'X', 'Y', 'Z' or 'A' (all). All characters are in upper case.
     */
    protected void latchAnalog(char axis) {
        if (axis != 'X' && axis != 'Y' && axis != 'Z' && axis != 'A') {
            throw new IllegalArgumentException("axis = " + axis);
        }
        try {
            this.serialIO.writeMessage(axis + "LD\r");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param axis 'X', 'Y', 'Z' or 'A' (all). All characters are in upper case.
     */
    protected void latchCounter(char axis) {
        if (axis != 'X' && axis != 'Y' && axis != 'Z' && axis != 'A') {
            throw new IllegalArgumentException("axis = " + axis);
        }
        try {
            this.serialIO.writeMessage(axis + "LC\r");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generic send message sender, use with caution and knowledge. Checks if commands are good.
     *
     * @param axis       'X', 'Y' or 'Z'. All characters are in upper case.
     * @param command    "D" Send back the analog data last captured with the LATCH command. The data field is not
     *                   required. <br/>"C" Send back the counter value last captured with the LATCH command. The data
     *                   field is not required. <br/>"S" Send back status. Various pieces of status can be sent by the
     *                   magnetometer electronics.
     * @param datavalues Datavalues one or more: <br/>"A" Send back all status. <br/>"F" Send back all filter status.
     *                   <br/>"R" Send back all range status. <br/>"S" Send back slew status. <br/>"L" Send back SQUID
     *                   feedback loop status. Return feedback, waiting time?
     * @return Returns data wanted, see command and datavalue
     */
    protected String getData(char axis, char command, String datavalues) {
        if (axis != 'X' && axis != 'Y' && axis != 'Z') {
            throw new IllegalArgumentException("axis = " + axis);
        }
        if (command == 'D' || command == 'C') {
            try {
                this.serialIO.writeMessage(axis + "S" + command + "\r");
            } catch (SerialIOException ex) {
                System.err.println(ex);
                return null;
            }
        } else if (command == 'S') {
            try {
                this.serialIO.writeMessage(axis + "S" + command + datavalues + "\r");
            } catch (SerialIOException ex) {
                System.err.println(ex);
                return null;
            }
        } else {
            throw new IllegalArgumentException("command = " + command);
        }
        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) queue.poll(pollTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        return answer;
    }

    /**
     * Pulse reset (open then close) feedback loop for axis. Need to be done before measuring.
     *
     * @param axis 'X', 'Y', 'Z' or 'A' (all). All characters are in upper case.
     */
    public void pulseReset(char axis) {
        configure(axis, 'L', 'P');
        configure(axis, 'L', 'C');
    }

    /**
     * Clears flux counter for axis. Need to be done measuring.
     *
     * @param axis 'X', 'Y', 'Z' or 'A' (all). All characters are in upper case.
     */
    public void clearFlux(char axis) {
        resetCounter(axis);
    }

    /**
     * Latches axes, reads counters and analog. Calculates data from them and returns them.
     *
     * @return Returns 3 double values in following order: (x,y,z)
     */
    public double[] readData() {

        // wait for magnetometer to settle down
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        measuring = true;
        latchCounter('A');

        //read all latched counter values
        double counterX = Double.parseDouble(getData('X', 'C', ""));
        double counterY = Double.parseDouble(getData('Y', 'C', ""));
        double counterZ = Double.parseDouble(getData('Z', 'C', ""));

        latchAnalog('A');

        //Maybe need to read many times and take mean value.
        //But we do it only ones (default for old software)
        //read all latched analog values
        double analogX = Double.parseDouble(getData('X', 'D', ""));
        double analogY = Double.parseDouble(getData('Y', 'D', ""));
        double analogZ = Double.parseDouble(getData('Z', 'D', ""));

        double[] result = new double[3];

        //when to use flux counting and when not? TODO
        result[0] = (counterX + analogX) * Settings.getMagnetometerXAxisCalibration();
        result[1] = (counterY + analogY) * Settings.getMagnetometerYAxisCalibration();
        result[2] = (counterZ + analogZ) * Settings.getMagnetometerZAxisCalibration();

        measuring = false;
        return result;
    }

    public boolean isMeasuring() {
        return measuring;
    }

    /**
     * Returns filter configurations for all axis.
     *
     * @return return filter values for all axis in order (x,y,z). <br/>Values: <br/>"1" One Hertz Filter; 1 Hz <br/>"T"
     *         Ten Hertz Filter; 10 Hz <br/>"H" One hundred Hertz Filter; 100 Hz <br/>"W" Wide band filter; WB
     */
    public char[] getFilters() {
        char[] data = new char[3];

        String answer = getData('X', 'S', "F");
        data[0] = answer.charAt(1);

        answer = getData('Y', 'S', "F");
        data[1] = answer.charAt(1);

        answer = getData('Z', 'S', "F");
        data[2] = answer.charAt(1);

        return data;
    }

    /**
     * Returns range configurations for all axis.
     *
     * @return return filter values for all axis in order (x,y,z). <br/>Values: <br/>"1" One time range; 1x <br/>"T" Ten
     *         times range; 10x <br/>"H" One hundred times range; 100x <br/>"E" Extended range; 1000x
     */
    public char[] getRange() {
        char[] data = new char[3];

        String answer = getData('X', 'S', "R");
        data[0] = answer.charAt(1);

        answer = getData('Y', 'S', "R");
        data[1] = answer.charAt(1);

        answer = getData('Z', 'S', "R");
        data[2] = answer.charAt(1);

        return data;

    }

    /**
     * Returns Fast Slew options value.
     *
     * @return true if Fast Slew is on, false if not. In order (x,y,z).
     */
    public boolean[] getSlew() {
        boolean[] data = new boolean[3];

        String answer = this.getData('X', 'S', "S");
        data[0] = (!answer.equals("SD"));

        answer = this.getData('Y', 'S', "S");
        data[1] = (!answer.equals("SD"));

        answer = this.getData('Z', 'S', "S");
        data[2] = (!answer.equals("SD"));

        return data;
    }

    /**
     * Returns if Loops have been opened on axes.
     *
     * @return return Loop status for all axis in order (x,y,z). Values true = on, false = off.
     */
    public boolean[] getLoop() {
        boolean[] data = new boolean[3];

        String answer = this.getData('X', 'S', "L");
        data[0] = (answer.equals("LO"));

        answer = this.getData('Y', 'S', "L");
        data[1] = (answer.equals("LO"));

        answer = this.getData('Z', 'S', "L");
        data[2] = (answer.equals("LO"));

        return data;
    }

    /**
     * Checks if connection is ok.
     *
     * @return true if ok.
     */
    public boolean isOK() {
        if (serialIO != null) {
            return true;
        }
        return false;
    }

    public void serialIOEvent(SerialIOEvent event) {
        //TODO: problem when Degausser and Magnetometer uses same port :/
        String message = event.getCleanMessage();
        if (message != null) {
            if (waitingForMessage) {
                try {
                    queue.put(message);
                } catch (InterruptedException e) {
                    System.err.println("Interrupted Magnetometer message event");
                } catch (NullPointerException e) {
                    System.err.println("Null from SerialEvent in Magnetometer");
                }
            }
            messageBuffer.add(message);
        }
    }
}
