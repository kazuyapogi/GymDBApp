package gym.model;

import java.util.ArrayList;
import java.util.List;

/** Column headers plus rows returned by a report query. */
public class ReportResult {
    public final String[] columns;
    public final List<Object[]> rows = new ArrayList<>();

    public ReportResult(String... columns) {
        this.columns = columns;
    }
}
