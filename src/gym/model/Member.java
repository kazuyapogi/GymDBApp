package gym.model;

import java.time.LocalDate;

/** One row of the member table. Owner: Kazuya Niki. */
public class Member {
    public static final String[] TYPES = {"Monthly", "Quarterly", "Annual"};
    public static final String[] STATUSES = {"Active", "Expired"};
    public static final String[] SEXES = {"Male", "Female"};

    public int memberId;
    public String lastName;
    public String firstName;
    public LocalDate birthdate;
    public String sex;
    public String contactNumber;
    public String email;
    public String membershipType;
    public LocalDate membershipStart;
    public LocalDate membershipEnd;
    public String status;

    public String fullName() {
        return lastName + ", " + firstName;
    }
}
