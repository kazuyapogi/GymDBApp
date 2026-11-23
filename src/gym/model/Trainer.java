package gym.model;

import java.time.LocalDate;

/** One row of the trainer table. Owner: Jan Carlo Avelino. */
public class Trainer {
    /** Martial arts disciplines a coach can specialize in. */
    public static final String[] SPECIALIZATIONS = {
            "Arnis", "Boxing", "Brazilian Jiu-Jitsu", "Judo", "Karate", "Kickboxing",
            "Krav Maga", "MMA", "Muay Thai", "Taekwondo", "Wrestling"};
    public static final String[] STATUSES = {"Active", "Inactive"};

    public int trainerId;
    public String lastName;
    public String firstName;
    public String contactNumber;
    public String email;
    public String specialization;
    public LocalDate hireDate;
    public String status;

    public String fullName() {
        return lastName + ", " + firstName;
    }
}
