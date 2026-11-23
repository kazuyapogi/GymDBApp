package gym.model;

import java.time.LocalDate;

/** One row of the class_session table. */
public class ClassSession {
    public static final String[] STATUSES = {"Held", "Cancelled"};

    public int sessionId;
    public String classCode;
    public LocalDate sessionDate;
    public String status;

    /** Filled in by queries that join gym_class, trainer and class_attendance (for display only). */
    public String className;
    public String dayOfWeek;
    public String classStatus;
    public String trainerName;
    public int attendeeCount;
}
