package hourparser;

import java.util.Vector;
import java.util.Date;
import java.util.Calendar;

/**
 * Formats the data of persons to HTML format.
 */
public class Report {

    private final Person[] persons;

    public Report(Vector<Person> persons) {
        this(persons.toArray(new Person[persons.size()]));
    }

    /**
     * Creates a report from an array of Persons.
     *
     * @param persons Persons to be included in the report; must be at least one
     */
    public Report(Person[] persons) {
        if (persons.length == 0) {
            throw new IllegalArgumentException("Length of persons array must be 1 or greater");
        }
        this.persons = persons;
        process();
    }

    /**
     * Collects the data from the persons and saves it for later use in the reports.
     */
    private void process() {
        // find out the beginning and end of the statistics
        Date start = persons[0].getStart();
        Date end = persons[0].getEnd();
        for (Person p : persons) {
            Date date;
            date = p.getStart();
            if (date.before(start)) {
                start = date;
            }
            date = p.getEnd();
            if (date.after(end)) {
                end = date;
            }
        }

        // get the beginning of the first week
        Calendar weekStart = Calendar.getInstance();
        weekStart.setTime(start);
        weekStart.set(Calendar.HOUR, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);
        while (weekStart.get(Calendar.DAY_OF_WEEK) != weekStart.getFirstDayOfWeek()) {
            weekStart.add(Calendar.DATE, -1);
        }

        // process each week
        Calendar weekEnd;
        do {
            weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DATE, 7);
            processWeek(weekStart, weekEnd);
        } while (weekEnd.before(end));
    }

    /**
     * Collects the data from the persons for one week and saves it for later use in the reports.
     *
     * @param weekStart The beginning of the week, must this week's first day at 00:00:00
     * @param weekEnd   The end of the week, must be <i>next</i> week's first day at 00:00:00
     */
    private void processWeek(Calendar weekStart, Calendar weekEnd) {

    }


}
