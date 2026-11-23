package gym.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** One row of the class_attendance table. Owner: Matthew Mari Manzano. */
public class Attendance {
    public int attendanceId;
    public int memberId;
    public int sessionId;
    public LocalTime timeIn;

    /** Filled in by queries that join member, class_session, gym_class and trainer (for display only). */
    public String memberName;
    public LocalDate sessionDate;
    public String classCode;
    public String className;
    public String trainerName;
}
