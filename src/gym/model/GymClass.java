package gym.model;

import java.time.LocalTime;

/** One row of the gym_class table. Owner: Charlotte Mae Ojeda. */
public class GymClass {
    public static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    public static final String[] STATUSES = {"Active", "Inactive"};

    public String classCode;
    public String className;
    public String description;
    public int trainerId;
    public String dayOfWeek;
    public LocalTime startTime;
    public LocalTime endTime;
    public int maxCapacity;
    public String status;

    /** Filled in by queries that join the trainer table (for display only). */
    public String trainerName;
}
