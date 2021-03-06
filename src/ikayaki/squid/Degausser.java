/*
 * Degausser.java
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
 * Offers an interface for controlling the degausser (demagnetizer). Because the data link is implemented in the
 * degausser by a single board computer running a small basic program, the response time of the degausser to commands is
 * slow. This class will make sure that commands are not sent faster than the device can handle.
 *
 * @author Aki Korpua
 */
public class Degausser implements SerialIOListener {

    /**
     * buffer for incoming messages, readed when needed.
     */
    private Stack<String> messageBuffer;

    /**
     * Synchronous queue for waiting result message from degausser
     */
    private SynchronousQueue<String> queue;
    private int pollTimeout = 60;

    /**
     * COM port for communication.
     */
    protected SerialIO serialIO;

    /**
     * 1-9 seconds default delay 1 second
     */
    private int degausserDelay;

    /**
     * (3, 5, 7, 9) default 3
     */
    private int degausserRamp;

    private boolean waitingForMessage = false;
    private double minimumField;
    private double maximumField;

    private boolean demagnetizing = false;

    /**
     * Creates a new degausser interface. Opens connection to degausser COM port (if not open yet) and reads settings
     * from the Setting class.
     */
    public Degausser() throws SerialIOException {
        this.serialIO = SerialIO.openPort(new SerialParameters(Settings.getDegausserPort()));
        serialIO.addSerialIOListener(this);
        messageBuffer = new Stack<String>();
        queue = new SynchronousQueue<String>();
        this.degausserDelay = Settings.getDegausserDelay();
        this.degausserRamp = Settings.getDegausserRamp();
        this.minimumField = Settings.getDegausserMinimumField();
        this.maximumField = Settings.getDegausserMaximumField();

        //needs to call new functions setDelay() and setRamp(). TODO
        //TODO: do we need to check values? (original does)
        try {
            blockingWrite("DCD" + this.degausserDelay);
        } catch (SerialIOException ex1) {
            System.err.println("Error using port in degausser:" + ex1);
        }
        try {
            blockingWrite("DCR" + this.degausserRamp);
        } catch (SerialIOException ex1) {
            System.err.println("Error using port in degausser:" + ex1);
        }
    }

    /**
     * Checks which settings have changed and updates the degausser interface. This method will be called by the Squid
     * class.
     */
    public void updateSettings() {
        // No check, only two options. Doesnt matter.
        this.degausserDelay = Settings.getDegausserDelay();
        this.degausserRamp = Settings.getDegausserRamp();
        this.minimumField = Settings.getDegausserMinimumField();
        this.maximumField = Settings.getDegausserMaximumField();
        try {
            blockingWrite("DCD" + this.degausserDelay);
        } catch (SerialIOException ex1) {
            System.err.println("Error using port in degausser:" + ex1);
        }
        try {
            blockingWrite("DCR" + this.degausserRamp);
        } catch (SerialIOException ex1) {
            System.err.println("Error using port in degausser:" + ex1);
        }
    }

