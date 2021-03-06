/*
 * Person.java
 *
 * Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
 *
 * This file is part of HourParser.
 *
 * HourParser is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * HourParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HourParser; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package hourparser;

import java.io.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Represents a person who has a personal log file.
 *
 * @author Esko Luontola, http://www.orfjackal.net/
 */
public class Person {

    /**
     * Personal log file of this person.
     */
    private File file;

    /**
     * Name of this person, or <code>null</code> if no name is known.
     */
    private String name;

    /**
     * Work records of this person.
     */
    private Vector<Entry> records;

    /**
     * Constructs a new person by reading the data from a log file.
     *
     * @param file the log file of the person.
     * @throws IOException if reading the log file fails.
     */
    public Person(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("No such file " + file);
        }
        this.file = file;
        this.name = null;
        this.records = new Vector<Entry>();
        readFile();
    }

    /**
     * Reads all the information from the the log file of this person.
     *
     * @throws IOException if reading the log file fails.
     */
    private void readFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            Entry e = new Entry(line);
            if (e.isRecord()) {
                // sort list
                int insert = 0;
                Date thisDate = e.getDate();
                for (int i = records.size() - 1; i >= 0; i--) {
                    Date otherDate = records.get(i).getDate();
                    if (thisDate.after(otherDate) || thisDate.equals(otherDate)) {
                        insert = i + 1;
                        break;
                    }
                }
                records.add(insert, e);
            } else if (e.isName() && name == null) {
                name = e.getName();
            }
        }
        reader.close();
    }

    /**
     * Returns the name of this person as defined in the first row of the log file.
     *
     * @return the name of this person or <code>null</code> if no name is set.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the entries the person has made during a time period.
     *
     * @param start the beginning of the time period. Results are <code>&gt;= start</code>.
     * @param end   the end of the time period. Results are <code>&lt; end</code>.
     * @return total hours of work during the time period.
     */
    public Entry[] getEntries(Date start, Date end) {
        Vector<Entry> result = new Vector<Entry>();
        for (Entry e : records) {
            if ((e.getDate().after(start) || e.getDate().equals(start)) && e.getDate().before(end)) {
                result.add(e);
            }
        }
        return result.toArray(new Entry[result.size()]);
    }

    /**
     * Returns how many hours in total the person has made during a time period.
     *
     * @param start the beginning of the time period. Results are <code>&gt;= start</code>.
     * @param end   the end of the time period. Results are <code>&lt; end</code>.
     * @return total hours of work during the time period.
     */
    public double getHours(Date start, Date end) {
        Entry[] entries = getEntries(start, end);
        double sum = 0.0;
        for (Entry e : entries) {
            sum += e.getHours();
        }
        return sum;
    }

    /**
     * Returns how many hours of a specific work the person has made during a time period.
     *
     * @param start the beginning of the time period. Results are <code>&gt;= start</code>.
     * @param end   the end of the time period. Results are <code>&lt; end</code>.
     * @param code  the code of the work type to be included or <code>null</code> to include all.
     * @return total hours of work during the time period.
     */
    public double getHours(Date start, Date end, String code) {
        if (code == null) {
            return getHours(start, end);
        }
        Entry[] entries = getEntries(start, end);
        double sum = 0.0;
        for (Entry e : entries) {
            if (e.getCode().equals(code)) {
                sum += e.getHours();
            }
        }
        return sum;
    }

    /**
     * Returns an array of all different codes used during the specified time period.
     *
     * @param start the beginning of the time period. Results are <code>&gt;= start</code>.
     * @param end   the end of the time period. Results are <code>&lt; end</code>.
     * @return total hours of work during the time period.
     */
    public String[] getCodes(Date start, Date end) {
        Entry[] entries = getEntries(start, end);
        Set<String> codes = new HashSet<String>();
        for (Entry e : entries) {
            codes.add(e.getCode());
        }
        return codes.toArray(new String[codes.size()]);
    }

    /**
     * Returns the time of the first record this person has.
     *
     * @return the time of the first record, or the current time if there are no records.
     */
    public Date getStart() {
        if (records.size() == 0) {
            return new Date();
        } else {
            Date first = records.get(0).getDate();
            for (Entry e : records) {
                if (e.getDate().before(first)) {
                    first = e.getDate();
                }
            }
            return first;
        }
    }

    /**
     * Returns the time of the last record this person has.
     *
     * @return the time of the last record, or the current time if there are no records.
     */
    public Date getEnd() {
        if (records.size() == 0) {
            return new Date();
        } else {
            Date last = records.get(0).getDate();
            for (Entry e : records) {
                if (e.getDate().after(last)) {
                    last = e.getDate();
                }
            }
            return last;
        }
    }
}
