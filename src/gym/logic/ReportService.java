package gym.logic;

import gym.data.ReportDAO;
import gym.model.ReportResult;

import java.sql.SQLException;
import java.time.YearMonth;

/** Logic tier for the four monthly reports: checks the period and passes its first and last day. */
public class ReportService {
    private final ReportDAO dao = new ReportDAO();

    public ReportResult memberActivity(int month, int year) throws ValidationException, SQLException {
        YearMonth p = period(month, year);
        return dao.memberActivity(p.atDay(1), p.atEndOfMonth());
    }

    public ReportResult trainerWorkload(int month, int year) throws ValidationException, SQLException {
        YearMonth p = period(month, year);
        return dao.trainerWorkload(p.atDay(1), p.atEndOfMonth());
    }

    public ReportResult classAttendanceSummary(int month, int year) throws ValidationException, SQLException {
        YearMonth p = period(month, year);
        return dao.classAttendanceSummary(p.atDay(1), p.atEndOfMonth());
    }

    public ReportResult membershipEngagement(int month, int year, String membershipType)
            throws ValidationException, SQLException {
        YearMonth p = period(month, year);
        ValidationException.required(membershipType, "Membership type");
        return dao.membershipEngagement(p.atDay(1), p.atEndOfMonth(), membershipType);
    }

    private static YearMonth period(int month, int year) throws ValidationException {
        ValidationException.check(month >= 1 && month <= 12, "Month must be from 1 to 12.");
        ValidationException.check(year >= 2000 && year <= 2100, "Year must be from 2000 to 2100.");
        return YearMonth.of(year, month);
    }
}