    /**
     * Sets coil X,Y,Z.
     *
     * @param coil coil to set on.
     */
    protected void setCoil(char coil) {
        if (coil == 'X' || coil == 'Y' || coil == 'Z') {
            try {
                blockingWrite("DCC" + coil);
            } catch (SerialIOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("coil = " + coil);
        }
    }

    /**
     * Sets amplitude to ramp, range 1.0 to maximumField. A value of 1.0 will actually be rounded to 1.1 which is the
     * actual minimum amplitude of the degausser.
     *
     * @param amplitude amplitude to demag.
     * @throws IllegalArgumentException if the amplitude is not in the allowed range.
     */
    protected void setAmplitude(double amplitude) {
        if (amplitude >= minimumField && amplitude <= maximumField) {
            try {
                String amps = Integer.toString((int) Math.round(amplitude * 10.0));
                while (amps.length() < 4) {
                    amps = "0" + amps;
                }
                blockingWrite("DCA" + amps);
                Thread.sleep(1500);     // needs to wait for the degausser to process the command

            } catch (SerialIOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("amplitude = " + amplitude);
        }
    }

    /**
     * Performs Ramp up. If this is used, make sure you Ramp down in less than 10 seconds because it can damage coil
     */
    protected void executeRampUp() {
        try {
            blockingWrite("DERU");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Brings Ramp down.
     */
    protected void executeRampDown() {
        try {
            blockingWrite("DERD");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs Ramp up and down.
     */
    protected void executeRampCycle() {
        try {
            blockingWrite("DERC");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    protected void blockingWrite(String command) throws SerialIOException {
        try {
            serialIO.writeMessage(command + "\r");

            waitingForMessage = true;
            String answer = queue.take();
            waitingForMessage = false;

            if (!command.equals(answer)) {
//                for (int i = 0; i < answer.length(); i++) {
//                    System.out.println((int) answer.charAt(i));
//                }
//                throw new IllegalArgumentException("sent: " + command + " recieved: " + answer);
                System.err.println("Degausser.blockingWrite() sent: " + command + " recieved: " + answer);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs full sequence to demagnetize Z coil with the given amplitude. Blocking method.
     *
     * @param amp amplitude to demag.
     * @return true if process was sended succesfully, otherwise false.
     */
    public boolean demagnetizeZ(double amp) {
        setAmplitude(amp);
        setCoil('Z');
        demagnetizing = true;
        executeRampCycle();

        // need to wait for DONE message or TRACK ERROR message
        waitingForMessage = true;
        String answer = null;
        try {
            answer = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        demagnetizing = false;

        if (answer.equals("DONE")) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Performs full sequence to demagnetize Y (and X) coil with the given amplitude. Blocking method.
     *
     * @param amp amplitude to demag.
     * @return true if process was sended succesfully, otherwise false.
     */
    public boolean demagnetizeY(double amp) {
        setAmplitude(amp);
        setCoil('Y');
        demagnetizing = true;
        executeRampCycle();

        // need to wait for DONE message or TRACK ERROR message
        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        demagnetizing = false;

        if (answer.equals("DONE")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isDemagnetizing() {
        return demagnetizing;
    }

    /**
     * Sends status query to degausser and returns answer. Blocking.
     *
     * @return Z=Zero, T=Tracking, ?=Unknown
     */
    public char getRampStatus() {
        try {
            blockingWrite("DSS");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) queue.poll(pollTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        return answer.charAt(1);
    }

    /**
     * Sends ramp query to degausser and returns answer. Blocking.
     *
     * @return 3, 5, 7 or 9
     */
    public int getRamp() {
        try {
            blockingWrite("DSS");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) queue.poll(pollTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        return (int) answer.charAt(4);
    }

    /**
     * Sends delay query to degausser and returns answer. Blocking.
     *
     * @return 1 to 9 as seconds
     */
    public int getDelay() {
        try {
            blockingWrite("DSS");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) queue.poll(pollTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        return (int) answer.charAt(7);
    }

    /**
     * Sends coil query to degausser and returns answer. Blocking.
     *
     * @return X=X Axis, Y=Y Axis, Z=Z Axis, ?=Unknown
     */
    public char getCoil() {
        try {
            blockingWrite("DSS");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) queue.poll(pollTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        return answer.charAt(10);
    }

    /**
     * Sends amplitude query to degausser and returns answer. Blocking.
     *
     * @return 0 to 3000
     */
    public int getAmplitude() {
        try {
            blockingWrite("DSS");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) queue.poll(pollTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;

        return Integer.parseInt(answer.substring(13, 17));
    }

    /**
     * Checks if connection is ok.
     *
     * @return true if ok.
     */
    public boolean isOK() {
        if (this.serialIO != null) {
            return true;
        } else {
            return false;
        }
    }

    public void serialIOEvent(SerialIOEvent event) {
        //TODO: problem when Degausser and Magnetometer uses same port :/
        String message = event.getCleanMessage();
        if (message != null) {
            if (waitingForMessage) {
                try {
                    queue.put(message);
                } catch (InterruptedException e) {
                    System.err.println("Interrupted Degausser message event");
                } catch (NullPointerException e) {
                    System.err.println("Null from SerialEvent in Degausser");
                }
            }
            messageBuffer.add(message);
        }
    }
}
